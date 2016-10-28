package org.elastic.rest.scala.driver.json.tests

import io.circe._
import io.circe.generic.JsonCodec
import io.circe.parser.parse
import org.elastic.rest.scala.driver.RestBase
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseRuntimeTyped._
import org.elastic.rest.scala.driver.RestBaseImplicits._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.RestResourcesRuntimeTyped._
import org.elastic.rest.scala.driver.json.CirceRuntimeTypeModule._
import org.elastic.rest.scala.driver.json.CirceJsonModule._
import org.elastic.rest.scala.driver.utils.MockRestDriver
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object CirceRuntimeTypeModuleTests extends TestSuite {

  val tests = this {

    "Test typed" - {
      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(TestApiRuntimeTyped.`/typed`(), RestBase.PUT,
          Some("""{"testWrite":"write"}"""), List(), List()) =>
            Future.successful("""{ "test": "written" }""")
        case BaseDriverOp(TestApiRuntimeTyped.`/typed`(), RestBase.GET, _, List(), List()) =>
          Future.successful("""{ "testRead": "get" }""")
        case x @ _ =>
          Future.failed(new Exception(s"Unexpected request: $x"))
      }
      implicit val mockDriver = new MockRestDriver(handler)

      Await.result(TestApiRuntimeTyped.`/typed`().read().exec(), Duration("1 second")) ==>
        TestRuntimeDataModel.TestRead("get")

      Await.result(TestApiRuntimeTyped.`/typed`().write(TestRuntimeDataModel.TestWrite("write")).execJ(),
        Duration("1 second")) ==>
          parse("""{ "test": "written" }""").getOrElse(Json.Null)
    }
    "Test typed extensions" - {  // (originally found classes inside a trait that the base model extends didn't work)

      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(TestApiRuntimeTyped.`/data_model`(), RestBase.PUT,
          Some("""{"testWrite":"write"}"""), List(), List()) =>
            Future.successful("""{ "test": "written" }""")
        case BaseDriverOp(TestApiRuntimeTyped.`/data_model`(), RestBase.GET, _, List(), List()) =>
          Future.successful("""{ "testRead": "get" }""")
        case x @ _ =>
          Future.failed(new Exception(s"Unexpected request: $x"))
      }
      implicit val mockDriver = new MockRestDriver(handler)

      TestApiRuntimeTyped.`/data_model`().read().result().get ==>
        TestRuntimeDataModel.OtherTestRead("get")

      TestApiRuntimeTyped.`/data_model`()
        .write(TestRuntimeDataModel.OtherTestWrite("write")).resultJ(Duration("1 second")).get ==>
        parse("""{ "test": "written" }""").getOrElse(Json.Null)
    }
    "Test custom typed extensions" - {
      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(TestApiRuntimeTyped.`/custom_typed`(), RestBase.PUT,
        Some("""{"testWrite":"write"}"""), List(), List()) =>
          Future.successful("""{ "test": "written" }""")
        case BaseDriverOp(TestApiRuntimeTyped.`/custom_typed`(), RestBase.GET, _, List(), List()) =>
          Future.successful("""{ "testRead": "get" }""")
        case x @ _ =>
          Future.failed(new Exception(s"Unexpected request: $x"))
      }
      implicit val mockDriver = new MockRestDriver(handler)

      TestApiRuntimeTyped.`/custom_typed`()
        .read().result().get ==> TestRuntimeDataModel.TestWrapperRead("""{ "testRead": "get" }""")

      TestApiRuntimeTyped.`/custom_typed`().write(TestRuntimeDataModel.TestWrapperWrite("write")).resultJ().get ==>
        parse("""{ "test": "written" }""").getOrElse(Json.Null)

    }
  }
}

/** Test object containing example data model for `TestApiRuntimeTyped`
  * Note the `@JsonCodec`s are needed in the runtime version, unlike in the macro version
  * (sidenote: annotating `TestRuntimeDataModel` doesn't make `TestRuntimeDataModelComponent` visible)
  */
object TestRuntimeDataModel extends TestRuntimeDataModelComponent{
  @JsonCodec case class TestRead(testRead: String)
  @JsonCodec case class TestWrite(testWrite: String)
}

/**Illustrates the case where sub-components are used to partition
  * the code
  * Note the `@JsonCodec`s are needed in the runtime version, unlike in the macro version
  */
trait TestRuntimeDataModelComponent {
  @JsonCodec case class OtherTestRead(testRead: String)
  @JsonCodec case class OtherTestWrite(testWrite: String)

  case class TestWrapperWrite(s: String) extends CustomTypedToString {
    def fromTyped: String = s"""{"testWrite":"$s"}"""
  }
  case class TestWrapperRead(s: String) extends CustomStringToTyped
}

/** Sample API for testing CIRCE integration
  */
object TestApiRuntimeTyped extends TestApiRuntimeTypedExtensions {
  case class `/typed`()
    extends RestReadableT[Modifier, TestRuntimeDataModel.TestRead]
      with RestWritableTU[Modifier, TestRuntimeDataModel.TestWrite]
      with RestResource
}
trait TestApiRuntimeTypedExtensions {
  case class `/data_model`()
    extends RestReadableT[Modifier, TestRuntimeDataModel.OtherTestRead]
      with RestWritableTU[Modifier, TestRuntimeDataModel.OtherTestWrite]
      with RestResource

  case class `/custom_typed`()
    extends RestReadableT[Modifier, TestRuntimeDataModel.TestWrapperRead]
      with RestWritableTU[Modifier, TestRuntimeDataModel.TestWrapperWrite]
      with RestResource
}
