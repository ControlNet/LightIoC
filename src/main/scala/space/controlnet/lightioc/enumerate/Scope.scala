package space.controlnet.lightioc.enumerate

protected[lightioc] sealed trait Scope
private[lightioc] case object Singleton extends Scope
private[lightioc] case object Transient extends Scope
