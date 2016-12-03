package org.elastic.rest.scala.driver.json.tests

import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser.parse
import org.elastic.rest.scala.driver.RestBase
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestResources._
import org.elastic.rest.scala.driver.utils.MockRestDriver
import org.elastic.rest.scala.driver.json.CirceJsonModule._
import utest._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CirceJsonModuleTests extends TestSuite {

  val tests = this {
    val handler: PartialFunction[BaseDriverOp, Future[String]] = {
      case BaseDriverOp(TestApiUntyped.`/`(), RestBase.PUT,
        Some("""{"test":"write"}"""), List(), List()) =>
          Future.successful("""{ "test": "written" }""")
      case BaseDriverOp(TestApiUntyped.`/`(), RestBase.DELETE, _, List(), List()) =>
        Future.successful(""" NOT_JSON """)
      case BaseDriverOp(TestApiUntyped.`/`(), RestBase.GET, _, List(), List()) =>
        Future.successful("""{ "test": "get" }""")
      case x @ _ =>
        Future.failed(new Exception(s"Unexpected request: $x"))
    }
    implicit val mockDriver = new MockRestDriver(handler)
    "Test JSON - read" - {
      TestApiUntyped.`/`().read().execJ().map { result =>
        result ==> parse("""{ "test": "get" }""").right.getOrElse(Json.Null)
      }
    }
    "Test JSON - failed to deser response" - {
      TestApiUntyped.`/`().delete().execJ().failed.map { case result: RestServerException =>
        result.code ==> 200
      }
    }
    "Test JSON - write" - {
      val json = parse("""{ "test": "write" }""").right.getOrElse(Json.Null)
      TestApiUntyped.`/`().writeJ(json).execJ().map { result =>
        result ==> parse("""{ "test": "written" }""").right.getOrElse(Json.Null)
      }
    }
    "Test Lens helpers" - {

      case class DeclareJsonFromStringTest(asString: String) extends DeclareJsonFromString {
        import DeclareJsonFromStringTest._
        def getTest = registerTyped[String](test, "default")
        def getTestOption = registerTyped[String](test)
        def getList = registerTyped[String](list)
        def getListObj: List[JsonObject] = registerJson(listObj)
        def getTestObjOption: Option[JsonObject] = registerJson(testObj)
        def getTestObj: JsonObject = registerJson(testObj, parse("{}").right.get.asObject.get)
      }
      object DeclareJsonFromStringTest {
        val test = root.test.string
        val testObj = root.testObj.json
        val list = root.list.each.string
        val listObj = root.listObj.each.json
      }

      intercept[Exception] { DeclareJsonFromStringTest("not_json_obj").asJson }
      DeclareJsonFromStringTest("{}").asJson ==> parse("{}").right.get.asObject.get
      DeclareJsonFromStringTest("{}").asRawJson ==> parse("{}").right.get
      DeclareJsonFromStringTest("not_json_obj")
        .asParsedRawJson.left.get.message ==> parse("not_json_obj").left.get.message //(JS won't match the errors)
      DeclareJsonFromStringTest("{}").asParsedRawJson ==> parse("{}")

      DeclareJsonFromStringTest("{}").getTest ==> "default"
      DeclareJsonFromStringTest("{}").getTestOption ==> None
      DeclareJsonFromStringTest(""" { "test": "val" } """).getTest ==> "val"
      DeclareJsonFromStringTest(""" { "test": "val" } """).getTestOption ==> Some("val")

      DeclareJsonFromStringTest("{}").getList ==> List()
      DeclareJsonFromStringTest(""" { "list": [ "val1", "val2" ] } """).getList ==> List("val1", "val2")

      DeclareJsonFromStringTest("{}").getTestObj ==> parse("{}").right.get.asObject.get
      DeclareJsonFromStringTest("{}").getTestObjOption ==> None

      DeclareJsonFromStringTest(""" { "testObj": {"test": "val"} } """).getTestObj ==> parse(
        """{"test": "val"}""").right.get.asObject.get
      DeclareJsonFromStringTest(""" { "testObj": {"test": "val"} } """).getTestObjOption ==> Some(parse(
        """{"test": "val"}""").right.get.asObject.get)

      DeclareJsonFromStringTest("{}").getListObj ==> List()
      DeclareJsonFromStringTest(""" { "listObj": [ {"test": "val1"}, {"test": "val2"} ] } """).getListObj ==> List(
        parse("""{"test": "val1"}""").right.get.asObject.get,
        parse("""{"test": "val2"}""").right.get.asObject.get
      )
    }
  }
}

/** Sample API for testing CIRCE integration
  */
object TestApiUntyped {
  case class `/`()
    extends RestReadable[Modifier]
    with RestDeletable[Modifier]
    with RestWritable[Modifier]
    with RestResource
}
