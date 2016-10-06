package org.elastic.rest.scala.driver

import org.elastic.rest.scala.driver.RestBase.{BaseDriverOp, RestDriver}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Try
import scala.language.experimental.macros

/** Contains JS specific implementation of typed support for the REST driver
  */
object RestBaseTyped {

  /** A trait to be implemented and used as an implicit to define how to go from a typed object
    * (eg case class) to a string, normally via JSON unless derived from `CustomTypedToString`
    * To handle `CustomTypedToString`, `fromTyped` should check for `T` being an instance of that
    * and handle it separately
    */
  trait TypedToStringHelper {
    /** Helper to convert from a typed (Case class) object to a string, see above
      * remarks about handling `T` that is inherited from `CustomTypedToString`
      *
      * @param t Typed object
      * @tparam T The type
      * @return The JSON string representing `t`
      */
    def fromTyped[T](t: T): String
  }

  /** A trait to be implemented and used as an implicit to indicate how to go from a
    * JSON string (ie a return from an operation) to a typed (case class) object
    * Note that the overridden `toType` should check `ct.tpe <:< CustomStringToTyped` and
    * simply return `x.asInstanceOf[CustomStringToTyped].toType(s)` in such cases
    */
  trait StringToTypedHelper {

    /** Helper to convert from a JSON string to a typed (case class) object
      *
      * @param s String return
      * @tparam T The desired type of the return operation
      * @return An object of type `T`
      */
    def toType[T](s: String): T
  }
  /** A trait of `BaseDriverOp` that indicates the typed return type of an operation
    *
    * @tparam T The type of the operation return
    */
  trait TypedOperation[T] { self: BaseDriverOp =>

    /** Actually executes the operation (aysnc)
      *
      * @param stringToTypedHelper An implicit helper to convert the op return to a type
      * @param driver The driver which executes the operation
      * @param ec The execution context for futures
      * @return A future containing the result of the operation as a type
      */
    def exec
    ()
    (implicit stringToTypedHelper: StringToTypedHelper,
     driver: RestDriver,
     ec: ExecutionContext
    )
    : Future[T] =
    self.execS().map(stringToTypedHelper.toType)

    /** Actually executes the operation (sync)
      *
      * @param timeout Optionally, the amount of time to wait before failing
      * @param stringToTypedHelper An implicit helper to convert the op return to a type
      * @param driver The driver which executes the operation
      * @param ec The execution context for futures
      * @return The result of the operation as a type
      */
    def result
    (timeout: Duration = null)
    (implicit stringToTypedHelper: StringToTypedHelper,
     driver: RestDriver,
     ec: ExecutionContext)
    : Try[T] =
    Try { Await.result(this.exec(), Option(timeout).getOrElse(driver.timeout)) }

  }
}
