package org.elastic.rest.scala.driver

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Try
import scala.reflect.runtime.universe._
import RestBase._
import org.elastic.rest.scala.driver.RestResources._

/** Contains JVM specific implementation of typed support for the REST driver
  */
object RestBaseTyped {

  /**
    * TODO test code ... create an implicit class based on this?
    * @tparam T
    */
  trait StringToTypedImplicitBase[T] {
    val typedOp: TypedOperation[T]

    //TODO: can't override macros so need 2x compile units here or just tell people what to do...
    def testExec()(implicit driver: RestDriver, ec: ExecutionContext): Future[T] = null

    def testResult(timeout: Duration = null)(implicit driver: RestDriver, ec: ExecutionContext): Try[T] =
      Try { Await.result(this.testExec(), Option(timeout).getOrElse(driver.timeout)) }
  }

  trait TypedToStringImplicitBaseWithDataReadableTU[D <: BaseDriverOp, I]
  trait TypedToStringImplicitBaseDataReadableTT[D <: BaseDriverOp, I, O]
  trait TypedToStringImplicitBaseWritableTU[D <: BaseDriverOp, I] {
    val resource: RestWritableTU[D, I]
    def testWrite(body: I): D = null.asInstanceOf[D]
  }
  trait TypedToStringImplicitBaseWritableTT[D <: BaseDriverOp, I, O]
  trait TypedToStringImplicitBaseSendableTU[D <: BaseDriverOp, I]
  trait TypedToStringImplicitBaseSendableTT[D <: BaseDriverOp, I, O]
  trait TypedToStringImplicitBaseWithDataDeletableTU[D <: BaseDriverOp, I]
  trait TypedToStringImplicitBaseWithDataDeletableTT[D <: BaseDriverOp, I, O]

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
    def fromTyped[T](t: T)(implicit ct: WeakTypeTag[T]): String
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
      * @param ct The type tag associated with the type
      * @tparam T The desired type of the return operation
      * @return An object of type `T`
      */
    def toType[T](s: String)(implicit ct: WeakTypeTag[T]): T
  }
  /** A trait of `BaseDriverOp` that indicates the typed return type of an operation
    *
    * @tparam T The type of the operation return
    */
  trait TypedOperation[T] { self: BaseDriverOp =>

    /**
      * Evidence for the type of the operation
      */
    protected implicit val ct: WeakTypeTag[T]

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
    self.execS().map(stringToTypedHelper.toType(_)(ct))

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
