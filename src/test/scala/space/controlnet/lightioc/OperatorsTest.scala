package space.controlnet.lightioc

import org.scalatest.funspec.AnyFunSpec
import space.controlnet.lightioc.BindingSetter.{ New, Self }
import space.controlnet.lightioc.DynamicRegistryTest.factory
import space.controlnet.lightioc.exception.RegistryTypeException

class OperatorsTest extends AnyFunSpec {
  import OperatorsTest._
  Container.init("space.controlnet.lightioc")

  describe("Binding setter operators") {
    it("should support binding for value and class") {
      Container.register("A string") -> "Hello IOC"
      Container.register[Foo] -> classOf[Foo]
      Container.register[Bar] := classOf[Bar]

      assert(Container.resolve[String]("A string") == "Hello IOC")
      assert(Container.resolve[Foo].isInstanceOf[Foo])
      assert(Container.resolve[Foo] != Container.resolve[Foo])
      assert(Container.resolve[Bar].isInstanceOf[Bar])
      assert(Container.resolve[Bar] == Container.resolve[Bar])
    }

    it("should support binding to self") {
      Container.register[Baz] -> Self
      assert(Container.resolve[Baz].isInstanceOf[Baz])
    }

    it("should support binding to constructor") {
      Container.register[Qux] := New(classOf[Foo])
      Container.register[Quux] -> New(classOf[Qux])

      assert(Container.resolve[Qux].isInstanceOf[Qux])
      assert(Container.resolve[Qux] == Container.resolve[Qux])
      assert(Container.resolve[Quux].isInstanceOf[Quux])
      assert(Container.resolve[Quux] != Container.resolve[Quux])
    }

    it("should throw exception when use constructor bindings with StringId") {
      assertThrows[RegistryTypeException](Container.register("Qux") := New(classOf[Foo]))
    }

    it("should support toFactory operator") {
      Container.register[DynamicRegistryTest.Bar] ~> factory
      assert(Container.has[DynamicRegistryTest.Bar])
      assert(Container.resolve[DynamicRegistryTest.Bar].isInstanceOf[DynamicRegistryTest.Bar])
      val obj = Container.resolve[DynamicRegistryTest.Bar]
      assert(obj.isInstanceOf[DynamicRegistryTest.Bar] && obj.x == DynamicRegistryTest.barX &&
        obj.y == DynamicRegistryTest.barY)
    }

    it("should support toService operator") {
      Container.register("BarService") := classOf[Bar]
      Container.register("AnotherBar") >> "BarService"

      assert(Container.has("BarService"))
      assert(Container.has("AnotherBar"))
      assert(Container.resolve[Bar]("BarService") == Container.resolve[Bar]("AnotherBar"))
    }
  }
}

object OperatorsTest {
  class Foo
  class Bar
  class Baz
  class Qux(val foo: Foo) {
    val x = 0
  }
  class Quux(val x: Int) {
    def this(qux: Qux) = this(qux.x)
  }
}
