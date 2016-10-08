package org.elastic.rest.scala.driver.json

import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseImplicits._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.json.utils.MacroUtils
import org.elastic.rest.scala.driver.utils.MacroUtils.OpType

import scala.concurrent.{ExecutionContext, Future}
import scala.language.experimental.macros

/** Integration for CIRCE with REST drivers - handles typed APIs
  */
object CirceTypeModule {

  /** Typed outputs */
  implicit class CirceTypedStringToTypeHelper[T](val typedOp: TypedOperation[T]) extends StringToTypedHelper[T] {

    override def exec()(implicit driver: RestDriver, ec: ExecutionContext): Future[T] =
      macro MacroUtils.execMaterialize[T]
  }

  // Lots of classes for the different typed inputs cases

  // Untyped output:

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperWithDataReadableTU[D <: BaseDriverOp, I]
    (val resource: RestWithDataReadableTU[D, I] with RestResource)
    extends TypedToStringHelperWithDataReadableTU[D, I]
  {
    @OpType("read")
    override def read(body: I): D = macro MacroUtils.writeMaterialize[D, I]
  }

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperSendableTU[D <: BaseDriverOp, I]
    (val resource: RestSendableTU[D, I] with RestResource)
    extends TypedToStringHelperSendableTU[D, I]
  {
    @OpType("send")
    override def send(body: I): D = macro MacroUtils.writeMaterialize[D, I]
  }

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperWritableTU[D <: BaseDriverOp, I]
    (val resource: RestWritableTU[D, I] with RestResource)
    extends TypedToStringHelperWritableTU[D, I]
  {
    @OpType("write")
    override def write(body: I): D = macro MacroUtils.writeMaterialize[D, I]
  }

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperWithDataDeletableTU[D <: BaseDriverOp, I]
    (val resource: RestWithDataDeletableTU[D, I] with RestResource)
    extends TypedToStringHelperWithDataDeletableTU[D, I]
  {
    @OpType("delete")
    override def delete(body: I): D = macro MacroUtils.writeMaterialize[D, I]
  }

  // Typed output:

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperWithDataReadableTT[D <: BaseDriverOp, I, O]
    (val resource: RestWithDataReadableTT[D, I, O] with RestResource)
    extends TypedToStringHelperWithDataReadableTT[D, I, O]
  {
    @OpType("read")
    override def read(body: I): D with TypedOperation[O] = macro MacroUtils.writeTypedOutputMaterialize[D, I, O]
  }

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperSendableTT[D <: BaseDriverOp, I, O]
    (val resource: RestSendableTT[D, I, O] with RestResource)
    extends TypedToStringHelperSendableTT[D, I, O]
  {
    @OpType("send")
    override def send(body: I): D with TypedOperation[O] = macro MacroUtils.writeTypedOutputMaterialize[D, I, O]
  }

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperWritableTT[D <: BaseDriverOp, I, O]
    (val resource: RestWritableTT[D, I, O] with RestResource)
    extends TypedToStringHelperWritableTT[D, I, O]
  {
    @OpType("write")
    override def write(body: I): D with TypedOperation[O] = macro MacroUtils.writeTypedOutputMaterialize[D, I, O]
  }

  /** Typed input case */
  implicit class CirceTypedStringToTypeHelperWithDataDeletableTT[D <: BaseDriverOp, I, O]
    (val resource: RestWithDataDeletableTT[D, I, O] with RestResource)
    extends TypedToStringHelperWithDataDeletableTT[D, I, O]
  {
    @OpType("delete")
    override def delete(body: I): D with TypedOperation[O] = macro MacroUtils.writeTypedOutputMaterialize[D, I, O]
  }
}
