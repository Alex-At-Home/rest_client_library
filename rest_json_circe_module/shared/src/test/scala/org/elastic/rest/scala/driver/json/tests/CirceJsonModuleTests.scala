package org.elastic.rest.scala.driver.json.tests

import io.circe._
import io.circe.parser.parse
import org.elastic.rest.scala.driver.RestBase
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.utils.MockRestDriver
import org.elastic.rest.scala.driver.json.CirceJsonModule._
import utest._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CirceJsonModuleTests extends TestSuite {

  val tests = this {
    val handler: PartialFunction[BaseDriverOp, Future[String]] = {
      case BaseDriverOp(TestApiUntyped.`/`(), RestBase.PUT,
      Some("""{"test":"write"}"""), List(), List()) =>
        Future.successful("""{ "test": "written" }""")
      case BaseDriverOp(TestApiUntyped.`/`(), RestBase.GET, _, List(), List()) =>
        Future.successful("""{ "test": "get" }""")
      case x @ _ =>
        Future.failed(new Exception(s"Unexpected request: $x"))
    }
    implicit val mockDriver = new MockRestDriver(handler)
    "Test JSON - read" - {
      TestApiUntyped.`/`().read().execJ().map { result =>
        result ==> parse("""{ "test": "get" }""").getOrElse(Json.Null)
      }
    }
    "Test JSON - write" - {
      val json = parse("""{ "test": "write" }""").getOrElse(Json.Null)
      TestApiUntyped.`/`().writeJ(json).execJ().map { result =>
        result ==> parse("""{ "test": "written" }""").getOrElse(Json.Null)
      }
    }
  }
}

/** Sample API for testing CIRCE integration
  */
object TestApiUntyped {
  case class `/`()
    extends RestReadable[BaseDriverOp]
    with RestWritable[BaseDriverOp]
    with RestResource
}
