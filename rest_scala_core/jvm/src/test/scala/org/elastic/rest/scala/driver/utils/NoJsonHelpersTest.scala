package org.elastic.rest.scala.driver.utils

import org.elastic.rest.scala.driver.RestBaseImplicits.CustomStringToTyped
import utest._

object NoJsonRuntimeHelpersTest extends TestSuite {

  case class TestWrapper1(s: String) extends CustomStringToTyped

  object Embedded extends EmbeddedTrait {
    case class TestWrapper2(s: String) extends CustomStringToTyped
  }
  trait EmbeddedTrait {
    case class TestWrapper3(s: String) extends CustomStringToTyped
  }

  class TestFail(s: String, b: Boolean) extends CustomStringToTyped

  val tests = this {
    "Test createCustomTyped" - {

      val test1 = NoJsonRuntimeHelpers.createCustomTyped[TestWrapper1]("test")
      test1 ==> TestWrapper1("test")

      val test2 = NoJsonRuntimeHelpers.createCustomTyped[Embedded.TestWrapper2]("test")
      test2 ==> Embedded.TestWrapper2("test")

      val test3 = NoJsonRuntimeHelpers.createCustomTyped[Embedded.TestWrapper3]("test")
      test3 ==> Embedded.TestWrapper3("test")
    }
  }
}
