package org.elastic.rest.scala.driver

import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseRuntimeTyped._
import org.elastic.rest.scala.driver.test_utils.SampleResources._
import org.elastic.rest.scala.driver.test_utils.SampleResourcesTyped._
import org.elastic.rest.scala.driver.utils.MockRestDriver
import utest._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object RestBaseAsyncTests extends TestSuite {

  val tests = this {

    "Check JSON serialization" - {
      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(`/$resource`(index), "GET", None, List(), List()) =>
          Future.successful(s"""{"index":"$index"}""")
      }
      implicit val mockDriver = new MockRestDriver(handler)

      val res = Await.result(`/$resource`("test").read().execJ(), Duration("1 second"))
      res ==> MockJson("""{"index":"test"}""")
    }
    "Check JSON de-serialization" - {
      val handler: PartialFunction[BaseDriverOp, Future[String]] = {
        case BaseDriverOp(`/$resource`(index), method @ _, Some(s @ _), List(), List()) =>
          Future.successful(s"$method: $s")
        case BaseDriverOp(`/$resource`(index), method @ _, None, List(), List()) =>
          Future.successful(s"$method")

        case BaseDriverOp(`/$resource_ut`(index), method @ _, None, List(), List()) =>
          Future.successful(s"$method")
        case BaseDriverOp(`/$resource_ut`(index), method @ _, Some(s @ _), List(), List()) =>
          Future.successful(s"$method: $s")

        case BaseDriverOp(`/$resource_tu`(index), method @ _, Some(s @ _), List(), List()) =>
          Future.successful(s"$method: $s")

        case BaseDriverOp(`/$resource_tt`(index), method @ _, Some(s @ _), List(), List()) =>
          Future.successful(s"$method: $s")
      }
      implicit val mockDriver = new MockRestDriver(handler)

      // Coverage test for BaseDriverOps and related (resultS / execS / resultJ / execJ / result / exec)

      // Read with data
      {
        val expected = "GET: test"
        val res_ss = `/$resource`("test").read("test").resultS(Duration("1 second")).get
        res_ss ==> expected
        val res_js = `/$resource`("test").read(MockJson("test")).resultS(Duration("1 second")).get
        res_js ==> expected
        val res_sj = Await.result(`/$resource`("test").read("test").execS(), Duration("1 second"))
        res_sj ==> expected
        val res_jj = Await.result(`/$resource`("test").read(MockJson("test")).execJ(), Duration("1 second"))
        res_jj.s ==> expected
        val res_st = `/$resource_ut`("test").read("test").result(Duration("1 second")).get
        res_st.s ==> expected
        val res_jt = Await.result(`/$resource_ut`("test").read(MockJson("test")).exec(), Duration("1 second"))
        res_jt.s ==> expected
        val res_ts = `/$resource_tt`("test").read(InWrapper("test")).resultS(Duration("1 second")).get
        res_ts ==> expected
        val res_tj = `/$resource_tt`("test").read(InWrapper("test")).resultJ(Duration("1 second")).get
        res_tj.s ==> expected
        val res_tt = `/$resource_tt`("test").read(InWrapper("test")).result(Duration("1 second")).get
        res_tt.s ==> expected
      }
      // Send
      {
        val expected = "POST: test"
        val res_ss = `/$resource`("test").send("test").resultS(Duration("1 second")).get
        res_ss ==> expected
        val res_js = `/$resource`("test").send(MockJson("test")).resultS(Duration("1 second")).get
        res_js ==> expected
        val res_sj = Await.result(`/$resource`("test").send("test").execS(), Duration("1 second"))
        res_sj ==> expected
        val res_jj = Await.result(`/$resource`("test").send(MockJson("test")).execJ(), Duration("1 second"))
        res_jj.s ==> expected
        val res_st = `/$resource_ut`("test").send("test").result(Duration("1 second")).get
        res_st.s ==> expected
        val res_jt = Await.result(`/$resource_ut`("test").send(MockJson("test")).exec(), Duration("1 second"))
        res_jt.s ==> expected
        val res_ts = `/$resource_tt`("test").send(InWrapper("test")).resultS(Duration("1 second")).get
        res_ts ==> expected
        val res_tj = `/$resource_tt`("test").send(InWrapper("test")).resultJ(Duration("1 second")).get
        res_tj.s ==> expected
        val res_tt = `/$resource_tt`("test").send(InWrapper("test")).result(Duration("1 second")).get
        res_tt.s ==> expected
      }
      // Write
      {
        val expected = "PUT: test"
        val res_ss = `/$resource`("test").write("test").resultS(Duration("1 second")).get
        res_ss ==> expected
        val res_js = `/$resource`("test").write(MockJson("test")).resultS(Duration("1 second")).get
        res_js ==> expected
        val res_sj = Await.result(`/$resource`("test").write("test").execS(), Duration("1 second"))
        res_sj ==> expected
        val res_jj = Await.result(`/$resource`("test").write(MockJson("test")).execJ(), Duration("1 second"))
        res_jj.s ==> expected
        val res_st = `/$resource_ut`("test").write("test").result(Duration("1 second")).get
        res_st.s ==> expected
        val res_jt = Await.result(`/$resource_ut`("test").write(MockJson("test")).exec(), Duration("1 second"))
        res_jt.s ==> expected
        val res_ts = `/$resource_tt`("test").write(InWrapper("test")).resultS(Duration("1 second")).get
        res_ts ==> expected
        val res_tj = `/$resource_tt`("test").write(InWrapper("test")).resultJ(Duration("1 second")).get
        res_tj.s ==> expected
        val res_tt = `/$resource_tt`("test").write(InWrapper("test")).result(Duration("1 second")).get
        res_tt.s ==> expected
      }
      // Delete with data
      {
        val expected = "DELETE: test"
        val res_ss = `/$resource`("test").delete("test").resultS(Duration("1 second")).get
        res_ss ==> expected
        val res_js = `/$resource`("test").delete(MockJson("test")).resultS(Duration("1 second")).get
        res_js ==> expected
        val res_sj = Await.result(`/$resource`("test").delete("test").execS(), Duration("1 second"))
        res_sj ==> expected
        val res_jj = Await.result(`/$resource`("test").delete(MockJson("test")).execJ(), Duration("1 second"))
        res_jj.s ==> expected
        val res_st = `/$resource_ut`("test").delete("test").result(Duration("1 second")).get
        res_st.s ==> expected
        val res_jt = Await.result(`/$resource_ut`("test").delete(MockJson("test")).exec(), Duration("1 second"))
        res_jt.s ==> expected
        val res_ts = `/$resource_tt`("test").delete(InWrapper("test")).resultS(Duration("1 second")).get
        res_ts ==> expected
        val res_tj = `/$resource_tt`("test").delete(InWrapper("test")).resultJ(Duration("1 second")).get
        res_tj.s ==> expected
        val res_tt = `/$resource_tt`("test").delete(InWrapper("test")).result(Duration("1 second")).get
        res_tt.s ==> expected
      }
      // (Check the operations without data while we're here)
      // Check
      {
        val expected = "HEAD"
        val res_s = Await.result(`/$resource`("test").check().execS(), Duration("1 second"))
        val res_j = Await.result(`/$resource`("test").check().execJ(), Duration("1 second"))
        val res_t = Await.result(`/$resource_ut`("test").check().exec(), Duration("1 second"))
        res_s ==> expected
        res_j.s ==> expected
        res_t.s ==> expected
      }
      // Read
      {
        val expected = "GET"
        val res_s = Await.result(`/$resource`("test").read().execS(), Duration("1 second"))
        val res_j = Await.result(`/$resource`("test").read().execJ(), Duration("1 second"))
        val res_t = Await.result(`/$resource_ut`("test").read().exec(), Duration("1 second"))
        res_s ==> expected
        res_j.s ==> expected
        res_t.s ==> expected
      }
      // Send
      {
        val expected = "POST"
        val res_s = Await.result(`/$resource`("test").send().execS(), Duration("1 second"))
        val res_j = Await.result(`/$resource`("test").send().execJ(), Duration("1 second"))
        val res_t = Await.result(`/$resource_ut`("test").send().exec(), Duration("1 second"))
        res_s ==> expected
        res_j.s ==> expected
        res_t.s ==> expected
      }
      // Write
      {
        val expected = "PUT"
        val res_s = Await.result(`/$resource`("test").write().execS(), Duration("1 second"))
        val res_j = Await.result(`/$resource`("test").write().execJ(), Duration("1 second"))
        val res_t = Await.result(`/$resource_ut`("test").write().exec(), Duration("1 second"))
        res_s ==> expected
        res_j.s ==> expected
        res_t.s ==> expected
      }
      // Delete
      {
        val expected = "DELETE"
        val res_s = Await.result(`/$resource`("test").delete().execS(), Duration("1 second"))
        val res_j = Await.result(`/$resource`("test").delete().execJ(), Duration("1 second"))
        val res_t = Await.result(`/$resource_ut`("test").delete().exec(), Duration("1 second"))
        res_s ==> expected
        res_j.s ==> expected
        res_t.s ==> expected
      }
    }
  }
}
