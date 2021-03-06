package org.elastic.rest.scala.driver.json.tests

import java.util.concurrent.TimeUnit

import org.elastic.rest.scala.driver.RestBase
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseImplicits._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.utils.MockRestDriver
import org.elastic.rest.scala.driver.json.fixed_typing.CirceTypeModule._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

/** Register all concrete output types here, note has to be at the top of the file */
object JvmConcreteTypes {
  implicit val RegisterTestRead = new RegisterType[JvmTestDataModel.TestRead] {}
  implicit val RegisterTestWrapperRead = new RegisterType[JvmTestDataModel.TestWrapperRead] {}
}
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
      import JvmConcreteTypes._

      JvmTestApiTyped.`/typed`().read().result().get ==> JvmTestDataModel.TestRead("get")
    }
    "Test custom typed extensions (read)" - {
      implicit val mockDriver = new MockRestDriver(customHandler)
      val timeout = Duration(10, TimeUnit.SECONDS)
      import JvmConcreteTypes._

      JvmTestApiTyped.`/custom_typed`().read().result(timeout).get ==> JvmTestDataModel.TestWrapperRead(
        """{ "testRead": "get" }""")
    }
  }
}

/** Test object containing example data model for `TestApiTyped`
  * (sidenote: annotating `TestDataModel` doesn't make `TestDataModelComponent` visible)
  */
object JvmTestDataModel extends JvmTestDataModelComponent{
  case class TestRead(testRead: String)
}

/**Illustrates the case where sub-components are used to partition
  * the code
  */
trait JvmTestDataModelComponent {
  case class OtherTestRead(testRead: String)

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
      with RestResource
}
trait JvmTestApiTypedExtensions {
  case class `/data_model`()
    extends RestReadableT[Modifier, JvmTestDataModel.OtherTestRead]
      with RestResource

  case class `/custom_typed`()
    extends RestReadableT[Modifier, JvmTestDataModel.TestWrapperRead]
      with RestResource
}
