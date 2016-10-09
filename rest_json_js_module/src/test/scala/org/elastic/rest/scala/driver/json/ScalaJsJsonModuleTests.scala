package org.elastic.rest.scala.driver.json

import org.elastic.rest.scala.driver.RestBase
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestResources.{RestReadable, RestWritable}
import org.elastic.rest.scala.driver.utils.MockRestDriver
import org.elastic.rest.scala.driver.json.ScalaJsJsonModule._
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

object ScalaJsJsonModuleTests extends TestSuite {

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
      TestApiUntyped.`/`().read().execJ().map(js.JSON.stringify(_)).map { result =>
        result ==> """{"test":"get"}"""
      }
    }
    "Test JSON - write" - {
      val json = js.JSON.parse("""{ "test": "write" }""")
      TestApiUntyped.`/`().writeJ(json).execJ().map(js.JSON.stringify(_)).map { result =>
        result ==> """{"test":"written"}"""
      }
    }
  }
}
/** Sample API for testing JS integration
  */
object TestApiUntyped {
  case class `/`()
    extends RestReadable[BaseDriverOp]
      with RestWritable[BaseDriverOp]
      with RestResource
}


