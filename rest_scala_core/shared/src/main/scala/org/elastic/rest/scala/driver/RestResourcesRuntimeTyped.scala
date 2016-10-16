package org.elastic.rest.scala.driver

import org.elastic.rest.scala.driver.RestBase.{BaseDriverOp, Modifier, RestResource, TypedDriverOp}
import org.elastic.rest.scala.driver.RestBaseRuntimeTyped.RuntimeTypedToStringHelper
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.utils.MacroUtils

import scala.language.experimental.macros

/** Implicits providing typed inputs for the REST resource
  */
object RestResourcesRuntimeTyped {

  // Typed input

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    *
    * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWithDataReadableTU`
    * unless there is good reason not to)
    *
    * @param resource The underlying resource that this implicit is wrapping
    */
  implicit class RuntimeTypedToStringHelperWithDataReadableTU[D <: Modifier, I](val resource: RestWithDataReadableTU[D, I] with RestResource) {
    /** Creates a driver operation
      *
      * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWithDataReadableTU`
      * unless there is good reason not to)
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    def read(body: I)(implicit typeToStringHelper: RuntimeTypedToStringHelper): D with BaseDriverOp =
      macro MacroUtils.materializeOpImpl_CBody[D, I]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    *
    * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWithDataReadableTU`
    * unless there is good reason not to)
    *
    * @param resource The underlying resource that this implicit is wrapping
    */
  implicit class RuntimeTypedToStringHelperWithDataReadableTT[D <: Modifier, I, O](val resource: RestWithDataReadableTT[D, I, O] with RestResource) {

    /** Creates a driver operation
      *
      * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWithDataReadableTT`
      * unless there is good reason not to)
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    def read(body: I)(implicit typeToStringHelper: RuntimeTypedToStringHelper): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_CBody_TypedOutput[D, I, O]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    *
    * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWithDataReadableTU`
    * unless there is good reason not to)
    *
    * @param resource The underlying resource that this implicit is wrapping
    */
  implicit class RuntimeTypedToStringHelperWritableTU[D <: Modifier, I](val resource: RestWritableTU[D, I] with RestResource) {

    /** Creates a driver operation
      *
      * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWritableTU`
      * unless there is good reason not to)
      *
      * @param body The typed data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    def write(body: I)(implicit typeToStringHelper: RuntimeTypedToStringHelper): D with BaseDriverOp =
      macro MacroUtils.materializeOpImpl_CBody[D, I]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    *
    * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWithDataReadableTU`
    * unless there is good reason not to)
    *
    * @param resource The underlying resource that this implicit is wrapping
    */
  implicit class RuntimeTypedToStringHelperWritableTT[D <: Modifier, I, O](val resource: RestWritableTT[D, I, O] with RestResource) {

    /** Creates a driver operation
      *
      * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWritableTT`
      * unless there is good reason not to)
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    def write(body: I)(implicit typeToStringHelper: RuntimeTypedToStringHelper): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_CBody_TypedOutput[D, I, O]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    *
    * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWithDataReadableTU`
    * unless there is good reason not to)
    *
    * @param resource The underlying resource that this implicit is wrapping
    */
  implicit class RuntimeTypedToStringHelperSendableTU[D <: Modifier, I](val resource: RestSendableTU[D, I] with RestResource) {

    /** Creates a driver operation
      *
      * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperSendableTU`
      * unless there is good reason not to)
      *
      * @param body The typed data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    def send(body: I)(implicit typeToStringHelper: RuntimeTypedToStringHelper): D with BaseDriverOp =
      macro MacroUtils.materializeOpImpl_CBody[D, I]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    *
    * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWithDataReadableTU`
    * unless there is good reason not to)
    *
    * @param resource The underlying resource that this implicit is wrapping
    */
  implicit class RuntimeTypedToStringHelperSendableTT[D <: Modifier, I, O](val resource: RestSendableTT[D, I, O] with RestResource) {

    /** Creates a driver operation
      *
      * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperSendableTT`
      * unless there is good reason not to)
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    def send(body: I)(implicit typeToStringHelper: RuntimeTypedToStringHelper): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_CBody_TypedOutput[D, I, O]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    *
    * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWithDataReadableTU`
    * unless there is good reason not to)
    *
    * @param resource The underlying resource that this implicit is wrapping
    */
  implicit class RuntimeTypedToStringHelperWithDataDeletableTU[D <: Modifier, I](val resource: RestWithDataDeletableTU[D, I] with RestResource) {

    /** Creates a driver operation
      *
      * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWithDataDeletableTU`
      * unless there is good reason not to)
      *
      * @param body The typed data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    def delete(body: I)(implicit typeToStringHelper: RuntimeTypedToStringHelper): D with BaseDriverOp =
      macro MacroUtils.materializeOpImpl_CBody[D, I]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    *
    * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWithDataReadableTU`
    * unless there is good reason not to)
    *
    * @param resource The underlying resource that this implicit is wrapping
    */
  implicit class RuntimeTypedToStringHelperWithDataDeletableTT[D <: Modifier, I, O]
    (val resource: RestWithDataDeletableTT[D, I, O] with RestResource)
  {

    /** Creates a driver operation
      *
      * (This is the runtime version, it is recommended to use the macro version `TypedToStringHelperWithDataDeletableTT`
      * unless there is good reason not to)
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    def delete(body: I)(implicit typeToStringHelper: RuntimeTypedToStringHelper): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_CBody_TypedOutput[D, I, O]
  }
}
