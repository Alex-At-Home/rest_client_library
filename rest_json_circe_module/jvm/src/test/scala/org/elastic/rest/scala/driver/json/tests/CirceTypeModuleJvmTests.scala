package org.elastic.rest.scala.driver.json.tests

import java.util.concurrent.TimeUnit

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
import scala.concurrent.duration.Duration

object CirceTypeModuleJvmTests extends TestSuite {

  val tests = this {

    val macroHandler: PartialFunction[BaseDriverOp, Future[String]] = {
      case BaseDriverOp(JvmTestApiTyped.`/typed`(), RestBase.GET, _, List(), List()) =>
        Future.successful("""{ "testRead": "get" }""")
      case x @ _ =>
        Future.failed(new Exception(s"Unexpected request: $x"))
    }

    val customHandler: PartialFunction[BaseDriverOp, Future[String]] = {
      case BaseDriverOp(JvmTestApiTyped.`/custom_typed`(), RestBase.GET, _, List(), List()) =>
        Future.successful("""{ "testRead": "get" }""")
      case x @ _ =>
        Future.failed(new Exception(s"Unexpected request: $x"))
    }

    "Test macro version of typed (read)" - {
      implicit val mockDriver = new MockRestDriver(macroHandler)

      JvmTestApiTyped.`/typed`().read().result().get ==>  JvmTestDataModel.TestRead("get")
    }
    "Test custom typed extensions (read)" - {
      implicit val mockDriver = new MockRestDriver(customHandler)
      val timeout = Duration(10, TimeUnit.SECONDS)

      JvmTestApiTyped.`/custom_typed`().read().result(timeout).get ==> JvmTestDataModel.TestWrapperRead(
        """{ "testRead": "get" }""")
    }
  }
}

/** Test object containing example data model for `TestApiTyped`
  * (sidenote: annotating `TestDataModel` doesn't make `TestDataModelComponent` visible)
  */
object JvmTestDataModel extends JvmTestDataModelComponent{
  @JsonCodec case class TestRead(testRead: String)
  @JsonCodec case class TestWrite(testWrite: String)
}

/**Illustrates the case where sub-components are used to partition
  * the code
  */
trait JvmTestDataModelComponent {
  @JsonCodec case class OtherTestRead(testRead: String)
  @JsonCodec case class OtherTestWrite(testWrite: String)

  case class TestWrapperWrite(s: String) extends CustomTypedToString {
    def fromTyped: String = s"""{"testWrite":"$s"}"""
  }
  case class TestWrapperRead(s: String) extends CustomStringToTyped
}

/** Sample API for testing CIRCE integration
  */
object JvmTestApiTyped extends JvmTestApiTypedExtensions {
  case class `/typed`()
    extends RestReadableT[Modifier, JvmTestDataModel.TestRead]
      with RestWritableTU[Modifier, JvmTestDataModel.TestWrite]
      with RestResource
}
trait JvmTestApiTypedExtensions {
  case class `/data_model`()
    extends RestReadableT[Modifier, JvmTestDataModel.OtherTestRead]
      with RestWritableTU[Modifier, JvmTestDataModel.OtherTestWrite]
      with RestResource

  case class `/custom_typed`()
    extends RestReadableT[Modifier, JvmTestDataModel.TestWrapperRead]
      with RestWritableTU[Modifier, JvmTestDataModel.TestWrapperWrite]
      with RestResource
}
