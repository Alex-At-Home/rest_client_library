# Scala REST driver   [![Build Status](https://travis-ci.org/Alex-At-Home/rest_client_library.svg?branch=master)](https://travis-ci.org/Alex-At-Home/rest_client_library)

## Overview

This is a small library designed to make it easy to build clients/drivers for (JSON) REST-based services:
* Almost exclusively declarative, no client-specific business logic
* Optional typing, natively supports strings and ("bring-your-own") JSON (`*`)
   * (`*`) This currently comes with some caveats, see below under TODO_LINK
* Versionable
* "Bring-your-own-HTTP-client"
* Runs in JVM Scala or scalajs

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

```
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
  trait PrettyModifier extends Modifier { self: BaseDriverOp =>
    def pretty(b: Boolean) = self.withModifier(this.getModifier(b))
  }
}
```

And that's it! See below (TODO_LINK) for more details on the API. Then you can use it as follows:

```
  import ApiModel
  
  val implicit driver = ??? // see below
  val createRequest = `/database/users`.send(""" { "name": "Alex" } """)
  val createReply: Future[String] = createRequest.execS() //or execJ to get JSON, see below
  // {"name":"Alex"}
  val getRequest = `/database/users/$userId`("Alex").pretty(true)
  val getReply: Future[String] = createReply.flatMap(_ => getRequest.execS()) // (or execJ)
  // {\n\t"name": "Alex"\n}
```

## JSON

The REST driver supports any JSON library, with a simple connector (see below). Support for CIRCE (TODO_LINK) is provided. 

For returning the REST response in JSON directly, import all the classes in the desired JSON connector (eg `import org.elastic.rest.scala.driver.json.CirceJsonModule._`), and then:
* call the implicit `execJ` on the `BaseDriverOp` that is returned from the `read()`/`write(...)`/`send(...)`/`delete()`/etc calls to return a `Future[J]` where `J` is eg `JObject` in CIRCE (TODO_LINK), TODO other examples
* When sending data (eg in `write()` or `send()` calls, simply pass an object of type `J` (eg TODO) instead of a String.

eg:

```
import org.elastic.rest.scala.driver.json.CirceJsonModule._
TODO examples
```

_(Of course the default string input/output can be used together with a JSON library with no connector with your own implicits etc)_

## Typed API calls

It is possible to declare any of the REST resources as optionally typed in either their input or their output. 

* For resources with no input data (such as `Readable[M]`, `Checkable[M]`, etc), the typed variant has a `T` on the end, and an type extra parameter for the type, eg `ReadableT[M, O]` (after `read()`, then an extra method `exec()` returns a future `O`)
* For resources with both input and output data (such as `Writable[M]` and `Sendable[M]`), there are 3 typed variants:
   * `TU` with one extra type parameter, for typed input and untyped output, eg `WritableTU[M, I]`, with the extra method  `write(I)` that returns a future String/JSON object via `execJ()`/`execS()`
   * `UT` withone extra type parameter, for untyped input and typed output, eg `SendableUT[M, O]`, where `write(String)` and `write[J](J)` can in addition return a future `O` via `exec()`

The typed variants require that a JSON module (see below TODO_LINK) is imported for its implicts

So extending the example above:

```
object DataModel {
  case class DatabaseRecord(name: String, age: Option[Int])
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

```
import org.elastic.rest.scala.json.CirceTypeModule._

val newRecord = DatabaseRecord("Alex", Some(21))
val updatedRecord: Future[DatabaseRecord] = `/database/users/$userId`("Alex").write(newRecord).exec()
```

## Summary of the API

### Resources

TODO

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
