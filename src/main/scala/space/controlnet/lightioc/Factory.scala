package space.controlnet.lightioc


trait Factory[A, B] {
  def apply(xs: A*): B = {
    val obj = call(xs: _*)
    Container.autowire[B](obj)
  }
  def call(xs: A*): B
}


object Factory {
  type *=>[A, B] = Factory[A, B]
}
