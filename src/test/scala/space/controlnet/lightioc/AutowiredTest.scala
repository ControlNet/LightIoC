package space.controlnet.lightioc

import org.scalatest.funspec.AnyFunSpec
import space.controlnet.lightioc.annotation.{ Autowired, Provider, Singleton }

trait AutowiredTest extends AnyFunSpec {
  import AutowiredTest._

  describe("Autowired test ::") {

    it("should autowired string identifier fields") {
      Container.register[Int]("Bar.num").toValue(intValue).inSingletonScope()
      Container.register[Boolean]("Baz.bool").toValue(boolValue).inSingletonScope()
      Container.register[Qux].toSelf.inTransientScope()
      assert(Container.resolve[Bar].num == intValue)
      assert(Container.resolve[Baz.type].bool == boolValue)
    }

    it("should autowired fields in class") {
      Container.register[String]("Foo.str").toValue(strValue).inSingletonScope()
      assert(Container.resolve[Foo].bar.isInstanceOf[Bar])
      assert(Container.resolve[Foo].baz.isInstanceOf[Baz.type])
      assert(Container.resolve[Foo].baz == Baz)
      assert(Container.resolve[Foo].qux.isInstanceOf[Qux])
      assert(Container.resolve[Bar].baz.isInstanceOf[Baz.type])
      assert(Container.resolve[Bar].baz == Baz)

      assert(Container.resolve[Foo].bar == Container.resolve[Foo].bar)
      assert(Container.resolve[Foo].baz == Container.resolve[Foo].baz)
      assert(Container.resolve[Foo].qux != Container.resolve[Foo].qux)
    }

    it("should autowired fields in object") {
      assert(Container.resolve[Baz.type].qux.isInstanceOf[Qux])
    }

    it("should autowired fields in trail") {
      Container.register[Int]("IQuux.num").toValue(intValue).inSingletonScope()
      assert(Container.resolve[Quux].num == intValue)
    }
  }
}


object AutowiredTest {

  private[lightioc] val intValue = 100
  private[lightioc] val strValue = "Hello IOC"
  private[lightioc] val boolValue = true

  @Provider
  class Foo {
    @Autowired val bar: Bar = null
    @Autowired val baz: Baz.type = null
    @Autowired val qux: Qux = null
    @Autowired(stringId = "Foo.str") val str: String = null
  }

  @Singleton
  class Bar {
    @Autowired val baz: Baz.type = null
    @Autowired(stringId = "Bar.num") val num: Int = 0
  }

  @Provider(isObject = true)
  object Baz {
    @Autowired var qux: Qux = _
    @Autowired(stringId = "Baz.bool") var bool: Boolean = _
  }

  class Qux

  trait IQuux {
    @Autowired(stringId = "IQuux.num") val num: Int = 0
  }

  @Provider
  class Quux extends IQuux
}