# LightIoC

<div align="center">
    <img src="https://img.shields.io/github/stars/ControlNet/LightIoC?style=flat-square">
    <img src="https://img.shields.io/github/forks/ControlNet/LightIoC?style=flat-square">
    <a href="https://github.com/ControlNet/LightIoC/issues"><img src="https://img.shields.io/github/issues/ControlNet/LightIoC?style=flat-square"></a>
    <a href="https://search.maven.org/artifact/space.controlnet/lightioc_2.13"><img src="https://img.shields.io/maven-central/v/space.controlnet/lightioc_2.13?style=flat-square"></a>
    <img src="https://img.shields.io/github/license/ControlNet/LightIoC?style=flat-square">
</div>

<div align="center">  
    <a href="https://www.scala-lang.org/"><img src="https://img.shields.io/badge/Scala-2.11%20%7C%202.12%20%7C%202.13-DC322F?style=flat-square&logo=scala"></a>
    <a href="https://openjdk.org/"><img src="https://img.shields.io/badge/Java-%3E%3D8-1565C0?style=flat-square&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAYAAAA7MK6iAAAABmJLR0QA/wD/AP+gvaeTAAAC1klEQVRIieWWy4tUVxDGfzXGMRlFHV/oiPhCQYZB1BDiEx1ciI9VdsaNC0V0oyFZ6SYoJP4DBkIMuFA34kKIWQRE8YGi6EJFRScxPogkRI0zGNSMvyzOaWjb6Zl7TceNBRf6Vp/6Purcqq8KGmDqJHVkmZimRhAD84CVb5VYHQfsBp79V6wypO3qjfw0vy3SDWqP+kSdl30j1HXqLnV6owmHq4dM1q0uyf6t+f2Q2tpo0iHq2Uz6WF2Q/Ruzb7caDSXNBOszQa+6qMp/Xn2mDiqKVbaqK9kciYhTVf4eoBmYXRKvmKlD1VvqFbWpyr9Mfanu+1+IM8ks9Tf10xr/fvVqUZzSAhIR14CPgDXq8Kq//gC+LYvXr6kT1aVqpzoi+warM/PvoWpnTUybulqd/CaEn6i3cyXvV1sGOD9InaA2qXPVferf6pyyxJuzMlWsW+1SL6ln1AtVz2X1uDq/Kv793Hrba7Hf6484Ivao3wFrgcXAWVJLdQP/AM+BO8DtiHjUB0QHqY5+fQ27QNbtwEVS8XwDXAUk9e1gYAppLDYBn0XELzmuBTgNvAAWRcTzgbhqiYeoX6rX7dt61ZvqXnV5jmnNNfG1Oqwv3MK6mgtkB7ATqEjjE+CvnHF3Rc3UscDDiOgtlWUd4is5wwfqSdOwuJU12qxcnxfFK5PxQmAbMBcYm909wE3gEnAU+CkiXhZP51WCNvWY+jT34Yw3wJilriob9EW+unOWGHNV8a3qYfX7/s69dtVqG+naZgNdwI+ktrgH3CUVVMVaSNc+AZgDfAx0AmeAdRHxe8Zsrm2nPr+xaeStIK2sHUA7MLpeksDPpF6/CPwQEZercLYAXRFxtL8bqGumRW6iOi3r8FTrLPHqB+raLKNfWXYdylo71QFWV9Ok6lA3qQfVP9UD6of1YopIZjOwAJgBjALGAJNIEjkMGJ+P3geuAyeAUxHxuEhy7479CzDReAJhB6COAAAAAElFTkSuQmCC"></a>
    <a href="https://github.com/ControlNet/LightIoC/actions/workflows/unittest.yml"><img src="https://img.shields.io/github/actions/workflow/status/ControlNet/LightIoC/unittest.yml?branch=dev&label=unittest&style=flat-square"></a>
    <a href="https://github.com/ControlNet/LightIoC/actions/workflows/release.yml"><img src="https://img.shields.io/github/actions/workflow/status/ControlNet/LightIoC/release.yml?branch=master&label=release&style=flat-square"></a>
</div>

A light-weight Inversion of Control (IoC) tools by Dependency Injection (DI) for Scala.

## Using library
This library is complied for Scala 2.13.x, 2.12.x, 2.11.x, and also tested in Java8, 11, 16. 

SBT:
```scala
libraryDependencies += "space.controlnet" %% "lightioc" % "0.3.2"
```

Gradle: 
```groovy
implementation group: "space.controlnet", name: "lightioc_<scala_version>", version: "0.3.2"
```

A Java-friendly API compiled by Scala 2.13 and 2.12 is also provided. See [Java-Friendly API](#java-friendly-api).
Gradle:
```groovy
implementation group: "space.controlnet", name: "lightioc-api_2.13", version: "0.3.2"
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
implementation group: "space.controlnet", name: "lightioc-api_2.13", version: "0.3.1"
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