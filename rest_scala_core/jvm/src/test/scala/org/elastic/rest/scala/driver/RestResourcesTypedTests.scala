package org.elastic.rest.scala.driver

import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseRuntimeTyped._
import org.elastic.rest.scala.driver.test_utils.SampleResources._
import org.elastic.rest.scala.driver.test_utils.SampleResourcesTyped._
import utest._

import scala.reflect.runtime.universe

object RestResourcesTypedTests extends TestSuite {

  def checkOutputType[T](op: TypedOperation[T])(implicit ct: universe.WeakTypeTag[T]): Boolean =
    ct.tpe =:= universe.typeOf[OutWrapper]

  val tests = this {
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
