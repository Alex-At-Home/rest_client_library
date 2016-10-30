package org.elastic.rest.scala.driver.utils

import utest._
import org.elastic.rest.scala.driver.RestBaseImplicits.CustomTypedToString
import org.elastic.rest.scala.driver.utils.NoJsonHelpers.{SimpleObjectDescription => obj}
import org.elastic.rest.scala.driver.utils.NoJsonHelpers.SimpleObjectDescription
import org.elastic.rest.scala.driver.utils.NoJsonHelpers.SimpleObjectDescription._
import io.circe.parser.parse

object SimpleObjectDescriptionTests extends TestSuite {
  val tests = this {
    "Basic mock functionality - handled" - {

      //TODO: add coverage here

      case class Test(field1: String) extends CustomTypedToString {
        @SimpleObjectDescription("obj",
          obj.SimpleObject("insert_here") {
            obj.Field("field1")
          }
        )
        override def fromTyped: String = AutoGenerated
      }
      parse(Test("test").fromTyped) ==> parse("""{ "insert_here": { "field1": "test" } }""")

      case class Test2(field1: String) extends CustomTypedToString { //(test no prefix)
        @SimpleObjectDescription("",
          SimpleObject("insert_here") {
            Field("field1")
          }
        )
        override def fromTyped: String = AutoGenerated
      }
      parse(Test2("test").fromTyped) ==> parse("""{ "insert_here": { "field1": "test" } }""")

    }
  }
}
