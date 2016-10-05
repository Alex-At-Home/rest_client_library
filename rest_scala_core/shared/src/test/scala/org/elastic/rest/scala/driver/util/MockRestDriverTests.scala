package org.elastic.rest.scala.driver.util

import SampleResources._
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.RestBase.BaseDriverOp
import org.elastic.rest.scala.driver.utils.MockRestDriver
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object MockRestDriverTests extends TestSuite {

  val tests = this {
    "Basic mock functionality" - {

      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(`/$resource`(index: String), "GET", None, List(), List()) =>
          Future.successful(s"Received: $index")
      }
      val mockDriver = new MockRestDriver(handler)

      // Handler
      {
        val future = `/$resource`("/test").read().execS()(mockDriver)
        val retVal = Await.result(future, Duration("1 second"))
        retVal ==> "Received: /test"
      }
      // Unhandled:
      {
        val future = `/`().read().execS()(mockDriver)
          .recover { case e: RestServerException => s"${e.code}" }

        val retVal = Await.result(future, Duration("1 second"))
        retVal ==> "404"
      }
    }
  }
}
