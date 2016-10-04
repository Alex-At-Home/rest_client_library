package org.elastic.rest.scala.driver.json

import utest._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.jawn._
import io.circe._
import io.circe.generic.JsonCodec
import org.elastic.rest.scala.driver.RestBase
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.utils.MockRestDriver
import org.elastic.rest.scala.driver.json.CirceTypeModule._
import org.elastic.rest.scala.driver.json.CirceJsonModule._

import scala.concurrent.duration.Duration

object CirceModuleTests extends TestSuite {

  val tests = this {
    "Test JSON" - {
      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(TestApi.`/`(), RestBase.PUT,
          Some("""{"test":"write"}"""), List(), List()) =>
            Future.successful("""{ "test": "written" }""")
        case BaseDriverOp(TestApi.`/`(), RestBase.GET, _, List(), List()) =>
          Future.successful("""{ "test": "get" }""")
      }
      implicit val mockDriver = new MockRestDriver(handler)

      Await.result(TestApi.`/`().read().execJ(), Duration("1 second")) ==>
        parse("""{ "test": "get" }""").getOrElse(Json.Null)

      val json = parse("""{ "test": "write" }""").getOrElse(Json.Null)
      Await.result(TestApi.`/`().write(json).execJ(), Duration("1 second")) ==>
        parse("""{ "test": "written" }""").getOrElse(Json.Null)
    }
    "Test typed" - {
      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(TestApi.`/typed`(), RestBase.PUT,
          Some("""{"testWrite":"write"}"""), List(), List()) =>
            Future.successful("""{ "test": "written" }""")
        case BaseDriverOp(TestApi.`/typed`(), RestBase.GET, _, List(), List()) =>
          Future.successful("""{ "testRead": "get" }""")
      }
      implicit val mockDriver = new MockRestDriver(handler)

      Await.result(TestApi.`/typed`().read().exec(), Duration("1 second")) ==>
        TestDataModel.TestRead("get")

      Await.result(TestApi.`/typed`().write(TestDataModel.TestWrite("write")).execJ(),
        Duration("1 second")) ==>
          parse("""{ "test": "written" }""").getOrElse(Json.Null)
    }
    "Test typed extensions" - {  // (originally found classes inside a trait that the base model extends didn't work)

      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(TestApi.`/data_model`(), RestBase.PUT,
          Some("""{"testWrite":"write"}"""), List(), List()) =>
            Future.successful("""{ "test": "written" }""")
        case BaseDriverOp(TestApi.`/data_model`(), RestBase.GET, _, List(), List()) =>
          Future.successful("""{ "testRead": "get" }""")
      }
      implicit val mockDriver = new MockRestDriver(handler)

      TestApi.`/data_model`().read().result().get ==>
        TestDataModel.OtherTestRead("get")

      TestApi.`/data_model`().write(TestDataModel.OtherTestWrite("write")).resultJ(Duration("1 second")).get ==>
        parse("""{ "test": "written" }""").getOrElse(Json.Null)
    }
    "Test custom typed extensions" - {
      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(TestApi.`/custom_typed`(), RestBase.PUT,
        Some("""{"testWrite":"write"}"""), List(), List()) =>
          Future.successful("""{ "test": "written" }""")
        case BaseDriverOp(TestApi.`/custom_typed`(), RestBase.GET, _, List(), List()) =>
          Future.successful("""{ "testRead": "get" }""")
      }
      implicit val mockDriver = new MockRestDriver(handler)

      TestApi.`/custom_typed`().read().result().get ==> TestDataModel.TestWrapperRead("""{ "testRead": "get" }""")

      TestApi.`/custom_typed`().write(TestDataModel.TestWrapperWrite("write")).resultJ().get ==>
        parse("""{ "test": "written" }""").getOrElse(Json.Null)

    }
  }
}

/** Test object containing example data model for `TestApi`
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
object TestApi extends TestApiExtensions {
  case class `/`()
    extends RestReadable[BaseDriverOp]
    with RestWritable[BaseDriverOp]
    with RestResource

  case class `/typed`()
    extends RestReadableT[BaseDriverOp, TestDataModel.TestRead]
      with RestWritableTU[BaseDriverOp, TestDataModel.TestWrite]
      with RestResource
}
trait TestApiExtensions {
  case class `/data_model`()
    extends RestReadableT[BaseDriverOp, TestDataModel.OtherTestRead]
      with RestWritableTU[BaseDriverOp, TestDataModel.OtherTestWrite]
      with RestResource

  case class `/custom_typed`()
    extends RestReadableT[BaseDriverOp, TestDataModel.TestWrapperRead]
      with RestWritableTU[BaseDriverOp, TestDataModel.TestWrapperWrite]
      with RestResource
}
