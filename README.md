# LightIoC
A light-weight Inversion of Control (IoC) tools by Dependency Injection (DI) for Scala.

## Using library
This library is complied for Scala 2.13.x, 2.12.x, 2.11.x, and also tested in Java8, 11, 16. 

SBT:
```scala
libraryDependencies += "space.controlnet" %% "lightioc" % "0.3.0"
```

Gradle: 
```groovy
implementation group: "space.controlnet", name: "lightioc_<scala_version>", version: "0.3.0"
```

A Java-friendly API compiled by Scala 2.13 and 2.12 is also provided. See [Java-Friendly API](#java-friendly-api).
Gradle:
```groovy
implementation group: "space.controlnet", name: "lightioc-api_2.13", version: "0.3.0"
```

## Quick start

### Static registry

```scala
import space.controlnet.lightioc.Container

@Provider 
class TopLevelClass

@Singleton
class TopLevelSingletonClass

@Provider(isObject = true)
object TopLevelObject

@Provider(stringId = "IdForThisClass")
class NamedProviderClass

@Provider(isObject = true)
object NestedClass {

  @Provider
  class InnerClass

  @Provider(isObject = true)
  object InnerObject
}

object Main extends App {
  Container.init("<package name>")
}
```

### Dynamic registry
```scala
import space.controlnet.lightioc.Container
import space.controlnet.lightioc.Util._

class Foo
class Bar {
  var x: Int = _
  var y: Int = _
}
object Baz
class Qux(foo: Foo)

object Main extends App {
  // register Foo to self constructor in Transient scope
  Container.register[Foo].toSelf.inTransientScope()
  // register Bar to self constructor in Singleton scope
  Container.register[Bar].toSelf.inSingletonScope()
  // register Baz as well
  Container.register[Baz.type].toSelf.inSingletonScope()
  // register a constructor with parameters
  Container.register[Qux].toConstructor(classOf[Foo]).inTransientScope()
  // register a constant value
  Container.register("A Number").toValue(123).inSingletonScope()
  // register a factory
  val barX = 1
  val barY = 2
  Container.register[Int]("Bar.x").toValue(barX).inSingletonScope()
  Container.register[Int]("Bar.y").toValue(barY).inSingletonScope()
  val factory: Factory[Bar] = Container => {
    // do anything you want
    val bar = new Bar
    bar.x = Container.resolve[Int]("Bar.x")
    bar.y = Container.resolve[Int]("Bar.y")
    bar
  }
  Container.register[Bar].toFactory(factory).inTransientScope()
  // register to another service (registry)
  Container.register[Foo].toSelf.inTransientScope() // target service
  Container.register("AnotherFoo").toService[Foo]
}
```

Of course, if you prefer to use operators...
```scala
import space.controlnet.lightioc.Container
import space.controlnet.lightioc.BindingSetter.{ New, Self }

object Main extends App {
  // register to value in Transient
  Container.register("AString") -> "Hello IOC"
  // register to constructor/class in Transient
  Container.register[Foo] -> classOf[Foo]
  // register to constructor/class in Singleton
  Container.register[Bar] := classOf[Bar]
  // register to self
  Container.register[Baz.type] -> Self
  // register to a constructor with parameters
  Container.register[Qux] -> New(classOf[Foo])
  // register to factory
  Container.register[Bar] ~> factory
  // register to service
  Container.register("AnotherString") >> "AString"
}
```

### Dynamic resolve
```scala
import space.controlnet.lightioc.Container

object Main extends App {
  // resolve by type
  val foo: Foo = Container.resolve[Foo]
  // resolve by string
  val str: String = Container.resolve[String]("AString")
  // resolve by factory
  val bar: Bar = Container.resolve[Bar]
}
```

### Autowire
```scala
import space.controlnet.lightioc.annotation.{ Autowired, Provider }

@Provider
class Foo {
  @Autowired
  val bar: Bar = null
}

@Provider
class Bar

@Provider
object Baz {
  @Autowired
  var x: Int = 0 // should be "var"
}
```

## Java-Friendly API

Gradle:
```groovy
implementation group: "space.controlnet", name: "lightioc-api_2.13", version: "0.3.0"
```

### Kotlin Example
```kotlin
import space.controlnet.lightioc.annotation.Singleton
import space.controlnet.lightioc.api.Container

@Singleton
class A {
    val x: Int = 100
}

fun main() {
    Container.init("<package name>")
    val a = Container.resolve(A::class.java)
    println(a.x)
}
```


## Acknowledges

- [dylech30th/simple-ioc](https://github.com/dylech30th/simple-ioc): A kotlin based IoC container implementation.
- [inversify/InversifyJS](https://github.com/inversify/InversifyJS): A powerful and lightweight IoC container for JS apps powered by TypeScript.