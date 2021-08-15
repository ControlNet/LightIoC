package space.controlnet.lightioc

import org.scalatest.FunSpec
import space.controlnet.lightioc.annotation.{ Autowired, Provider, Singleton }

class AutowiredTest extends FunSpec {

  import AutowiredTest._

  val intValue = 100
  val strValue = "Hello IOC"
  val boolValue = true
  Container.register[Int]("Bar.num").toValue(intValue).inSingletonScope.done()
  Container.register[Boolean]("Baz.bool").toValue(boolValue).inSingletonScope.done()
  Container.register[String]("Foo.str").toValue(strValue).inSingletonScope.done()
  Container.register[Qux].toSelf.inTransientScope.done()

  describe("Autowired test") {
    it("should autowired string identifier fields") {
      assert(Container.resolve[Bar].num == intValue)
      assert(Container.resolve[Baz.type].bool == boolValue)
    }

    it("should autowired fields in class") {
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
  }
}


object AutowiredTest {

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
    @Autowired var qux: Qux = null
    @Autowired(stringId = "Baz.bool") var bool: Boolean = _
  }

  class Qux

}