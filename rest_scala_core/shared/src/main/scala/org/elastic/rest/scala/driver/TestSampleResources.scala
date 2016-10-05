package org.elastic.rest.scala.driver

import org.elastic.rest.scala.driver.RestBase.{TypedToStringHelper, _}
import org.elastic.rest.scala.driver.RestResources._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.runtime.universe

/**
  * Useful sample REST resources for testing
  */
object TestSampleResources {

  // Data Model

  /** Wraps the response to a typed test resource call */
  case class OutWrapper(s: String) extends CustomStringToTyped
  /** Wraps the request  of a typed test resource call */
  case class InWrapper(s: String) extends CustomTypedToString {
    override def fromTyped: String = s
  }

  /** Dummy JSON class */
  case class MockJson(s: String)
  /** JSON implicit, write */
  implicit val myStringToJsonHelper = new JsonToStringHelper[MockJson] {
    override def fromJson(j: MockJson): String = j match { case MockJson(s) => s }
  }
  /** JSON implicit, read */
  implicit class DummyStringToJsonHelper(op: BaseDriverOp) extends StringToJsonHelper[MockJson] {
    override def execJ()(implicit driver: RestDriver): Future[MockJson] = driver.exec(op).map(MockJson)
  }

  /** Dummy helper to convert `InWrapper` to string */
  implicit val myTypedToStringHelper = new  TypedToStringHelper {
    override def fromTyped[T](t: T)(implicit ct: universe.WeakTypeTag[T]): String =
      t.asInstanceOf[InWrapper].fromTyped
  }
  implicit val myStringToTypedHelper = new StringToTypedHelper {
    override def toType[T](s: String)(implicit ct: universe.WeakTypeTag[T]): T =
      OutWrapper(s).asInstanceOf[T]
  }

  // API Model

  /** No modifiers are supported for these parameters */
  trait NoParams extends BaseDriverOp

  /** Read-only root URI
    */
  case class `/`() extends RestResource
    with RestReadable[NoParams]
  {
    override lazy val location = "/"
  }

  /** Allows for generic access to the client - any URI string, any operation, and any modifier
    * (untyped)
    *
    * @param uri The resource name (including the leading '/')
    */
  case class `/$resource`(uri: String) extends RestResource
    with RestReadable[NoParams] with RestWithDataReadable[NoParams]
    with RestCheckable[NoParams]
    with RestSendable[NoParams] with RestNoDataSendable[NoParams]
    with RestWritable[NoParams] with RestNoDataWritable[NoParams]
    with RestDeletable[NoParams] with RestWithDataDeletable[NoParams]
  {
    override lazy val location = uri
  }

  /** Allows for generic access to the client - any URI string, any operation, and any modifier
    * (typed output)
    *
    * @param uri The resource name (including the leading '/')
    */
  case class `/$resource_ut`(uri: String) extends RestResource
    with RestReadableT[NoParams, OutWrapper] with RestWithDataReadableUT[NoParams, OutWrapper]
    with RestCheckableT[NoParams, OutWrapper]
    with RestSendableUT[NoParams, OutWrapper] with RestNoDataSendableT[NoParams, OutWrapper]
    with RestWritableUT[NoParams, OutWrapper] with RestNoDataWritableT[NoParams, OutWrapper]
    with RestDeletableT[NoParams, OutWrapper] with RestWithDataDeletableUT[NoParams, OutWrapper]
  {
    override lazy val location = uri
  }

  /** Allows for generic access to the client - any URI string, any operation, and any modifier
    * (typed input)
    *
    * @param uri The resource name (including the leading '/')
    */
  case class `/$resource_tu`(uri: String) extends RestResource
    with RestReadable[NoParams] with RestWithDataReadableTU[NoParams, InWrapper]
    with RestCheckable[NoParams]
    with RestSendableTU[NoParams, InWrapper] with RestNoDataSendable[NoParams]
    with RestWritableTU[NoParams, InWrapper] with RestNoDataWritable[NoParams]
    with RestDeletable[NoParams] with RestWithDataDeletableTU[NoParams, InWrapper]
  {
    override lazy val location = uri
  }

  /** Allows for generic access to the client - any URI string, any operation, and any modifier
    * (typed input and output)
    *
    * @param uri The resource name (including the leading '/')
    */
  case class `/$resource_tt`(uri: String) extends RestResource
    with RestReadableT[NoParams, OutWrapper] with RestWithDataReadableTT[NoParams, InWrapper, OutWrapper]
    with RestCheckableT[NoParams, OutWrapper]
    with RestSendableTT[NoParams, InWrapper, OutWrapper] with RestNoDataSendableT[NoParams, OutWrapper]
    with RestWritableTT[NoParams, InWrapper, OutWrapper] with RestNoDataWritableT[NoParams, OutWrapper]
    with RestDeletableT[NoParams, OutWrapper] with RestWithDataDeletableTT[NoParams, InWrapper, OutWrapper]
  {
    override lazy val location = uri
  }

}
import scala.scalajs.js.JSApp
import TestSampleResources._

object TutorialApp extends JSApp {
  def main(): Unit = {
    println(`/$resource`("/").check())
    //`/$resource_ut`("/").check()

    println("Hello world!")
  }
}
