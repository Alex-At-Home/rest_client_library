package org.elastic.rest.scala.driver.json.fixed_typing

import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseImplicits._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.json.CirceInputTypeSubModule
import org.elastic.rest.scala.driver.json.utils.MacroUtils

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.language.experimental.macros
import scala.util.Try

/** Integration for CIRCE with REST drivers - handles typed APIs
  * Supports only concrete output types (cf `flexible_typing.CirceTypeModule`), although input types can be flexible
  * (ie the resource declares a trait and then different modules can provide different concrete classes - eg
  * supporting different JSON libs)
  */
object CirceTypeModule extends CirceInputTypeSubModule {

  /** Typed outputs */
  implicit class CirceTypedStringToTypeHelper[T](val typedOp: TypedDriverOp[T]) extends StringToTypedHelper[T] {

    override def exec()(implicit driver: RestDriver, ec: ExecutionContext): Future[T] =
        macro MacroUtils.execMaterializeFixed[T]

    override def result(timeout: Duration)(implicit driver: RestDriver, ec: ExecutionContext): Try[T] =
        macro MacroUtils.resultMaterializeFixed[T]

    /** Actually executes the operation (sync), with default timeout
      *
      * @param driver The driver which executes the operation
      * @param ec The execution context for futures
      * @return The result of the operation as a type
      */
    def result()(implicit driver: RestDriver, ec: ExecutionContext): Try[T] =
      macro MacroUtils.resultMaterializeFixedNoTimeout[T]
  }
}
