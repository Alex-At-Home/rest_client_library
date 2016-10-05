package org.elastic.rest.scala.driver

import RestBase.{TypedOperation, _}
import org.elastic.rest.scala.driver.utils.MacroUtils

import scala.language.experimental.macros

//TODO include a trait that returns documentation (eg from embedded scaladoc)

/** Contains the base operations that, associated with the modifiers, can be executed against the
  * REST resources
  */
object RestResources {

  // Checking

  /** The base check (HEAD) resource (untyped)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestCheckable[D <: BaseDriverOp] { self: RestResource =>
    /** Creates a driver operation
      *
      * @return The driver operation
      */
    @MacroUtils.OpType("HEAD")
    def check(): D = macro MacroUtils.materializeOpImpl[D]
  }

  /** The base typed check (HEAD) resource
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type (case class) of the return operation
    */
  trait RestCheckableT[D <: BaseDriverOp, O] extends RestCheckable[D] { self: RestResource =>
    /** Creates a driver operation
      *
      * @return The driver operation
      */
    @MacroUtils.OpType("HEAD")
    override def check(): D with TypedOperation[O] = macro MacroUtils.materializeOpImpl_TypedOutput[D, O]
  }

  // Readable

  /** The base readable resource (untyped)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestReadable[D <: BaseDriverOp] { self: RestResource =>
    /**
      * Creates a driver operation
      *
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    def read(): D = macro MacroUtils.materializeOpImpl[D]
  }

  /** The base typed readable resource
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type (case class) of the return operation
    */
  trait RestReadableT[D <: BaseDriverOp, O] extends RestReadable[D] { self: RestResource =>
    /** Creates a typed driver operation
      *
      * @return The typed driver operation
      */
    @MacroUtils.OpType("GET")
    override def read(): D with TypedOperation[O] = macro MacroUtils.materializeOpImpl_TypedOutput[D, O]
  }

  // Readable with Data

  /** The base (untyped) readable resource where the reply is controlled by data written to the resource
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestWithDataReadable[D <: BaseDriverOp] {
    /** Creates a driver operation
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    def read(body: String): D = macro MacroUtils.materializeOpImpl_Body[D]

    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    def read[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D =
      macro MacroUtils.materializeOpImpl_JBody[D, J]
  }

  /** The base readable resource where the reply is controlled by data written to the resource
    * (input untyped, output typed)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type (case class) of the return operation
    */
  trait RestWithDataReadableUT[D <: BaseDriverOp, O] extends RestWithDataReadable[D] {
    /**
      * Creates a driver operation
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    override def read(body: String): D with TypedOperation[O] =
      macro MacroUtils.materializeOpImpl_Body_TypedOutput[D, O]

    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    override def read[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D with TypedOperation[O] =
      macro MacroUtils.materializeOpImpl_JBody_TypedOutput[D, J, O]
  }

  /** The base (untyped) readable resource where the reply is controlled by data written to the resource
    * (input typed, output untyped)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    */
  trait RestWithDataReadableTU[D <: BaseDriverOp, I] extends RestWithDataReadable[D] {
    /** Creates a driver operation
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    def read(body: I)(implicit typeToStringHelper: TypedToStringHelper): D =
      macro MacroUtils.materializeOpImpl_CBody[D, I]
  }

  /** The base (untyped) readable resource where the reply is controlled by data written to the resource
    * (input typed, output typed)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    * @tparam O The type of the output object
    */
  trait RestWithDataReadableTT[D <: BaseDriverOp, I, O] extends RestWithDataReadableUT[D, O] {
    /** Creates a driver operation
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    def read(body: I)(implicit typeToStringHelper: TypedToStringHelper): D with TypedOperation[O] =
      macro MacroUtils.materializeOpImpl_CBody_TypedOutput[D, I, O]
  }

  // Sendable

  /** The base sendable resource (untyped in both input and output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestSendable[D <: BaseDriverOp] {
    /** Creates a driver operation
      *
      * @param body The String data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    def send(body: String): D = macro MacroUtils.materializeOpImpl_Body[D]

    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    def send[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D =
      macro MacroUtils.materializeOpImpl_JBody[D, J]
  }

  /** The base sendable resource (typed input, untyped output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    */
  trait RestSendableTU[D <: BaseDriverOp, I] extends RestSendable[D] {
    /** Creates a driver operation
      *
      * @param body The typed data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    def send(body: I)(implicit typeToStringHelper: TypedToStringHelper): D =
      macro MacroUtils.materializeOpImpl_CBody[D, I]
  }

  /** The base sendable resource (untyped input, typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type of the output object
    */
  trait RestSendableUT[D <: BaseDriverOp, O] extends RestSendable[D] {
    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    override def send[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D with TypedOperation[O] =
      macro MacroUtils.materializeOpImpl_JBody_TypedOutput[D, J, O]

    /** Creates a driver operation
      *
      * @param body The string data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    override def send(body: String): D with TypedOperation[O] =
      macro MacroUtils.materializeOpImpl_Body_TypedOutput[D, O]
  }

  /** The base sendable resource (typed input, typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    * @tparam O The type of the output object
    */
  trait RestSendableTT[D <: BaseDriverOp, I, O] extends RestSendableUT[D, O] {
    /** Creates a driver operation
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    def send(body: I)(implicit typeToStringHelper: TypedToStringHelper): D with TypedOperation[O] =
      macro MacroUtils.materializeOpImpl_CBody_TypedOutput[D, I, O]
  }

  // Sendable with no data

  /** The base sendable type, for cases where no data is written (untyped output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestNoDataSendable[D <: BaseDriverOp] {
    /** Creates a driver operation
      *
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    def send(): D = macro MacroUtils.materializeOpImpl[D]
  }

  /** The base sendable type, for cases where no data is written (typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type (case class) of the return operation
    */
  trait RestNoDataSendableT[D <: BaseDriverOp, O] extends RestNoDataSendable[D] { self: RestResource =>
    /** Creates a typed driver operation
      *
      * @return The typed driver operation
      */
    @MacroUtils.OpType("POST")
    override def send(): D with TypedOperation[O] = macro MacroUtils.materializeOpImpl_TypedOutput[D, O]
  }

  // Writable

  /** The base writable resource (untyped in both input and output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestWritable[D <: BaseDriverOp] {
    /** Creates a driver operation
      *
      * @param body The String data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    def write(body: String): D = macro MacroUtils.materializeOpImpl_Body[D]

    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    def write[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D =
      macro MacroUtils.materializeOpImpl_JBody[D, J]
  }

  /** The base writable resource (typed input, untyped output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    */
  trait RestWritableTU[D <: BaseDriverOp, I] extends RestWritable[D] {
    /** Creates a driver operation
      *
      * @param body The typed data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    def write(body: I)(implicit typeToStringHelper: TypedToStringHelper): D =
      macro MacroUtils.materializeOpImpl_CBody[D, I]
  }

  /** The base writable resource (untyped input, typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type of the output object
    */
  trait RestWritableUT[D <: BaseDriverOp, O] extends RestWritable[D] {
    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    override def write[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D with TypedOperation[O] =
      macro MacroUtils.materializeOpImpl_JBody_TypedOutput[D, J, O]

    /** Creates a driver operation
      *
      * @param body The string data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    override def write(body: String): D with TypedOperation[O] =
      macro MacroUtils.materializeOpImpl_Body_TypedOutput[D, O]
  }

  /** The base writable resource (typed input, typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    * @tparam O The type of the output object
    */
  trait RestWritableTT[D <: BaseDriverOp, I, O] extends RestWritableUT[D, O] {
    /** Creates a driver operation
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    def write(body: I)(implicit typeToStringHelper: TypedToStringHelper): D with TypedOperation[O] =
      macro MacroUtils.materializeOpImpl_CBody_TypedOutput[D, I, O]
  }

  // No data writable

  /** The base writable type, for cases where no data is written (untyped output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestNoDataWritable[D <: BaseDriverOp] {
    /** Creates a driver operation
      *
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    def write(): D = macro MacroUtils.materializeOpImpl[D]
  }

  /** The base writable type, for cases where no data is written (typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type (case class) of the return operation
    */
  trait RestNoDataWritableT[D <: BaseDriverOp, O] extends RestNoDataWritable[D] { self: RestResource =>
    /** Creates a typed driver operation
      *
      * @return The typed driver operation
      */
    @MacroUtils.OpType("PUT")
    override def write(): D with TypedOperation[O] = macro MacroUtils.materializeOpImpl_TypedOutput[D, O]
  }

  // Deletable

  /** The base deletable type
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestDeletable[D <: BaseDriverOp] {
    /** Creates a driver operation
      *
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    def delete(): D = macro MacroUtils.materializeOpImpl[D]
  }

  /** The base deletable type (typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type (case class) of the return operation
    */
  trait RestDeletableT[D <: BaseDriverOp, O] extends RestDeletable[D] { self: RestResource =>
    /** Creates a typed driver operation
      *
      * @return The typed driver operation
      */
    @MacroUtils.OpType("DELETE")
    override def delete(): D with TypedOperation[O] = macro MacroUtils.materializeOpImpl_TypedOutput[D, O]
  }

  // With Data Deletable

  /** The base deletable type where the delete is controlled by data written to the resource
    * (untyped input, untyped output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestWithDataDeletable[D <: BaseDriverOp] {
    /** Creates a driver operation
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    def delete(body: String): D = macro MacroUtils.materializeOpImpl_Body[D]

    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    def delete[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D =
      macro MacroUtils.materializeOpImpl_JBody[D, J]
  }

  /** The base deletable type where the delete is controlled by data written to the resource
    * (typed input, untyped output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    */
  trait RestWithDataDeletableTU[D <: BaseDriverOp, I] extends RestWithDataDeletable[D] {
    /** Creates a driver operation
      *
      * @param body The typed data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    def delete(body: I)(implicit typeToStringHelper: TypedToStringHelper): D =
      macro MacroUtils.materializeOpImpl_CBody[D, I]
  }

  /** The base deletable type where the delete is controlled by data written to the resource
    * (untyped input, typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type of the output object
    */
  trait RestWithDataDeletableUT[D <: BaseDriverOp, O] extends RestWithDataDeletable[D] {
    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    override def delete[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D with TypedOperation[O] =
      macro MacroUtils.materializeOpImpl_JBody_TypedOutput[D, J, O]

    /** Creates a driver operation
      *
      * @param body The string data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    override def delete(body: String): D with TypedOperation[O] =
      macro MacroUtils.materializeOpImpl_Body_TypedOutput[D, O]
  }

  /** The base deletable type where the delete is controlled by data written to the resource
    * (untyped input, untyped output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    * @tparam O The type of the output object
    */
  trait RestWithDataDeletableTT[D <: BaseDriverOp, I, O] extends RestWithDataDeletableUT[D, O] {
    /** Creates a driver operation
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    def delete(body: I)(implicit typeToStringHelper: TypedToStringHelper): D with TypedOperation[O] =
      macro MacroUtils.materializeOpImpl_CBody_TypedOutput[D, I, O]
  }
}