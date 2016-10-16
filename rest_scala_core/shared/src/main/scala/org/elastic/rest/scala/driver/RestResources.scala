package org.elastic.rest.scala.driver

import RestBase._
import RestBaseImplicits._
import RestBaseRuntimeTyped._
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
  trait RestCheckable[D <: Modifier] { self: RestResource =>
    /** Creates a driver operation
      *
      * @return The driver operation
      */
    @MacroUtils.OpType("HEAD")
    def check(): D with BaseDriverOp = macro MacroUtils.materializeOpImpl[D]
  }

  /** The base typed check (HEAD) resource
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type (case class) of the return operation
    */
  trait RestCheckableT[D <: Modifier, O] extends RestCheckable[D] { self: RestResource =>
    /** Creates a driver operation
      *
      * @return The driver operation
      */
    @MacroUtils.OpType("HEAD")
    override def check(): D with TypedDriverOp[O] = macro MacroUtils.materializeOpImpl_TypedOutput[D, O]
  }

  // Readable

  /** The base readable resource (untyped)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestReadable[D <: Modifier] { self: RestResource =>
    /**
      * Creates a driver operation
      *
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    def read(): D with BaseDriverOp = macro MacroUtils.materializeOpImpl[D]
  }

  /** The base typed readable resource
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type (case class) of the return operation
    */
  trait RestReadableT[D <: Modifier, O] extends RestReadable[D] { self: RestResource =>
    /** Creates a typed driver operation
      *
      * @return The typed driver operation
      */
    @MacroUtils.OpType("GET")
    override def read(): D with TypedDriverOp[O] = macro MacroUtils.materializeOpImpl_TypedOutput[D, O]
  }

  // Readable with Data

  /** The base (untyped) readable resource where the reply is controlled by data written to the resource
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestWithDataReadable[D <: Modifier] { self: RestResource =>
    /** Creates a driver operation
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    def readS(body: String): D with BaseDriverOp = macro MacroUtils.materializeOpImpl_Body[D]

    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    def readJ[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D with BaseDriverOp =
      macro MacroUtils.materializeOpImpl_JBody[D, J]
  }

  /** The base readable resource where the reply is controlled by data written to the resource
    * (input untyped, output typed)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type (case class) of the return operation
    */
  trait RestWithDataReadableUT[D <: Modifier, O] extends RestWithDataReadable[D] { self: RestResource =>
    /**
      * Creates a driver operation
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    override def readS(body: String): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_Body_TypedOutput[D, O]

    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("GET")
    override def readJ[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_JBody_TypedOutput[D, J, O]
  }

  /** The base (untyped) readable resource where the reply is controlled by data written to the resource
    * (input typed, output untyped)
    *
    * Various implicits provide a typed `read` operation, eg see `RuntimeTypedToStringHelperWithDataReadableTU`
    * and `TypedToStringHelperWithDataReadableTU`
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    */
  trait RestWithDataReadableTU[D <: Modifier, I] extends RestWithDataReadable[D] { self: RestResource => }

  /** The base (untyped) readable resource where the reply is controlled by data written to the resource
    * (input typed, output typed)
    *
    * Various implicits provide a typed `read` operation, eg see `RuntimeTypedToStringHelperWithDataReadableTT`
    * and `TypedToStringHelperWithDataReadableTT`
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    * @tparam O The type of the output object
    */
  trait RestWithDataReadableTT[D <: Modifier, I, O] extends RestWithDataReadableUT[D, O] { self: RestResource => }

  // Sendable

  /** The base sendable resource (untyped in both input and output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestSendable[D <: Modifier] { self: RestResource =>
    /** Creates a driver operation
      *
      * @param body The String data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    def sendS(body: String): D with BaseDriverOp = macro MacroUtils.materializeOpImpl_Body[D]

    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    def sendJ[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D with BaseDriverOp =
      macro MacroUtils.materializeOpImpl_JBody[D, J]
  }

  /** The base sendable resource (typed input, untyped output)
    *
    * Various implicits provide a typed `send` operation, eg see `RuntimeTypedToStringHelperSendableTU`
    * and `TypedToStringHelperSendableTU`
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    */
  trait RestSendableTU[D <: Modifier, I] extends RestSendable[D] { self: RestResource => }

  /** The base sendable resource (untyped input, typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type of the output object
    */
  trait RestSendableUT[D <: Modifier, O] extends RestSendable[D] { self: RestResource =>
    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    override def sendJ[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_JBody_TypedOutput[D, J, O]

    /** Creates a driver operation
      *
      * @param body The string data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    override def sendS(body: String): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_Body_TypedOutput[D, O]
  }

  /** The base sendable resource (typed input, typed output)
    *
    * Various implicits provide a typed `send` operation, eg see `RuntimeTypedToStringHelperSendableTT`
    * and `TypedToStringHelperSendableTT`
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    * @tparam O The type of the output object
    */
  trait RestSendableTT[D <: Modifier, I, O] extends RestSendableUT[D, O] { self: RestResource => }

  // Sendable with no data

  /** The base sendable type, for cases where no data is written (untyped output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestNoDataSendable[D <: Modifier] { self: RestResource =>
    /** Creates a driver operation
      *
      * @return The driver operation
      */
    @MacroUtils.OpType("POST")
    def send(): D with BaseDriverOp = macro MacroUtils.materializeOpImpl[D]
  }

  /** The base sendable type, for cases where no data is written (typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type (case class) of the return operation
    */
  trait RestNoDataSendableT[D <: Modifier, O] extends RestNoDataSendable[D] { self: RestResource =>
    /** Creates a typed driver operation
      *
      * @return The typed driver operation
      */
    @MacroUtils.OpType("POST")
    override def send(): D with TypedDriverOp[O] = macro MacroUtils.materializeOpImpl_TypedOutput[D, O]
  }

  // Writable

  /** The base writable resource (untyped in both input and output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestWritable[D <: Modifier] { self: RestResource =>
    /** Creates a driver operation
      *
      * @param body The String data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    def writeS(body: String): D with BaseDriverOp = macro MacroUtils.materializeOpImpl_Body[D]

    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    def writeJ[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D with BaseDriverOp =
      macro MacroUtils.materializeOpImpl_JBody[D, J]
  }

  /** The base writable resource (typed input, untyped output)
    *
    * Various implicits provide a typed `write` operation, eg see `RuntimeTypedToStringHelperWritableTU`
    * and `TypedToStringHelperWritableTU`
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    */
  trait RestWritableTU[D <: Modifier, I] extends RestWritable[D] { self: RestResource => }

  /** The base writable resource (untyped input, typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type of the output object
    */
  trait RestWritableUT[D <: Modifier, O] extends RestWritable[D] { self: RestResource =>
    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    override def writeJ[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_JBody_TypedOutput[D, J, O]

    /** Creates a driver operation
      *
      * @param body The string data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    override def writeS(body: String): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_Body_TypedOutput[D, O]
  }

  /** The base writable resource (typed input, typed output)
    *
    * Various implicits provide a typed `write` operation, eg see `RuntimeTypedToStringHelperWritableTT`
    * and `TypedToStringHelperWritableTT`
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    * @tparam O The type of the output object
    */
  trait RestWritableTT[D <: Modifier, I, O] extends RestWritableUT[D, O] { self: RestResource => }

  // No data writable

  /** The base writable type, for cases where no data is written (untyped output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestNoDataWritable[D <: Modifier] { self: RestResource =>
    /** Creates a driver operation
      *
      * @return The driver operation
      */
    @MacroUtils.OpType("PUT")
    def write(): D with BaseDriverOp = macro MacroUtils.materializeOpImpl[D]
  }

  /** The base writable type, for cases where no data is written (typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type (case class) of the return operation
    */
  trait RestNoDataWritableT[D <: Modifier, O] extends RestNoDataWritable[D] { self: RestResource =>
    /** Creates a typed driver operation
      *
      * @return The typed driver operation
      */
    @MacroUtils.OpType("PUT")
    override def write(): D with TypedDriverOp[O] = macro MacroUtils.materializeOpImpl_TypedOutput[D, O]
  }

  // Deletable

  /** The base deletable type
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestDeletable[D <: Modifier] { self: RestResource =>
    /** Creates a driver operation
      *
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    def delete(): D with BaseDriverOp = macro MacroUtils.materializeOpImpl[D]
  }

  /** The base deletable type (typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type (case class) of the return operation
    */
  trait RestDeletableT[D <: Modifier, O] extends RestDeletable[D] { self: RestResource =>
    /** Creates a typed driver operation
      *
      * @return The typed driver operation
      */
    @MacroUtils.OpType("DELETE")
    override def delete(): D with TypedDriverOp[O] = macro MacroUtils.materializeOpImpl_TypedOutput[D, O]
  }

  // With Data Deletable

  /** The base deletable type where the delete is controlled by data written to the resource
    * (untyped input, untyped output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    */
  trait RestWithDataDeletable[D <: Modifier] { self: RestResource =>
    /** Creates a driver operation
      *
      * @param body The data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    def deleteS(body: String): D with BaseDriverOp = macro MacroUtils.materializeOpImpl_Body[D]

    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    def deleteJ[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D with BaseDriverOp =
      macro MacroUtils.materializeOpImpl_JBody[D, J]
  }

  /** The base deletable type where the delete is controlled by data written to the resource
    * (typed input, untyped output)
    *
    * Various implicits provide a typed `delete` operation, eg see `RuntimeTypedToStringHelperWithDataDeletableTU`
    * and `TypedToStringHelperWithDataDeletableTU`
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    */
  trait RestWithDataDeletableTU[D <: Modifier, I] extends RestWithDataDeletable[D] { self: RestResource => }

  /** The base deletable type where the delete is controlled by data written to the resource
    * (untyped input, typed output)
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam O The type of the output object
    */
  trait RestWithDataDeletableUT[D <: Modifier, O] extends RestWithDataDeletable[D] { self: RestResource =>
    /** Creates a driver operation
      *
      * @param body The JSON data to write to the resource
      * @param jsonToStringHelper The implicit per-JSON-lib to generate
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    override def deleteJ[J](body: J)(implicit jsonToStringHelper: JsonToStringHelper[J]): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_JBody_TypedOutput[D, J, O]

    /** Creates a driver operation
      *
      * @param body The string data to write to the resource
      * @return The driver operation
      */
    @MacroUtils.OpType("DELETE")
    override def deleteS(body: String): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_Body_TypedOutput[D, O]
  }

  /** The base deletable type where the delete is controlled by data written to the resource
    * (untyped input, untyped output)
    *
    * Various implicits provide a typed `delete` operation, eg see `RuntimeTypedToStringHelperWithDataDeletableTT`
    * and `TypedToStringHelperWithDataDeletableTT`
    *
    * @tparam D The group of modifier operations supported mixed into the `BaseDriverOp`
    * @tparam I The type of the input object
    * @tparam O The type of the output object
    */
  trait RestWithDataDeletableTT[D <: Modifier, I, O] extends RestWithDataDeletableUT[D, O] { self: RestResource => }
}
