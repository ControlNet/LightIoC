# LightIoC
A light-weight Inversion of Control (IoC) tools by Dependency Injection (DI) for Scala.

## Using library
This library is complied for Scala 2.13.x, 2.12.x, 2.11.x, and also tested in Java8, 11, 16. 

SBT:
```scala
libraryDependencies += "space.controlnet" %% "lightioc" % "0.2.0"
```

Gradle: 
```groovy
implementation group: "space.controlnet", name: "lightioc_2.13", version: "0.2.0"
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
import space.controlnet.lightioc.Factory.*=>
import space.controlnet.lightioc.Util._

class Foo
class Bar {
  var x: Int = _
  var y: Int = _
}
object Baz

object Main extends App {
  // register Foo to self constructor in Transient scope
  Container.register[Foo].toSelf.inTransientScope.done()
  // register Bar to self constructor in Singleton scope
  Container.register[Bar].toSelf.inSingletonScope.done()
  // register Baz as well
  Container.register[Baz.type].toSelf.inSingletonScope.done()
  // register a constant value
  Container.register("A Number").toValue(123).inSingletonScope.done()
  // register a factory
  val factory: Any *=> Bar = new (Any *=> Bar) {
    override def call(xs: Any*): Bar = { (xs match {
      case Seq(x: Int, y: Int) => (new Bar, x, y)
      case _: Seq[Any] => throw new Exception
    }) |> {
      case (obj: Bar, x: Int, y: Int) =>
        obj.x = x
        obj.y = y
        obj
    }}
  }
  Container.register[Bar].toFactory(factory).inSingletonScope.done()
  // register to another service (registry)
  Container.register[Foo].toSelf.inTransientScope.done() // target service
  Container.register("AnotherFoo").toService[Foo].done()
}
```

Of course, if you prefer to use operators...
```scala
import space.controlnet.lightioc.Container
import space.controlnet.lightioc.BindingSetter.Self

object Main extends App {
  // register to value in Transient
  Container.register("AString") -> "Hello IOC"
  // register to constructor/class in Transient
  Container.register[Foo] -> classOf[Foo]
  // register to constructor/class in Singleton
  Container.register[Bar] := classOf[Bar]
  // register to self
  Container.register[Baz] -> Self
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
  // resolve factory
  val func = Container.resolveFactory[Bar]
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

## Acknowledges

- [dylech30th/simple-ioc](https://github.com/dylech30th/simple-ioc): A kotlin based IoC container implementation.
- [inversify/InversifyJS](https://github.com/inversify/InversifyJS): A powerful and lightweight IoC container for JS apps powered by TypeScript.