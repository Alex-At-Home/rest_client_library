package org.elastic.rest.scala.driver.json.flexible_typing

import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseImplicits._
import org.elastic.rest.scala.driver.json.CirceInputTypeSubModule
import org.elastic.rest.scala.driver.json.utils.MacroUtils

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.language.experimental.macros
import scala.util.Try

/** Integration for CIRCE with REST drivers - handles typed APIs
  * Supports flexibly-typed outputs (as well as inputs), ie the output type can be a trait and then different
  * (mutually exclusive) modules can provide different concrete versions (eg supporting different JSON libs)
  */
object CirceTypeModule extends CirceInputTypeSubModule {

  /** Typed outputs */
  implicit class CirceTypedStringToTypeHelper[T](val typedOp: TypedDriverOp[T]) extends StringToFlexibleTypedHelper[T] {

    override def exec[O <: T]
      ()(implicit driver: RestDriver, ec: ExecutionContext, ev: RegisterType[O]): Future[O] =
        macro MacroUtils.execMaterializeFlexible[O]

    override def result[O <: T]
      (timeout: Duration)(implicit driver: RestDriver, ec: ExecutionContext, ev: RegisterType[O]): Try[O] =
        macro MacroUtils.resultMaterializeFlexible[O]

    /** Actually executes the operation (sync), with default timeout
      *
      * @param driver The driver which executes the operation
      * @param ec The execution context for futures
      * @param ev The evidence for the registered type
      * @return The result of the operation as a type
      */
    def result[O <: T]()(implicit driver: RestDriver, ec: ExecutionContext, ev: RegisterType[O]): Try[O] =
      macro MacroUtils.resultMaterializeFlexibleNoTimeout[O]
  }
}
