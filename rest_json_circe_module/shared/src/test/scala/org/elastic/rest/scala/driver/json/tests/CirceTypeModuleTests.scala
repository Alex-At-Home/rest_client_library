package org.elastic.rest.scala.driver.json.tests

import io.circe._
import io.circe.parser.parse
import org.elastic.rest.scala.driver.RestBase
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseImplicits._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.utils.MockRestDriver
import org.elastic.rest.scala.driver.json.CirceJsonModule._
import org.elastic.rest.scala.driver.json.flexible_typing.CirceTypeModule._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** Register all concrete output types here, note has to be at the top of the file */
object ConcreteTypes {
  implicit val RegisterTestRead = new RegisterType[TestDataModel.TestRead] {}
  implicit val RegisterTestWrapperRead = new RegisterType[TestDataModel.TestWrapperRead] {}
}
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
      import ConcreteTypes._

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
      import ConcreteTypes._

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
  case class TestRead(testRead: String)
  case class TestWrite(testWrite: String)
}

/**Illustrates the case where sub-components are used to partition
  * the code
  */
trait TestDataModelComponent {
  case class OtherTestRead(testRead: String)
  case class OtherTestWrite(testWrite: String)

  case class TestWrapperWrite(s: String) extends CustomTypedToString {
    def fromTyped: String = s"""{"testWrite":"$s"}"""
  }
  case class TestWrapperRead(s: String) extends CustomStringToTyped
}

/** Sample API for testing CIRCE integration
  */
object TestApiTyped extends TestApiTypedExtensions {
  case class `/typed`()
    extends RestReadableT[Modifier, TestDataModel.TestRead]
      with RestWritableTU[Modifier, TestDataModel.TestWrite]
      with RestResource
}
trait TestApiTypedExtensions {
  case class `/data_model`()
    extends RestReadableT[Modifier, TestDataModel.OtherTestRead]
      with RestWritableTU[Modifier, TestDataModel.OtherTestWrite]
      with RestResource

  case class `/custom_typed`()
    extends RestReadableT[Modifier, TestDataModel.TestWrapperRead]
      with RestWritableTU[Modifier, TestDataModel.TestWrapperWrite]
      with RestResource
}
