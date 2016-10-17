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

      //(currently nothing to test here)
    }
  }
}
