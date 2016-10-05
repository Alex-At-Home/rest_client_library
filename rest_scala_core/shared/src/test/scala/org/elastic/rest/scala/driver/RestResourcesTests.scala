package org.elastic.rest.scala.driver

import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.util.SampleResources.OutWrapper
import utest._
import scala.reflect.runtime.universe

object RestResourcesTests extends TestSuite {

  def formatter(op: BaseDriverOp): (String, Option[String]) = (op.op, op.body)

  def checkOutputType[T](op: TypedOperation[T])(implicit ct: universe.WeakTypeTag[T]): Boolean =
    ct.tpe =:= universe.typeOf[OutWrapper]

  import util.SampleResources._

  val tests = this {
    "Test all resources, untyped" - {
      formatter(`/$resource`("/").check()) ==> ("HEAD", None)

      formatter(`/$resource`("/").read()) ==> ("GET", None)
      formatter(`/$resource`("/").read("body")) ==> ("GET", Some("body"))
      formatter(`/$resource`("/").read(MockJson("body"))) ==> ("GET", Some("body"))

      formatter(`/$resource`("/").send()) ==> ("POST", None)
      formatter(`/$resource`("/").send("body")) ==> ("POST", Some("body"))
      formatter(`/$resource`("/").send(MockJson("body"))) ==> ("POST", Some("body"))

      formatter(`/$resource`("/").write()) ==> ("PUT", None)
      formatter(`/$resource`("/").write("body")) ==> ("PUT", Some("body"))
      formatter(`/$resource`("/").write(MockJson("body"))) ==> ("PUT", Some("body"))

      formatter(`/$resource`("/").delete()) ==> ("DELETE", None)
      formatter(`/$resource`("/").delete("body")) ==> ("DELETE", Some("body"))
      formatter(`/$resource`("/").delete(MockJson("body"))) ==> ("DELETE", Some("body"))
    }
    "Test all resources, typed output" - {
      formatter(`/$resource_ut`("/").check()) ==> ("HEAD", None)

      formatter(`/$resource_ut`("/").read()) ==> ("GET", None)
      formatter(`/$resource_ut`("/").read("body")) ==> ("GET", Some("body"))
      formatter(`/$resource_ut`("/").read(MockJson("body"))) ==> ("GET", Some("body"))

      formatter(`/$resource_ut`("/").send()) ==> ("POST", None)
      formatter(`/$resource_ut`("/").send("body")) ==> ("POST", Some("body"))
      formatter(`/$resource_ut`("/").send(MockJson("body"))) ==> ("POST", Some("body"))

      formatter(`/$resource_ut`("/").write()) ==> ("PUT", None)
      formatter(`/$resource_ut`("/").write("body")) ==> ("PUT", Some("body"))
      formatter(`/$resource_ut`("/").write(MockJson("body"))) ==> ("PUT", Some("body"))

      formatter(`/$resource_ut`("/").delete()) ==> ("DELETE", None)
      formatter(`/$resource_ut`("/").delete("body")) ==> ("DELETE", Some("body"))
      formatter(`/$resource_ut`("/").delete(MockJson("body"))) ==> ("DELETE", Some("body"))
    }
    "Test all resources, typed output (output type)" - {
      // Output types:
      checkOutputType(`/$resource_ut`("/").check()) ==> true

      checkOutputType(`/$resource_ut`("/").read()) ==> true
      checkOutputType(`/$resource_ut`("/").read("body")) ==> true
      checkOutputType(`/$resource_ut`("/").read(MockJson("body"))) ==> true

      checkOutputType(`/$resource_ut`("/").send()) ==> true
      checkOutputType(`/$resource_ut`("/").send("body")) ==> true
      checkOutputType(`/$resource_ut`("/").send(MockJson("body"))) ==> true

      checkOutputType(`/$resource_ut`("/").write()) ==> true
      checkOutputType(`/$resource_ut`("/").write("body")) ==> true
      checkOutputType(`/$resource_ut`("/").write(MockJson("body"))) ==> true

      checkOutputType(`/$resource_ut`("/").delete()) ==> true
      checkOutputType(`/$resource_ut`("/").delete("body")) ==> true
      checkOutputType(`/$resource_ut`("/").delete(MockJson("body"))) ==> true
    }
    "Test all resources, typed input" - {
      formatter(`/$resource_tu`("/").check()) ==> ("HEAD", None)

      formatter(`/$resource_tu`("/").read()) ==> ("GET", None)
      formatter(`/$resource_tu`("/").read("body")) ==> ("GET", Some("body"))
      formatter(`/$resource_tu`("/").read(MockJson("body"))) ==> ("GET", Some("body"))
      formatter(`/$resource_tu`("/").read(InWrapper("body"))) ==> ("GET", Some("body"))

      formatter(`/$resource_tu`("/").send()) ==> ("POST", None)
      formatter(`/$resource_tu`("/").send("body")) ==> ("POST", Some("body"))
      formatter(`/$resource_tu`("/").send(MockJson("body"))) ==> ("POST", Some("body"))
      formatter(`/$resource_tu`("/").send(InWrapper("body"))) ==> ("POST", Some("body"))

      formatter(`/$resource_tu`("/").write()) ==> ("PUT", None)
      formatter(`/$resource_tu`("/").write("body")) ==> ("PUT", Some("body"))
      formatter(`/$resource_tu`("/").write(MockJson("body"))) ==> ("PUT", Some("body"))
      formatter(`/$resource_tu`("/").write(InWrapper("body"))) ==> ("PUT", Some("body"))

      formatter(`/$resource_tu`("/").delete()) ==> ("DELETE", None)
      formatter(`/$resource_tu`("/").delete("body")) ==> ("DELETE", Some("body"))
      formatter(`/$resource_tu`("/").delete(MockJson("body"))) ==> ("DELETE", Some("body"))
      formatter(`/$resource_tu`("/").delete(InWrapper("body"))) ==> ("DELETE", Some("body"))
    }
    "Test all resources, typed input and output" - {
      formatter(`/$resource_tt`("/").check()) ==> ("HEAD", None)

      formatter(`/$resource_tt`("/").read()) ==> ("GET", None)
      formatter(`/$resource_tt`("/").read("body")) ==> ("GET", Some("body"))
      formatter(`/$resource_tt`("/").read(MockJson("body"))) ==> ("GET", Some("body"))
      formatter(`/$resource_tt`("/").read(InWrapper("body"))) ==> ("GET", Some("body"))

      formatter(`/$resource_tt`("/").send()) ==> ("POST", None)
      formatter(`/$resource_tt`("/").send("body")) ==> ("POST", Some("body"))
      formatter(`/$resource_tt`("/").send(MockJson("body"))) ==> ("POST", Some("body"))
      formatter(`/$resource_tt`("/").send(InWrapper("body"))) ==> ("POST", Some("body"))

      formatter(`/$resource_tt`("/").write()) ==> ("PUT", None)
      formatter(`/$resource_tt`("/").write("body")) ==> ("PUT", Some("body"))
      formatter(`/$resource_tt`("/").write(MockJson("body"))) ==> ("PUT", Some("body"))
      formatter(`/$resource_tt`("/").write(InWrapper("body"))) ==> ("PUT", Some("body"))

      formatter(`/$resource_tt`("/").delete()) ==> ("DELETE", None)
      formatter(`/$resource_tt`("/").delete("body")) ==> ("DELETE", Some("body"))
      formatter(`/$resource_tt`("/").delete(MockJson("body"))) ==> ("DELETE", Some("body"))
      formatter(`/$resource_tt`("/").delete(InWrapper("body"))) ==> ("DELETE", Some("body"))
    }
    "Test all resources, typed input and output (output type)" - {
      // Output types:
      checkOutputType(`/$resource_tt`("/").check()) ==> true

      checkOutputType(`/$resource_tt`("/").read()) ==> true
      checkOutputType(`/$resource_tt`("/").read("body")) ==> true
      checkOutputType(`/$resource_tt`("/").read(MockJson("body"))) ==> true

      checkOutputType(`/$resource_tt`("/").send()) ==> true
      checkOutputType(`/$resource_tt`("/").send("body")) ==> true
      checkOutputType(`/$resource_tt`("/").send(MockJson("body"))) ==> true

      checkOutputType(`/$resource_tt`("/").write()) ==> true
      checkOutputType(`/$resource_tt`("/").write("body")) ==> true
      checkOutputType(`/$resource_tt`("/").write(MockJson("body"))) ==> true

      checkOutputType(`/$resource_tt`("/").delete()) ==> true
      checkOutputType(`/$resource_tt`("/").delete("body")) ==> true
      checkOutputType(`/$resource_tt`("/").delete(MockJson("body"))) ==> true
    }
  }
}