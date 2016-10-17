package org.elastic.rest.scala.driver.utils

import org.elastic.rest.scala.driver.test_utils.SampleResources._
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.RestBase.BaseDriverOp
import org.elastic.rest.scala.driver.utils.MockRestDriver
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MockRestDriverTests extends TestSuite {

  val tests = this {
    val handler: PartialFunction[BaseDriverOp, Future[String]] = {
      case BaseDriverOp(`/$resource`(index: String), "GET", None, List(), List()) =>
        Future.successful(s"Received: $index")
    }
    val mockDriver = new MockRestDriver(handler)

    "Basic mock functionality - handled" - {
        val future = `/$resource`("/test").read().execS()(mockDriver)
        future.map { retVal =>
          retVal ==> "Received: /test"
        }
      }
    "Basic mock functionality - unhandled" - {
        val future = `/`().read().execS()(mockDriver)
          .recover { case e: RestServerException => s"${e.code}" }

      future.map { retVal =>
        retVal ==> "404"
      }
    }
  }
}
