# Scala REST driver   [![Build Status](https://travis-ci.org/Alex-At-Home/rest_client_library.svg?branch=master)](https://travis-ci.org/Alex-At-Home/rest_client_library) [![Coverage Status](https://coveralls.io/repos/github/Alex-At-Home/rest_client_library/badge.svg?branch=master)](https://coveralls.io/github/Alex-At-Home/rest_client_library?branch=master) [![Scala.js](http://scala-js.org/assets/badges/scalajs-0.6.8.svg)](http://scala-js.org)

## Overview

This is a small library designed to make it easy to build clients/drivers for (JSON) REST-based services:
* Almost exclusively declarative, no client-specific business logic
* Optional typing, natively supports strings and ("bring-your-own") JSON (`*`)
   * (`*`) This currently comes with some caveats, see below under TODO_LINK
* Versionable
* "Bring-your-own-HTTP-client"
* Runs in [Scala.JS](https://www.scala-js.org/) as well as in the JVM

Note that the underlying idea is that these clients map very tightly to the REST endpoints exposed by the target service. This has two implications:
* To understand a service, you only need to understand the REST interface (vs both the REST interface and the scala/java/whatever API)
   * The counterpoint is that you could argue that manipulating REST-like resources is not a very idiomatic (/good) way of writing code, which is fair. If the examples below don't convince you this is a good idea, then this is may not be the library for you, which is fine!
* Taking this approach makes it far easier to define and maintain the API, which is of course the whole point of this library!

## Simple example

Let's say you have a fairly standard REST service:
* `POST` JSON to `/database/users` to create a new user
* `GET` `/database/users/<<userId>>` to get a user, with (eg) the modifier `?pretty=true` used to return prettified JSON
* `PUT` JSON to `/database/users/<<userId>>` to update a user
* `DELETE` `/database/users/<<userId>>` to delete a user
* `POST` with no body to `/database/users/_synchronize` to delete expired users (or whatever)  

Then you would declare this API using `scala_rest_driver` as follows:

```scala
object ApiModel {
  import org.elastic.rest.scala.RestBase._
  import org.elastic.rest.scala.RestResources._

  case class `/database/users`() 
    extends RestSendable[BaseDriverOp] 
    with RestResource

  case class `/database/users/$userId`(userId: String) 
    extends RestReadable[PrettyModifierGroup] 
    with RestWritable[BaseDriverOp] 
    with RestDeletable[BaseDriverOp] 
    with RestResource

  case class `/database/users/_synchronize`()
    extends RestNoDataSendable[BaseDriverOp] 
    with RestResource

  trait PrettyModifierGroup extends PrettyModifier with BaseDriverOp 
  trait PrettyModifier extends Modifier { 
    @Param def pretty(b: Boolean) = Modifier.body
  }
}
```

And that's it! See below (TODO_LINK) for more details on the API. Then you can use it as follows:

```scala
  import ApiModel._
  
  val implicit driver = ??? // see below
  val createRequest = `/database/users`().sendS(""" { "name": "Alex" } """) //or sendJ to send JSON, see below
  val createReply: Future[String] = createRequest.execS() //or execJ to get JSON, see below
  // eventually """{"name":"Alex"}"""
  // Blocking version (mostly for testing)
  `/database/users`().sendS(""" { "name": "Alex" } """).resultS() //or sendJ, or resultJ
  // Try[String] = Success("""{"name":"Alex"}""")
  val getRequest = `/database/users/$userId`("Alex").pretty(true)
  val getReply: Future[String] = createReply.flatMap(_ => getRequest.execS()) // (or execJ)
  // eventually """{\n\t"name": "Alex"\n}"""
```

## JSON

The REST driver supports any JSON library, with a simple connector (see below). Support for CIRCE (TODO_LINK) is provided. 

For returning the REST response in JSON directly, import all the classes in the desired JSON connector (eg `import org.elastic.rest.scala.driver.json.CirceJsonModule._`), and then:
* call the implicit `execJ` on the `BaseDriverOp` that is returned from the `read()`/`writeS(...)`/`sendS(...)`/`delete()`/etc calls to return a `Future[J]` where `J` is eg `Json` in [CIRCE](https://github.com/travisbrown/circe), TODO other examples
* When sending data (eg in `writeJ()` or `sendJ()` calls, simply pass an object of type `J` (eg TODO) instead of a String. (Or use `resultS`/`resultJ` to get the result via a single blocking call, though not in Scala.JS of course).

eg:

```scala
import org.elastic.rest.scala.driver.json.CirceJsonModule._
import io.circe._, io.circe.parser._

val jsonIn: Json = parse(""" { "name": "Alex" } """).getOrElse(Json.Null)
val tryJsonOut: Try[Json] = `/database/users`().sendJ(json).resultJ()
// Success({ "name": "Alex" })

```

_(Of course the default string input/output can be used together with a JSON library with no connector with your own implicits etc)_

## Typed API calls

It is possible to declare any of the REST resources as optionally typed in either their input or their output. 

* For resources with no input data (such as `Readable[M]`, `Checkable[M]`, etc), the typed variant has a `T` on the end, and an type extra parameter for the type, eg `ReadableT[M, O]` (after `read()`, then an extra method `exec()` returns a future `O`)
* For resources with both input and output data (such as `Writable[M]` and `Sendable[M]`), there are 3 typed variants:
   * `TU` with one extra type parameter, for typed input and untyped output, eg `WritableTU[M, I]`, with the extra method  `write(I)` that returns a future String/JSON object via `execJ()`/`execS()`
   * `UT` withone extra type parameter, for untyped input and typed output, eg `SendableUT[M, O]`, where `writeS(String)` and `writeJ[J](J)` can in addition return a future `O` via `exec()`

The typed variants require that a JSON module (see below TODO_LINK) is imported for its implicits

So extending the example above:

```scala
import io.circe.generic.JsonCodec

object DataModel {
  @JsonCodec case class DatabaseRecord(name: String, age: Option[Int])
}
object ApiModel {
//...
  case class `/database/users/$userId`(userId: String) 
    extends RestReadableT[PrettyModifierGroup, DatabaseRecord]
    with RestWritableTT[BaseDriverOp, DatabaseRecord, DatabaseRecord]
    with RestDeletable[BaseDriverOp]
    with RestResource
//...
}    
```

And then:

```scala
import org.elastic.rest.scala.json.CirceTypeModule._

val newRecord = DatabaseRecord("Alex", Some(21))
val updatedRecord: Future[DatabaseRecord] = `/database/users/$userId`("Alex").write(newRecord).exec()
```

## Summary of the API

* [Scaladocs for the REST client library](https://alex-at-home.github.io/rest_client_library/current/index.html)

### Resources

#### Declaring resources

Each REST resource should be declared as a case class inherited from a combination (using `extends`/`with`) of the following:
* `RestResource`, which provides the `location` (`String`) value as follows:
   * by default the resource URL is auto-generated from the case class name, assuming this name has the format has the format `` `url` `` where url (in backticks) is the path, where each segment name (eg `/segment/name`) is either a string literal or of the format `$variableName` (eg `/segment/$variable/name`). The variables are substituted for the case class parameters in order. 
   * or you can override it as per normal, eg `override lazy val location: String = "/override/resource/url"`
* 1+ from the following:
   * `RestCheckable[M]` / `RestCheckableT[M, O]`, which provides the method `check()` corresponding to the REST `HEAD` request
   * `RestReadable[M]` / `RestReadableT[M, O]`, which provides the method `read()` corresponding to the REST `GET` request
   * `RestDeletable[M]` / `RestDeletableT[M, O]`, which provides the method `delete()` corresponding to the REST `DELETE` request
   * `RestWritable[M]` / `RestWritableUT[M, O]` / `RestWritableTU[M, O]` / `RestWritableTT[M, I, O]` which provides the methods `writeS(str)`, `writeJ(json)`, and optionally `write(typeObj)` corresponding to the REST `PUT` request with a body
   * `RestSendable[M]` / `RestSendableUT[M, O]` / `RestSendableTU[M, O]` / `RestSendableTT[M, I, O]` which provides the methods `sendS(str)`, `sendJ(json)`, and optionally `send(typeObj)` corresponding to the REST `POST` request with a body
   * `RestNoDataWritable[M]` / `RestNoDataWritable[M]` which provides the methods `write()` corresponding to the REST `PUT` request but with no body data
   * `RestNoDataSendable[M]` / `RestNoDataSendableT[M]` which provides the methods `send()` corresponding to the REST `POST` request but with no body data
   * `RestWithDataReadable[M]` / `RestWithDataReadableUT[M, O]` / `RestWithDataReadableTU[M, O]` / `RestWithDataReadableTT[M, I, O]` which  provides the methods `readS(str)`, `readJ(json)`, and optionally `read(typeObj)` corresponding to the REST `GET` request but with a body
   * `RestWithDataDeletable[M]` / `RestWithDataDeleableUT[M, O]` / `RestWithDataDeletableTU[M, O]` / `RestWithDataDeletableTT[M, I, O]` which  provides the methods `deleteS(str)`, `deleteJ(json)`, and optionally `delete(typeObj)` corresponding to the REST `DELETE` request but with a body

The type parameters are: `M` for the list of modifiers (see below), `I` for the input type in the optional typed-input cases (mixed-in traits ending in `TU` or `TT`), `O` for the output type in the optional typed-output cases (mixed-in traits ending in just `T`, `UT` or `TT`). More details on the supported `I` and `O` types is provided below.

_(Note that the typed input methods like `write(typeObj)` are provided by implicits (see below) and hence are not in the scaladocs for the resource classes and may not appear in code completion if the implicits are not imported)_

#### Operating on resources

Once one of the supported resource methods (`read`, `writeS` etc) has been called on a resource instance, it becomes an operation. It is important to note that an operation is still just an effectless case class instance, to which one of three transforms can be applied:
* The operation can be modified using the type parameter `M` described below - this corresponds to URL parameters in HTTP.
* The operation can have headers added, corresponding to HTTP headers.
* The operation can be executed.

In order to execute the operation, an implicit of type `XXX` is required, which supports the following operations:
* `execS(): Future[String]` and `execJ(): Future[J]` - returns a future containing the response
* `resultS(): Try[String]` and `resultJ(): Try[J]` - blocks until a reply is returned or an optional timeout occurs (in the format eg `resultS(timeout: Duration): Try[String]`) 
   * (note that the blocking versions are not avaiable in ScalaJS)
* And in the typed case (resource class ends with a `T`)
   * `exec(): Future[O]` - returns a future containing the typed response
   * `result(): Try[O]` - blocks until the typed response is returned or an optional timeout occurs (in the format eg `result(timeout: Duration): Try[O]`) 
   
_(Note that the `exec()` and `result()` calls are provided via implicits (see below) and hence are not in the scaladocs for the resource classes and may not appear in code completion if the implicits are not imported)_   
   
TODO implicits   
   
TODO error types

### Modifiers

TODO

### Headers

TODO

# Advanced Topics

## Custom typed APIs

TODO

## Versioning

TODO

## Building JSON connectors

TODO

## Building REST connectors

TODO
