package org.elastic.rest.scala.driver

import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.test_utils.SampleResources._
import utest._

object RestBaseTests extends TestSuite {


  val tests = this {
    "Check modifiers work" - {
      //TODO
      /**/println("****************i")

      trait SeqModifiers extends Modifier with BaseDriverOp {
        @Param def getSeqModifier(ss: String*): this.type = Modifier.Body
        @Param def getStringModifier2(s: String): this.type = Modifier.Body
      }
      trait OtherModifiers extends Modifier with BaseDriverOp {
        @Param def getStringModifier(s: String): this.type = Modifier.Body

        @Param def getBooleanModifier(b: Boolean): this.type = Modifier.Body

        @Param def getListModifier(ss: List[String]): this.type = Modifier.Body

        @Param def getSeqModifier2(ss: String*): this.type = Modifier.Body
      }
      /** Example of a concrete base driver op generated by a macro method not under test here */
      case class SampleDriverOp
        (resource: RestResource, op: String, body: Option[String], mods: List[(String, Any)], headers: List[String])
        extends BaseDriverOp with SeqModifiers with OtherModifiers
      {
        override def withModifier(kv: (String, Any)): this.type = SampleDriverOp(resource, op, body, kv :: mods, headers)
          .asInstanceOf[this.type]

        override def withHeader(h: String): this.type = SampleDriverOp(resource, op, body, mods, h :: headers)
          .asInstanceOf[this.type]
      }
      val modTest = SampleDriverOp(null, "", None, List(), List())

      modTest.getStringModifier("x").mods.map(Modifier.asString) ==> List("getStringModifier=x")
      modTest.getBooleanModifier(true).mods.map(Modifier.asString) ==> List("getBooleanModifier=true")
      modTest.getListModifier(List("x", "y")).mods.map(Modifier.asString) ==> List("getListModifier=x,y")
      modTest.getSeqModifier("x", "y").mods.map(Modifier.asString) ==> List("getSeqModifier=x,y")
      modTest.getStringModifier2("x").mods.map(Modifier.asString) ==> List("getStringModifier2=x")
      modTest.getSeqModifier2("x", "y").mods.map(Modifier.asString) ==> List("getSeqModifier2=x,y")
    }
    "Check the location generator works" - {
      case class `/$list`(list: Seq[String]) extends RestResource
      case class `/test/$variable/test2/$anotherVariable`
        (variable: String, anotherVariable: String) extends RestResource
      case class `/test/$list`(ss: String*) extends RestResource

      `/`().location ==> "/"
      `/$list`(Seq("a", "b")).location ==> "/a,b"
      `/test/$variable/test2/$anotherVariable`("a", "b").location ==> "/test/a/test2/b"
      `/test/$list`("a", "b").location ==> "/test/a,b"
    }
  }
}
