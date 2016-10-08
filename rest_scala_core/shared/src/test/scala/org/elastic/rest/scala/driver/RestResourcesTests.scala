package org.elastic.rest.scala.driver

import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.utils.NoJsonHelpers._
import org.elastic.rest.scala.driver.test_utils.SampleResources._
import utest._

object RestResourcesTests extends TestSuite {

  def formatter(op: BaseDriverOp): (String, Option[String]) = (op.op, op.body)

  val tests = this {
    "Test all resources, untyped" - {
      formatter(`/$resource`("/").check()) ==> ("HEAD", None)

      formatter(`/$resource`("/").read()) ==> ("GET", None)
      formatter(`/$resource`("/").readS("body")) ==> ("GET", Some("body"))
      formatter(`/$resource`("/").readJ(MockJson("body"))) ==> ("GET", Some("body"))

      formatter(`/$resource`("/").send()) ==> ("POST", None)
      formatter(`/$resource`("/").sendS("body")) ==> ("POST", Some("body"))
      formatter(`/$resource`("/").sendJ(MockJson("body"))) ==> ("POST", Some("body"))

      formatter(`/$resource`("/").write()) ==> ("PUT", None)
      formatter(`/$resource`("/").writeS("body")) ==> ("PUT", Some("body"))
      formatter(`/$resource`("/").writeJ(MockJson("body"))) ==> ("PUT", Some("body"))

      formatter(`/$resource`("/").delete()) ==> ("DELETE", None)
      formatter(`/$resource`("/").deleteS("body")) ==> ("DELETE", Some("body"))
      formatter(`/$resource`("/").deleteJ(MockJson("body"))) ==> ("DELETE", Some("body"))
    }
    "Test all resources, typed output" - {
      formatter(`/$resource_ut`("/").check()) ==> ("HEAD", None)

      formatter(`/$resource_ut`("/").read()) ==> ("GET", None)
      formatter(`/$resource_ut`("/").readS("body")) ==> ("GET", Some("body"))
      formatter(`/$resource_ut`("/").readJ(MockJson("body"))) ==> ("GET", Some("body"))

      formatter(`/$resource_ut`("/").send()) ==> ("POST", None)
      formatter(`/$resource_ut`("/").sendS("body")) ==> ("POST", Some("body"))
      formatter(`/$resource_ut`("/").sendJ(MockJson("body"))) ==> ("POST", Some("body"))

      formatter(`/$resource_ut`("/").write()) ==> ("PUT", None)
      formatter(`/$resource_ut`("/").writeS("body")) ==> ("PUT", Some("body"))
      formatter(`/$resource_ut`("/").writeJ(MockJson("body"))) ==> ("PUT", Some("body"))

      formatter(`/$resource_ut`("/").delete()) ==> ("DELETE", None)
      formatter(`/$resource_ut`("/").deleteS("body")) ==> ("DELETE", Some("body"))
      formatter(`/$resource_ut`("/").deleteJ(MockJson("body"))) ==> ("DELETE", Some("body"))
    }
    "Test all resources, typed input" - {
      formatter(`/$resource_tu`("/").check()) ==> ("HEAD", None)

      formatter(`/$resource_tu`("/").read()) ==> ("GET", None)
      formatter(`/$resource_tu`("/").readS("body")) ==> ("GET", Some("body"))
      formatter(`/$resource_tu`("/").readJ(MockJson("body"))) ==> ("GET", Some("body"))
      formatter(`/$resource_tu`("/").read(InWrapper("body"))) ==> ("GET", Some("body"))

      formatter(`/$resource_tu`("/").send()) ==> ("POST", None)
      formatter(`/$resource_tu`("/").sendS("body")) ==> ("POST", Some("body"))
      formatter(`/$resource_tu`("/").sendJ(MockJson("body"))) ==> ("POST", Some("body"))
      formatter(`/$resource_tu`("/").send(InWrapper("body"))) ==> ("POST", Some("body"))

      formatter(`/$resource_tu`("/").write()) ==> ("PUT", None)
      formatter(`/$resource_tu`("/").writeS("body")) ==> ("PUT", Some("body"))
      formatter(`/$resource_tu`("/").writeJ(MockJson("body"))) ==> ("PUT", Some("body"))
      formatter(`/$resource_tu`("/").write(InWrapper("body"))) ==> ("PUT", Some("body"))

      formatter(`/$resource_tu`("/").delete()) ==> ("DELETE", None)
      formatter(`/$resource_tu`("/").deleteS("body")) ==> ("DELETE", Some("body"))
      formatter(`/$resource_tu`("/").deleteJ(MockJson("body"))) ==> ("DELETE", Some("body"))
      formatter(`/$resource_tu`("/").delete(InWrapper("body"))) ==> ("DELETE", Some("body"))
    }
    "Test all resources, typed input and output" - {
      formatter(`/$resource_tt`("/").check()) ==> ("HEAD", None)

      formatter(`/$resource_tt`("/").read()) ==> ("GET", None)
      formatter(`/$resource_tt`("/").readS("body")) ==> ("GET", Some("body"))
      formatter(`/$resource_tt`("/").readJ(MockJson("body"))) ==> ("GET", Some("body"))
      formatter(`/$resource_tt`("/").read(InWrapper("body"))) ==> ("GET", Some("body"))

      formatter(`/$resource_tt`("/").send()) ==> ("POST", None)
      formatter(`/$resource_tt`("/").sendS("body")) ==> ("POST", Some("body"))
      formatter(`/$resource_tt`("/").sendJ(MockJson("body"))) ==> ("POST", Some("body"))
      formatter(`/$resource_tt`("/").send(InWrapper("body"))) ==> ("POST", Some("body"))

      formatter(`/$resource_tt`("/").write()) ==> ("PUT", None)
      formatter(`/$resource_tt`("/").writeS("body")) ==> ("PUT", Some("body"))
      formatter(`/$resource_tt`("/").writeJ(MockJson("body"))) ==> ("PUT", Some("body"))
      formatter(`/$resource_tt`("/").write(InWrapper("body"))) ==> ("PUT", Some("body"))

      formatter(`/$resource_tt`("/").delete()) ==> ("DELETE", None)
      formatter(`/$resource_tt`("/").deleteS("body")) ==> ("DELETE", Some("body"))
      formatter(`/$resource_tt`("/").deleteJ(MockJson("body"))) ==> ("DELETE", Some("body"))
      formatter(`/$resource_tt`("/").delete(InWrapper("body"))) ==> ("DELETE", Some("body"))
    }
  }
}
