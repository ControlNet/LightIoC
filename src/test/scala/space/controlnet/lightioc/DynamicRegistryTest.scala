package space.controlnet.lightioc

import org.scalatest.funspec.AnyFunSpec
import space.controlnet.lightioc.exception.{ NotRegisteredException, ResolveTypeException }

import scala.annotation.tailrec

class DynamicRegistryTest extends AnyFunSpec {
  import DynamicRegistryTest._

  describe("Dynamic registry test ::") {
    it("should register and resolve Foo class") {
      Container.register[Foo].toSelf.inTransientScope.done()
      assert(Container.resolve[Foo].getClass == classOf[Foo])
      assert(Container.resolve[Foo].x == foo.x)
    }

    it("should register and resolve string abc with String identifier") {
      Container.register("aString").toValue(aString).inTransientScope.done()
      assert(Container.resolve[String]("aString") == aString)
    }

    it("should register and resolve int 123 with String identifier") {
      Container.register("aNumber").toValue(aNumber).inTransientScope.done()
      assert(Container.resolve[Int]("aNumber") == aNumber)
    }

    it("should throw Exception when the item is not registered") {
      assertThrows[NotRegisteredException](Container.resolve("Random Things"))
    }

    it("should register in transient scope") {
      Container.register[Foo].toSelf.inTransientScope.done()
      val foo1 = Container.resolve[Foo]
      val foo2 = Container.resolve[Foo]
      assert(foo1 != foo)
      assert(foo1 != foo2)
    }

    it("should register a object") {
      Container.register[Baz.type].toSelf.inSingletonScope.done()
      assert(Container.has[Baz.type])
      assert(Container.resolve[Baz.type].isInstanceOf[Baz.type])
      assert(Container.resolve[Baz.type].x == Baz.x)
    }

    it("should register in singleton scope") {
      Container.register[Foo].toSelf.inSingletonScope.done()
      val foo1 = Container.resolve[Foo]
      val foo2 = Container.resolve[Foo]
      assert(foo1 != foo)
      assert(foo1 == foo2)
    }

    it("should register a function") {
      val identifier = "SUM_FUNCTION"

      Container.register[List[Int] => Int](identifier).toValue { seq =>
        @tailrec
        def wrapper(innerSeq: List[Int], result: Int): Int = innerSeq match {
          case head::tail => wrapper(tail, result + head)
          case Nil => result
        }
        wrapper(seq, 0)
      }.inSingletonScope.done()

      assert(Container.has(identifier))
      assert(Container.resolve[List[Int] => Int](identifier).isInstanceOf[List[Int] => Int])
      assert(Container.resolve[List[Int] => Int](identifier).apply(List.range(0, 10)) == (1 until 10).sum)
    }

    it("should register a main constructor") {
      Container.register[Qux].toConstructor(classOf[Foo]).inTransientScope.done()
      val qux = Container.resolve[Qux]
      assert(qux.isInstanceOf[Qux])
      assert(qux.foo == Container.resolve[Foo])
    }

    it("should register an auxiliary constructor") {
      Container.register[Quux].toConstructor(classOf[Foo]).inTransientScope.done()
      val quux = Container.resolve[Quux]
      assert(quux.isInstanceOf[Quux])
      assert(quux.x == Container.resolve[Foo].x)
    }

    it("should register a factory") {
      Container.register[Bar].toFactory(factory).inTransientScope.done()
      assert(Container.has[Bar])
      assert(Container.resolve[Bar].isInstanceOf[Bar])

      val obj = Container.resolve[Bar]
      assert(obj.isInstanceOf[Bar] && obj.x == barX && obj.y == barY)
    }

    it("should register a service") {
      val identifier = "ANOTHER_FOO"
      Container.register[Foo].toSelf.inTransientScope.done()
      Container.register(identifier).toService[Foo].done()

      assert(Container.has(identifier))
      assert(Container.resolve[Foo](identifier).isInstanceOf[Foo])
      assert(Container.resolve[Foo](identifier) != Container.resolve[Foo](identifier))
    }

    it("should be able to update registry in transient scope") {
      Container.register("abc").toValue(0).inTransientScope.done()
      Container.register("abc").toValue(1).inTransientScope.done()
      assert(Container.resolve[Int]("abc") == 1)
      Container.register("abc").toValue(2).inTransientScope.done()
      assert(Container.resolve[Int]("abc") == 2)
    }

    it("should be able to update registry in singleton scope") {
      Container.register("abc").toValue(0).inSingletonScope.done()
      Container.register("abc").toValue(1).inSingletonScope.done()
      assert(Container.resolve[Int]("abc") == 1)
      Container.register("abc").toValue(2).inSingletonScope.done()
      assert(Container.resolve[Int]("abc") == 2)
    }

  }
}

object DynamicRegistryTest {
  val barX = 1
  val barY = 2
  Container.register[Int]("Bar.x").toValue(barX).inSingletonScope.done()
  Container.register[Int]("Bar.y").toValue(barY).inSingletonScope.done()

  val factory: Container.type => Bar = Container => {
    val bar = new Bar
    bar.x = Container.resolve[Int]("Bar.x")
    bar.y = Container.resolve[Int]("Bar.y")
    bar
  }

  class Foo {
    val x = 2
  }

  class Bar {
    var x: Int = _
    var y: Int = _
  }

  object Baz {
    val x = 0
  }

  class Qux(val foo: Foo)

  class Quux(val x: Int) {
    def this(foo: Foo) = this(foo.x)
  }

  val foo = new Foo
  val aNumber = 123
  val aString = "abc"
}