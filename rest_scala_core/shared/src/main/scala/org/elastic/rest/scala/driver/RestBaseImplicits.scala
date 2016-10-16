package org.elastic.rest.scala.driver

import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestResources._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Try

/** A container for the base classes that are used
  * to generate implicits for the "bring your own" JSON and type libraries
  * See also `RestBaseTyped` for runtime implicits (JVM only and not recommended)
  */
object RestBaseImplicits {

  // JSON:

  /** A trait to be implemented and used as an implicit to define how to go from JSON to string
    * (defaults to `j.toString`)
    *
    * @tparam J The json object type in this library
    */
  trait JsonToStringHelper[J] {
    /** Creates a String from the JSON object of the registered (via implicit) JSON lib
      * (probably can normally be `j.toString`, which is therefore left in as a default)
      *
      * @param j The JSON object
      * @return The JSON
      */
    def fromJson(j: J): String = j.toString
  }

  /** A trait to be implemented as an implicit class to provide the implicit methods `execJ`
    * and `resultJ`. Note different to `JsonToStringHelper` in that you declare this as an implicit class
    * vs `JsonToStringHelper` which you declare as an implicit value
    *
    * @tparam J The JSON type
    */
  trait StringToJsonHelper[J] {
    /** Actually executes the operation (async)
      *
      * @param driver The driver which executes the operation
      * @return A future containing the result of the operation as a JSON object
      */
    def execJ()(implicit driver: RestDriver): Future[J]

    /** Actually executes the operation (sync)
      *
      * @param timeout Optionally, the amount of time to wait before failing
      * @param driver The driver which executes the operation
      * @return A future containing the result of the operation as a JSON object
      */
    def resultJ(timeout: Duration = null)(implicit driver: RestDriver): Try[J] =
      Try { Await.result(this.execJ(), Option(timeout).getOrElse(driver.timeout)) }
  }

  // Custom typed (no ser/deser library required)

  /** Case classes that want a custom overwrite should inherit this trait and implement
    * `fromTyped`, bypasses needing a JSON library with an overridden serializer etc etc
    */
  trait CustomTypedToString {
    /** Converts self to JSON string
      *
      * @return self as JSON string
      */
    def fromTyped: String
  }

  /** Classes that want a custom overwrite as return types should inherit this trait
    * and implement `toType`, typically still use a JSON library, eg to wrap a JSON element
    *  and provide helpers
    *
    *  This is a pure trait, used by type handlers (like `CirceTypeModule` to decide when to override using
    *  standard JSON processing; all children of it must support a constructor with a single string arg
    *  (ie the response from the REST driver)
    *
    *  Type handlers (like `CirceTypeModule`) should use `NoJsonHelpers.createCustomTyped(s)`
    */
  trait CustomStringToTyped

  // Typed (common between JS and JVM)

  // Typed output

  /** A trait to be implemented and used as an implicit to define how to go from a typed object
    * (eg case class) to a string, normally via JSON unless derived from `CustomTypedToString`
    *
    * (The `exec` method needs to be overriden in the concrete implementation, `typedOp` should be the implicit
    *  class's input param)
    *
    * Note the implicit implementation needs to check for `CustomStringToTyped` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomStringToTyped])` then can simply do `$ct(s)` in macro-land)
    *
    * @tparam T The output type to decode
    */
  trait StringToTypedHelper[T] {
    /** The typed operation the implicit encloses */
    val typedOp: TypedDriverOp[T]

    /** Actually executes the operation (aysnc)
      *
      * @param driver The driver which executes the operation
      * @param ec The execution context for futures
      * @return A future containing the result of the operation as a type
      */
    def exec()(implicit driver: RestDriver, ec: ExecutionContext): Future[T] = EmptyBody

    /** Actually executes the operation (sync)
      * This version uses the runtime implicits (JVM only and it is recommended to use the macro implicits where
      * possible)
      *
      * @param timeout Optionally, the amount of time to wait before failing
      * @param driver The driver which executes the operation
      * @param ec The execution context for futures
      * @return The result of the operation as a type
      */
    def result(timeout: Duration = null)(implicit driver: RestDriver, ec: ExecutionContext): Try[T] =
      Try { Await.result(this.exec(), Option(timeout).getOrElse(driver.timeout)) }
  }

  // Typed input

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    */
  trait TypedToStringHelperWithDataReadableTU[D <: Modifier, I] {
    /** The underlying resource that this implicit is wrapping */
    val resource: RestWithDataReadableTU[D, I] with RestResource

    /** Create an executable operation from the inherited resource
      * (typed input, untyped output)
      * @param body The typed body to apply on execution
      * @return An executable operation
      */
    def read(body: I): D with BaseDriverOp = EmptyBody[D with BaseDriverOp]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    */
  trait TypedToStringHelperWithDataReadableTT[D <: Modifier, I, O] {
    /** The underlying resource that this implicit is wrapping */
    val resource: RestWithDataReadableTT[D, I, O] with RestResource

    /** Create an executable operation from the inherited resource
      * (typed input, typed output)
      * @param body The typed body to apply on execution
      * @return An executable operation
      */
    def read(body: I): D with TypedDriverOp[O] = EmptyBody[D with TypedDriverOp[O]]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    */
  trait TypedToStringHelperWritableTU[D <: Modifier, I] {
    /** The underlying resource that this implicit is wrapping */
    val resource: RestWritableTU[D, I] with RestResource

    /** Create an executable operation from the inherited resource
      * (typed input, untyped output)
      * @param body The typed body to apply on execution
      * @return An executable operation
      */
    def write(body: I): D with BaseDriverOp = EmptyBody[D with BaseDriverOp]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    */
  trait TypedToStringHelperWritableTT[D <: Modifier, I, O] {
    /** The underlying resource that this implicit is wrapping */
    val resource: RestWritableTT[D, I, O] with RestResource

    /** Create an executable operation from the inherited resource
      * (typed input, typed output)
      * @param body The typed body to apply on execution
      * @return An executable operation
      */
    def write(body: I): D with TypedDriverOp[O] = EmptyBody[D with TypedDriverOp[O]]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    */
  trait TypedToStringHelperSendableTU[D <: Modifier, I] {
    /** The underlying resource that this implicit is wrapping */
    val resource: RestSendableTU[D, I] with RestResource

    /** Create an executable operation from the inherited resource
      * (typed input, untyped output)
      * @param body The typed body to apply on execution
      * @return An executable operation
      */
    def send(body: I): D with BaseDriverOp = EmptyBody[D with BaseDriverOp]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    */
  trait TypedToStringHelperSendableTT[D <: Modifier, I, O] {
    /** The underlying resource that this implicit is wrapping */
    val resource: RestSendableTT[D, I, O] with RestResource

    /** Create an executable operation from the inherited resource
      * (typed input, typed output)
      * @param body The typed body to apply on execution
      * @return An executable operation
      */
    def send(body: I): D with TypedDriverOp[O] = EmptyBody[D with TypedDriverOp[O]]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    */
  trait TypedToStringHelperWithDataDeletableTU[D <: Modifier, I] {
    /** The underlying resource that this implicit is wrapping */
    val resource: RestWithDataDeletableTU[D, I] with RestResource

    /** Create an executable operation from the inherited resource
      * (typed input, untyped output)
      * @param body The typed body to apply on execution
      * @return An executable operation
      */
    def delete(body: I): D with BaseDriverOp = EmptyBody[D with BaseDriverOp]
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    *
    * Note the implicit implementation needs to check for `CustomTypedToString` and handle that separately
    * (eg `if (ct.tpe <:< typeOf[CustomTypedToString])` then can simply do `t.fromTyped` in macro-land)
    *
    * One of these implicits is required for each possible operation-with-body
    */
  trait TypedToStringHelperWithDataDeletableTT[D <: Modifier, I, O] {
    /** The underlying resource that this implicit is wrapping */
    val resource: RestWithDataDeletableTT[D, I, O] with RestResource

    /** Create an executable operation from the inherited resource
      * (typed input, typed output)
      * @param body The typed body to apply on execution
      * @return An executable operation
      */
    def delete(body: I): D with TypedDriverOp[O] = EmptyBody[D with TypedDriverOp[O]]
  }

  /** Placeholder because macros can't override abstract methods */
  private val EmptyBody = null

  /** Placeholder because macros can't override abstract methods */
  private def EmptyBody[T] =null.asInstanceOf[T]
}
