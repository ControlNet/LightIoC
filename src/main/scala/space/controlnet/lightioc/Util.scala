package space.controlnet.lightioc

object Util {
  implicit class AnyExt[T <: Any](any: T) {
    def let[R](f: T => R): R = f(any)
    def |>[R](f: T => R): R = let(f)
  }
  type Factory[T] = Container.type => T
}
