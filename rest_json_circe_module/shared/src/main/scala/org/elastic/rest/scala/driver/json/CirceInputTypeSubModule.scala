package org.elastic.rest.scala.driver.json

import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseImplicits._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.json.utils.MacroUtils
import org.elastic.rest.scala.driver.utils.MacroUtils.OpType

import scala.language.experimental.macros

/** Integration for CIRCE with REST drivers - handles typed APIs
  * Only handles the input types - see `flexible_typing.CirceTypeModule` or `fixed_typing.CirceTypeModule`
  * for output typing
  */
trait CirceInputTypeSubModule {

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
object CirceInputTypeSubModule extends CirceInputTypeSubModule