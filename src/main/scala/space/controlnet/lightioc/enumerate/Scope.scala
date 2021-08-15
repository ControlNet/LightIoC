package space.controlnet.lightioc.enumerate

sealed trait Scope
case object Singleton extends Scope
case object Transient extends Scope
