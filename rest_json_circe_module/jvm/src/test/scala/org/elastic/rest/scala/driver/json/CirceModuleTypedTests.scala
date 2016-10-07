package org.elastic.rest.scala.driver.json

import utest._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.parser.parse
import io.circe._
import io.circe.generic.JsonCodec
import org.elastic.rest.scala.driver.RestBase
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseTyped._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.utils.MockRestDriver
import org.elastic.rest.scala.driver.json.CirceTypeModule._
import org.elastic.rest.scala.driver.json.CirceJsonModule._

import scala.concurrent.duration.Duration

object CirceModuleTypedTests extends TestSuite {

  val tests = this {
    "Test macro version of typed (read)" - {
      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(TestApiTyped.`/typed`(), RestBase.GET, _, List(), List()) =>
          Future.successful("""{ "testRead": "get" }""")
        case x @ _ =>
          Future.failed(new Exception(s"Unexpected request: $x"))
      }
      implicit val mockDriver = new MockRestDriver(handler)

      TestApiTyped.`/typed`().read().testExec().map { result =>
        result ==>  TestDataModel.TestRead("get")
      }
    }
    "Test macro version of typed (write)" - {
      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(TestApiTyped.`/typed`(), RestBase.PUT,
        Some("""{"testWrite":"write"}"""), List(), List()) =>
          Future.successful("""{ "test": "written" }""")
        case x @ _ =>
          Future.failed(new Exception(s"Unexpected request: $x"))
      }
      implicit val mockDriver = new MockRestDriver(handler)

      TestApiTyped.`/typed`().testWrite(TestDataModel.TestWrite("write")).execJ().map { result =>
        result ==> parse("""{ "test": "written" }""").getOrElse(Json.Null)
      }
    }

    "Test typed" - {
      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(TestApiTyped.`/typed`(), RestBase.PUT,
          Some("""{"testWrite":"write"}"""), List(), List()) =>
            Future.successful("""{ "test": "written" }""")
        case BaseDriverOp(TestApiTyped.`/typed`(), RestBase.GET, _, List(), List()) =>
          Future.successful("""{ "testRead": "get" }""")
        case x @ _ =>
          Future.failed(new Exception(s"Unexpected request: $x"))
      }
      implicit val mockDriver = new MockRestDriver(handler)

      Await.result(TestApiTyped.`/typed`().read().exec(), Duration("1 second")) ==>
        TestDataModel.TestRead("get")

      Await.result(TestApiTyped.`/typed`().write(TestDataModel.TestWrite("write")).execJ(),
        Duration("1 second")) ==>
          parse("""{ "test": "written" }""").getOrElse(Json.Null)
    }
    "Test typed extensions" - {  // (originally found classes inside a trait that the base model extends didn't work)

      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(TestApiTyped.`/data_model`(), RestBase.PUT,
          Some("""{"testWrite":"write"}"""), List(), List()) =>
            Future.successful("""{ "test": "written" }""")
        case BaseDriverOp(TestApiTyped.`/data_model`(), RestBase.GET, _, List(), List()) =>
          Future.successful("""{ "testRead": "get" }""")
        case x @ _ =>
          Future.failed(new Exception(s"Unexpected request: $x"))
      }
      implicit val mockDriver = new MockRestDriver(handler)

      TestApiTyped.`/data_model`().read().result().get ==>
        TestDataModel.OtherTestRead("get")

      TestApiTyped.`/data_model`().write(TestDataModel.OtherTestWrite("write")).resultJ(Duration("1 second")).get ==>
        parse("""{ "test": "written" }""").getOrElse(Json.Null)
    }
    "Test custom typed extensions" - {
      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(TestApiTyped.`/custom_typed`(), RestBase.PUT,
        Some("""{"testWrite":"write"}"""), List(), List()) =>
          Future.successful("""{ "test": "written" }""")
        case BaseDriverOp(TestApiTyped.`/custom_typed`(), RestBase.GET, _, List(), List()) =>
          Future.successful("""{ "testRead": "get" }""")
        case x @ _ =>
          Future.failed(new Exception(s"Unexpected request: $x"))
      }
      implicit val mockDriver = new MockRestDriver(handler)

      TestApiTyped.`/custom_typed`().read().result().get ==> TestDataModel.TestWrapperRead("""{ "testRead": "get" }""")

      TestApiTyped.`/custom_typed`().write(TestDataModel.TestWrapperWrite("write")).resultJ().get ==>
        parse("""{ "test": "written" }""").getOrElse(Json.Null)

    }
  }
}

/** Test object containing example data model for `TestApiTyped`
  * (sidenote: annotating `TestDataModel` doesn't make `TestDataModelComponent` visible)
  */
object TestDataModel extends TestDataModelComponent{
  @JsonCodec case class TestRead(testRead: String)
  @JsonCodec case class TestWrite(testWrite: String)
}

/**Illustrates the case where sub-components are used to partition
  * the code
  */
trait TestDataModelComponent {
  @JsonCodec case class OtherTestRead(testRead: String)
  @JsonCodec case class OtherTestWrite(testWrite: String)

  case class TestWrapperWrite(s: String) extends CustomTypedToString {
    def fromTyped: String = s"""{"testWrite":"$s"}"""
  }
  case class TestWrapperRead(s: String) extends CustomStringToTyped
}

/** Sample API for testing CIRCE integration
  */
object TestApiTyped extends TestApiTypedExtensions {
  case class `/typed`()
    extends RestReadableT[BaseDriverOp, TestDataModel.TestRead]
      with RestWritableTU[BaseDriverOp, TestDataModel.TestWrite]
      with RestResource
}
trait TestApiTypedExtensions {
  case class `/data_model`()
    extends RestReadableT[BaseDriverOp, TestDataModel.OtherTestRead]
      with RestWritableTU[BaseDriverOp, TestDataModel.OtherTestWrite]
      with RestResource

  case class `/custom_typed`()
    extends RestReadableT[BaseDriverOp, TestDataModel.TestWrapperRead]
      with RestWritableTU[BaseDriverOp, TestDataModel.TestWrapperWrite]
      with RestResource
}
