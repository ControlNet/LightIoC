package space.controlnet.lightioc

import org.scalatest.funspec.AnyFunSpec
import space.controlnet.lightioc.samples.NestedClass.{ InnerClass, InnerObject }
import space.controlnet.lightioc.samples.{ InheritedClass, NamedProviderClass, TopLevelClass, TopLevelObject, TopLevelSingletonClass }

class StaticRegistryTest extends AnyFunSpec {

  Container.init("space.controlnet.lightioc")

  describe("Static registry test ::") {
    it("should registry a top level class by annotation") {
      assert(Container.has[TopLevelClass])
      assert(Container.resolve[TopLevelClass].isInstanceOf[TopLevelClass])
      assert(Container.resolve[TopLevelClass] != Container.resolve[TopLevelClass])
    }

    it("should registry a top level object by annotation") {
      assert(Container.has[TopLevelObject.type])
      assert(Container.resolve[TopLevelObject.type].isInstanceOf[TopLevelObject.type])
      assert(Container.resolve[TopLevelObject.type] == Container.resolve[TopLevelObject.type])
      assert(Container.resolve[TopLevelObject.type] == TopLevelObject)
      assert(Container.resolve[TopLevelObject.type].x == TopLevelObject.x)
    }

    it("should register a inner class by annotation") {
      assert(Container.has[InnerClass])
      assert(Container.resolve[InnerClass].isInstanceOf[InnerClass])
      assert(Container.resolve[InnerClass] != Container.resolve[InnerClass])
    }

    it("should register a inner object by annotation") {
      assert(Container.has[InnerObject.type])
      assert(Container.resolve[InnerObject.type].isInstanceOf[InnerObject.type])
      assert(Container.resolve[InnerObject.type] == Container.resolve[InnerObject.type])
    }

    it("should register a class also provided a String identifier") {
      assert(Container.has(NamedProviderClass.ID))
      assert(Container.resolve[NamedProviderClass](NamedProviderClass.ID).isInstanceOf[NamedProviderClass])
      assert(Container.resolve[NamedProviderClass](NamedProviderClass.ID) !=
        Container.resolve[NamedProviderClass](NamedProviderClass.ID)
      )
    }

    it("should register a singleton class by annotation") {
      assert(Container.has[TopLevelSingletonClass])
      assert(Container.resolve[TopLevelSingletonClass].isInstanceOf[TopLevelSingletonClass])
      assert(Container.resolve[TopLevelSingletonClass] == Container.resolve[TopLevelSingletonClass])
    }

    it("should register a class inherited from other packages") {
      assert(Container.has[InheritedClass])
      assert(Container.resolve[InheritedClass].isInstanceOf[InheritedClass])
      assert(Container.resolve[InheritedClass] != Container.resolve[InheritedClass])
    }
  }
}

