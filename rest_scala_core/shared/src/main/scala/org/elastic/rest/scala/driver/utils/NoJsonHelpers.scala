package org.elastic.rest.scala.driver.utils

import org.elastic.rest.scala.driver.RestBase.{BaseDriverOp, Modifier, RestResource, TypedDriverOp}
import org.elastic.rest.scala.driver.RestBaseImplicits._
import org.elastic.rest.scala.driver.RestResources._

import scala.language.experimental.macros

/** Helper implicits for custom types
  */
object NoJsonHelpers {

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperWithDataReadableTU[D <: Modifier, I <: CustomTypedToString]
    (val resource: RestWithDataReadableTU[D, I] with RestResource)
    extends TypedToStringHelperWithDataReadableTU[D, I]
  {
    @MacroUtils.OpType("GET")
    override def read(body: I): D with BaseDriverOp = macro MacroUtils.materializeOpImpl_CBodyCustom[D, I]
  }

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperSendableTU[D <: Modifier, I <: CustomTypedToString]
    (val resource: RestSendableTU[D, I] with RestResource)
    extends TypedToStringHelperSendableTU[D, I]
  {
    @MacroUtils.OpType("POST")
    override def send(body: I): D with BaseDriverOp = macro MacroUtils.materializeOpImpl_CBodyCustom[D, I]
  }

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperWritableTU[D <: Modifier, I <: CustomTypedToString]
    (val resource: RestWritableTU[D, I] with RestResource)
    extends TypedToStringHelperWritableTU[D, I]
  {
    @MacroUtils.OpType("PUT")
    override def write(body: I): D with BaseDriverOp = macro MacroUtils.materializeOpImpl_CBodyCustom[D, I]
  }

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperWithDataDeletableTU[D <: Modifier, I <: CustomTypedToString]
    (val resource: RestWithDataDeletableTU[D, I] with RestResource)
    extends TypedToStringHelperWithDataDeletableTU[D, I]
  {
    @MacroUtils.OpType("DELETE")
    override def delete(body: I): D with BaseDriverOp = macro MacroUtils.materializeOpImpl_CBodyCustom[D, I]
  }

  // Typed output:

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperWithDataReadableTT[D <: Modifier, I <: CustomTypedToString, O]
    (val resource: RestWithDataReadableTT[D, I, O] with RestResource)
    extends TypedToStringHelperWithDataReadableTT[D, I, O]
  {
    @MacroUtils.OpType("GET")
    override def read(body: I): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_CBodyCustom_TypedOutput[D, I, O]
  }

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperSendableTT[D <: Modifier, I <: CustomTypedToString, O]
    (val resource: RestSendableTT[D, I, O] with RestResource)
    extends TypedToStringHelperSendableTT[D, I, O]
  {
    @MacroUtils.OpType("POST")
    override def send(body: I): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_CBodyCustom_TypedOutput[D, I, O]
  }

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperWritableTT[D <: Modifier, I <: CustomTypedToString, O]
    (val resource: RestWritableTT[D, I, O] with RestResource)
    extends TypedToStringHelperWritableTT[D, I, O]
  {
    @MacroUtils.OpType("PUT")
    override def write(body: I): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_CBodyCustom_TypedOutput[D, I, O]
  }

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperWithDataDeletableTT[D <: Modifier, I <: CustomTypedToString, O]
    (val resource: RestWithDataDeletableTT[D, I, O] with RestResource)
    extends TypedToStringHelperWithDataDeletableTT[D, I, O]
  {
    @MacroUtils.OpType("DELETE")
    override def delete(body: I): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_CBodyCustom_TypedOutput[D, I, O]
  }

}
