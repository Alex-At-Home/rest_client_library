package org.elastic.rest.scala.driver.util

import org.elastic.rest.scala.driver.RestBase.CustomStringToTyped
import org.elastic.rest.scala.driver.utils.NoJsonHelpers
import utest._

object NoJsonHelpersTest extends TestSuite {

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
