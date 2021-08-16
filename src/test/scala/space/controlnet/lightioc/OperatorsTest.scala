package space.controlnet.lightioc

import org.scalatest.funspec.AnyFunSpec
import space.controlnet.lightioc.BindingSetter.Self
import space.controlnet.lightioc.DynamicRegistryTest.factory
import space.controlnet.lightioc.exception.ResolveTypeException

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

    it("should support binding for self") {
      Container.register[Baz] -> Self
      assert(Container.resolve[Baz].isInstanceOf[Baz])
    }

    it("should support toFactory operator") {
      Container.register[DynamicRegistryTest.Bar] ~> factory
      assert(Container.has[DynamicRegistryTest.Bar])
      assertThrows[ResolveTypeException.type](Container.resolve[DynamicRegistryTest.Bar])
      assert(Container.resolveFactory[DynamicRegistryTest.Bar].isInstanceOf[factory.type])
      val obj = Container.resolveFactory[DynamicRegistryTest.Bar].apply(1, 2)
      assert(obj.isInstanceOf[DynamicRegistryTest.Bar] && obj.x == 1 && obj.y == 2)
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
}
