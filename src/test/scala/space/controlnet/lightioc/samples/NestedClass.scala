package space.controlnet.lightioc.samples

import space.controlnet.lightioc.annotation.Provider

@Provider(isObject = true)
object NestedClass {

  @Provider
  class InnerClass

  @Provider(isObject = true)
  object InnerObject
}
