package org.elastic.rest.scala.driver

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Try
import scala.reflect.runtime.universe._
import RestBase._

/** Contains JVM specific implementation of typed support for the REST driver
  */
object RestBaseRuntimeTyped {

  /** A trait to be implemented and used as an implicit to define how to go from a typed object
    * (eg case class) to a string, normally via JSON unless derived from `CustomTypedToString`
    * To handle `CustomTypedToString`, `fromTyped` should check for `T` being an instance of that
    * and handle it separately
    *
    * This is the runtime version - it is recommended to use `TypedToStringHelper` unless there
    * is a good reason not to
    */
  trait RuntimeTypedToStringHelper {
    /** Helper to convert from a typed (Case class) object to a string, see above
      * remarks about handling `T` that is inherited from `CustomTypedToString`
      *
      * @param t Typed object
      * @tparam T The type
      * @return The JSON string representing `t`
      */
    def fromTyped[T](t: T)(implicit ct: WeakTypeTag[T]): String
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    * Note that the overridden `toType` should check `ct.tpe <:< CustomStringToTyped` and
    * simply return `x.asInstanceOf[CustomStringToTyped].toType(s)` in such cases
    *
    * This is the runtime version - it is recommended to use `StringToTypedHelper` unless there
    * is a good reason not to
    */
  trait RuntimeStringToTypedHelper {

    /** Helper to convert from a JSON string to a typed (case class) object
      *
      * @param s String return
      * @param ct The type tag associated with the type
      * @tparam T The desired type of the return operation
      * @return An object of type `T`
      */
    def toType[T](s: String)(implicit ct: WeakTypeTag[T]): T
  }

  /** Decorates a TypedOperation[T] with `exec` and `result` methods to execute the operation (async or sync)
    * (which is a trait of `BaseDriverOp` that indicates the typed return type of an operation)
    *
    * This is the runtime version - it is recommended to use `StringToTypedHelper` unless there
    * is a good reason not to
    *
    * @tparam T The type of the operation return
    */
  implicit class RuntimeTypedOperation[T](typedOp: TypedDriverOp[T] with BaseDriverOp) {

    /** Actually executes the operation (aysnc)
      *
      * This version uses the runtime implicits (JVM only and it is recommended to use the macro implicits
      * derived from `StringToTypedHelper` where possible)
      *
      * @param stringToTypedHelper An implicit helper to convert the op return to a type
      * @param driver The driver which executes the operation
      * @param ec The execution context for futures
      * @param ct The classtag for the output type
      * @return A future containing the result of the operation as a type
      */
    def exec
    ()
    (implicit stringToTypedHelper: RuntimeStringToTypedHelper,
     driver: RestDriver,
     ec: ExecutionContext,
     ct: WeakTypeTag[T])
    : Future[T] =
    typedOp.execS().map(stringToTypedHelper.toType(_)(ct))

    /** Actually executes the operation (sync)
      *
      * This version uses the runtime implicits (JVM only and it is recommended to use the macro implicits
      * derived from `StringToTypedHelper` where possible)
      *
      * @param timeout Optionally, the amount of time to wait before failing
      * @param stringToTypedHelper An implicit helper to convert the op return to a type
      * @param driver The driver which executes the operation
      * @param ec The execution context for futures
      * @param ct The classtag for the output type
      * @return The result of the operation as a type
      */
    def result
    (timeout: Duration = null)
    (implicit stringToTypedHelper: RuntimeStringToTypedHelper,
     driver: RestDriver,
     ec: ExecutionContext,
     ct: WeakTypeTag[T])
    : Try[T] =
    Try { Await.result(this.exec(), Option(timeout).getOrElse(driver.timeout)) }
  }
}
