package org.elastic.rest.scala.driver.json.tests

import io.circe._
import io.circe.generic.JsonCodec
import io.circe.parser.parse
import org.elastic.rest.scala.driver.RestBase
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseImplicits._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.utils.MockRestDriver
import org.elastic.rest.scala.driver.json.CirceJsonModule._
import org.elastic.rest.scala.driver.json.CirceTypeModule._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CirceTypeModuleTests extends TestSuite {

  val tests = this {

    val macroHandler: PartialFunction[BaseDriverOp, Future[String]] = {
      case BaseDriverOp(TestApiTyped.`/typed`(), RestBase.PUT,
        Some("""{"testWrite":"write"}"""), List(), List()) =>
        Future.successful("""{ "test": "written" }""")
      case BaseDriverOp(TestApiTyped.`/typed`(), RestBase.GET, _, List(), List()) =>
        Future.successful("""{ "testRead": "get" }""")
      case x @ _ =>
        Future.failed(new Exception(s"Unexpected request: $x"))
    }

    val customHandler: PartialFunction[BaseDriverOp, Future[String]] = {
      case BaseDriverOp(TestApiTyped.`/custom_typed`(), RestBase.PUT,
        Some("""{"testWrite":"write"}"""), List(), List()) =>
        Future.successful("""{ "test": "written" }""")
      case BaseDriverOp(TestApiTyped.`/custom_typed`(), RestBase.GET, _, List(), List()) =>
        Future.successful("""{ "testRead": "get" }""")
      case x @ _ =>
        Future.failed(new Exception(s"Unexpected request: $x"))
    }

    "Test macro version of typed (read)" - {
      implicit val mockDriver = new MockRestDriver(macroHandler)

      TestApiTyped.`/typed`().read().exec().map { result =>
        result ==>  TestDataModel.TestRead("get")
      }
    }
    "Test macro version of typed (write)" - {
      implicit val mockDriver = new MockRestDriver(macroHandler)

      TestApiTyped.`/typed`().write(TestDataModel.TestWrite("write")).execJ().map { result =>
        result ==> parse("""{ "test": "written" }""").getOrElse(Json.Null)
      }
    }
    "Test custom typed extensions (read)" - {
      implicit val mockDriver = new MockRestDriver(customHandler)

      TestApiTyped.`/custom_typed`().read().exec().map { result =>
        result ==> TestDataModel.TestWrapperRead("""{ "testRead": "get" }""")
      }
    }
    "Test custom typed extensions (write)" - {
      implicit val mockDriver = new MockRestDriver(customHandler)

      TestApiTyped.`/custom_typed`().write(TestDataModel.TestWrapperWrite("write")).execJ().map { result =>
        result ==> parse("""{ "test": "written" }""").getOrElse(Json.Null)
      }
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
