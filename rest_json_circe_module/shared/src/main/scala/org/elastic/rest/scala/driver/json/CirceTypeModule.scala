package org.elastic.rest.scala.driver.json

import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseImplicits._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.json.utils.MacroUtils
import org.elastic.rest.scala.driver.utils.MacroUtils.OpType

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.util.Try

/** Integration for CIRCE with REST drivers - handles typed APIs
  */
object CirceTypeModule {

  /** Typed outputs */
  implicit class CirceTypedStringToTypeHelper[T](val typedOp: TypedDriverOp[T]) extends StringToTypedHelper[T] {

    //TODO: use a class tag to allow this implicit to return the CIRCE specific version of a generic output
    // Then the actual REST service has a list of traits for the output types, but the implicit figures out
    // which _actual_ class to use

    override def exec[O <: T]
      ()(implicit driver: RestDriver, ec: ExecutionContext, ct: ClassTag[O]): Future[O] =
        macro MacroUtils.execMaterialize[O]

    override def result[O <: T]
      (timeout: Duration)(implicit driver: RestDriver, ec: ExecutionContext, ct: ClassTag[O]): Try[O] =
        macro MacroUtils.resultMaterialize[O]

    /** Actually executes the operation (sync), with default timeout
      * This version uses the runtime implicits (JVM only and it is recommended to use the macro implicits where
      * possible)
      *
      * @param driver The driver which executes the operation
      * @param ec The execution context for futures
      * @return The result of the operation as a type
      */
    def result()(implicit driver: RestDriver, ec: ExecutionContext): Try[T] =
      macro MacroUtils.resultMaterializeNoTimeout[T]
  }

  // Lots of classes for the different typed inputs cases

  // Untyped output:

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperWithDataReadableTU[D <: Modifier, I]
    (val resource: RestWithDataReadableTU[D, I] with RestResource)
    extends TypedToStringHelperWithDataReadableTU[D, I]
  {
    @OpType("read")
    override def read(body: I): D with BaseDriverOp = macro MacroUtils.writeMaterialize[D, I]
  }

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperSendableTU[D <: Modifier, I]
    (val resource: RestSendableTU[D, I] with RestResource)
    extends TypedToStringHelperSendableTU[D, I]
  {
    @OpType("send")
    override def send(body: I): D with BaseDriverOp = macro MacroUtils.writeMaterialize[D, I]
  }

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperWritableTU[D <: Modifier, I]
    (val resource: RestWritableTU[D, I] with RestResource)
    extends TypedToStringHelperWritableTU[D, I]
  {
    @OpType("write")
    override def write(body: I): D with BaseDriverOp = macro MacroUtils.writeMaterialize[D, I]
  }

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperWithDataDeletableTU[D <: Modifier, I]
    (val resource: RestWithDataDeletableTU[D, I] with RestResource)
    extends TypedToStringHelperWithDataDeletableTU[D, I]
  {
    @OpType("delete")
    override def delete(body: I): D with BaseDriverOp = macro MacroUtils.writeMaterialize[D, I]
  }

  // Typed output:

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperWithDataReadableTT[D <: Modifier, I, O]
    (val resource: RestWithDataReadableTT[D, I, O] with RestResource)
    extends TypedToStringHelperWithDataReadableTT[D, I, O]
  {
    @OpType("read")
    override def read(body: I): D with TypedDriverOp[O] = macro MacroUtils.writeTypedOutputMaterialize[D, I, O]
  }

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperSendableTT[D <: Modifier, I, O]
    (val resource: RestSendableTT[D, I, O] with RestResource)
    extends TypedToStringHelperSendableTT[D, I, O]
  {
    @OpType("send")
    override def send(body: I): D with TypedDriverOp[O] = macro MacroUtils.writeTypedOutputMaterialize[D, I, O]
  }

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperWritableTT[D <: Modifier, I, O]
    (val resource: RestWritableTT[D, I, O] with RestResource)
    extends TypedToStringHelperWritableTT[D, I, O]
  {
    @OpType("write")
    override def write(body: I): D with TypedDriverOp[O] = macro MacroUtils.writeTypedOutputMaterialize[D, I, O]
  }

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperWithDataDeletableTT[D <: Modifier, I, O]
    (val resource: RestWithDataDeletableTT[D, I, O] with RestResource)
    extends TypedToStringHelperWithDataDeletableTT[D, I, O]
  {
    @OpType("delete")
    override def delete(body: I): D with TypedDriverOp[O] = macro MacroUtils.writeTypedOutputMaterialize[D, I, O]
  }
}
