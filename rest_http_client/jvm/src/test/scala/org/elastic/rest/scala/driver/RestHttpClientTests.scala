package org.elastic.rest.scala.driver

import java.util.concurrent.TimeUnit

import colossus.core.ServerContext
import colossus.protocols.http.HttpMethod.Get
import colossus.protocols.http.HttpService
import colossus.protocols.http.UrlParsing.{Root, on}
import colossus.service.Callback
import fr.hmil.roshttp.HttpRequest
import org.elastic.rest.scala.driver.utils.ServerUtils
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestResources._
import utest._
import org.elastic.rest.scala.driver.test_utils.SampleResources._

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

object RestHttpClientTests extends TestSuite {

  val tests = this {

    "Can read/write to/from an HTTP server" - {

      // Just handle the "/" resource
      class TestService(context: ServerContext) extends HttpService(context) {
        def handle = {
          case request@Get on Root if request.head.query.isEmpty =>
            val hasDefaultHeader = request.head.headers.firstValue("x-default").contains("test1")
            val hasRequestHeader = request.head.headers.firstValue("x-request").contains("test2")
            val basicAuth = request.head.headers.firstValue("Authorization").map(" " + _).getOrElse("")
            Callback.successful(request.ok(s"rx:/ $hasDefaultHeader $hasRequestHeader$basicAuth"))
          case request@Get on Root if request.head.query.isDefined =>
            Callback.successful(request.ok(s"rx:/${request.head.query.get}"))

          // (Don't handle unexpected requests - the server will convert those into 404s)
        }
      }
      val (server, port) = ServerUtils.createTestServer(new TestService(_))

      import fr.hmil.roshttp.Protocol.HTTP
      val baseRequest = HttpRequest().withProtocol(HTTP).withHost("localhost").withPort(port)

      val unauthed = RestHttpClient(baseRequest)
      val unauthed2 = RestHttpClient(baseRequest.withHeader("x-default", "test1"))
      val authed = RestHttpClient(baseRequest).withBasicAuth("user", "pass")

      // Check timeout mappings
      val testTimestampTransform = unauthed.changeSettings(r => r.withTimeout(FiniteDuration(10, TimeUnit.SECONDS)))
      testTimestampTransform.timeout.toMillis ==> FiniteDuration(10, TimeUnit.SECONDS).toMillis

      // Now try a bunch of requests

      // Basic check
      {
        val futureResult = unauthed.exec(`/$resource`("").read())
        val retVal = Await.result(futureResult, Duration("5 seconds"))
        retVal ==> "rx:/ false false"
      }
      // Basic check (default header)
      {
        val futureResult = unauthed2.exec(`/$resource`("").read())
        val retVal = Await.result(futureResult, Duration("5 seconds"))
        retVal ==> "rx:/ true false"
      }
      // custom headers
      {
        val futureResult = unauthed2.exec(
          `/$resource`("").read().h("x-request: test2"))
        val retVal = Await.result(futureResult, Duration("5 seconds"))
        retVal ==> "rx:/ true true"
      }
      // URL params
      {
        val futureResult = unauthed.exec(`/$resource`("").read().m("pretty", true))
        val retVal = Await.result(futureResult, Duration("5 seconds"))
        retVal ==> "rx:/pretty=true"
      }
      // Check basic auth:
      {
        val futureResult = authed.exec(`/$resource`("").read())
        val retVal = Await.result(futureResult, Duration("5 seconds"))
        retVal ==> "rx:/ false false Basic dXNlcjpwYXNz"
      }
    }
  }
}
