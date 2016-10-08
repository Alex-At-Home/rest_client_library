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
      checkOutputType(`/$resource_ut`("/").readS("body")) ==> true
      checkOutputType(`/$resource_ut`("/").readJ(MockJson("body"))) ==> true

      checkOutputType(`/$resource_ut`("/").send()) ==> true
      checkOutputType(`/$resource_ut`("/").sendS("body")) ==> true
      checkOutputType(`/$resource_ut`("/").sendJ(MockJson("body"))) ==> true

      checkOutputType(`/$resource_ut`("/").write()) ==> true
      checkOutputType(`/$resource_ut`("/").writeS("body")) ==> true
      checkOutputType(`/$resource_ut`("/").writeJ(MockJson("body"))) ==> true

      checkOutputType(`/$resource_ut`("/").delete()) ==> true
      checkOutputType(`/$resource_ut`("/").deleteS("body")) ==> true
      checkOutputType(`/$resource_ut`("/").deleteJ(MockJson("body"))) ==> true
    }
    "Test all resources, typed input and output (output type)" - {
      // Output types:
      checkOutputType(`/$resource_tt`("/").check()) ==> true

      checkOutputType(`/$resource_tt`("/").read()) ==> true
      checkOutputType(`/$resource_tt`("/").readS("body")) ==> true
      checkOutputType(`/$resource_tt`("/").readJ(MockJson("body"))) ==> true

      checkOutputType(`/$resource_tt`("/").send()) ==> true
      checkOutputType(`/$resource_tt`("/").sendS("body")) ==> true
      checkOutputType(`/$resource_tt`("/").sendJ(MockJson("body"))) ==> true

      checkOutputType(`/$resource_tt`("/").write()) ==> true
      checkOutputType(`/$resource_tt`("/").writeS("body")) ==> true
      checkOutputType(`/$resource_tt`("/").writeJ(MockJson("body"))) ==> true

      checkOutputType(`/$resource_tt`("/").delete()) ==> true
      checkOutputType(`/$resource_tt`("/").deleteS("body")) ==> true
      checkOutputType(`/$resource_tt`("/").deleteJ(MockJson("body"))) ==> true
    }
  }
}
