package space.controlnet.lightioc

import space.controlnet.lightioc.enumerate.{ ConstructorEntry, Entry, FactoryEntry, Identifier, Scope, ServiceEntry, Singleton, Transient, ValueEntry }

private sealed abstract class ScopeSetter[T](val identifier: Identifier)

private sealed trait ScopeSettable {
  def inSingletonScope: EntryBuildable[_]
  def inTransientScope: EntryBuildable[_]
}

private sealed trait HasScope {
  def scope: Scope
}

private sealed trait SingletonScope extends HasScope {
  def scope: Scope = Singleton
}

private sealed trait TransientScope extends HasScope {
  def scope: Scope = Transient
}

private sealed trait HasValue {
  val value: Any
}

private sealed trait ValueEntryHasValue[T] extends HasValue {
  val value: () => T
}

private sealed trait ConstructorEntryHasValue[T] extends HasValue {
  val value: Seq[Class[_]]
}

private sealed trait FactoryEntryHasValue[T] extends HasValue {
  val value: Container.type => T
}

private sealed trait EntryBuildable[T] {
  this: ScopeSetter[T] =>
  protected def entry: Entry[T]
  private def build(): Unit = Container += identifier -> entry
  def done(): Container.type = {
    build()
    Container
  }
}

private sealed trait ValueEntryBuildable[T] extends EntryBuildable[T]  {
  this: ScopeSetter[T] with HasScope with ValueEntryHasValue[T] =>
  protected def entry: Entry[T] = new ValueEntry[T](identifier, scope, value)
}

private sealed trait ConstructorEntryBuildable[T] extends EntryBuildable[T] {
  this: ScopeSetter[T] with HasScope with ConstructorEntryHasValue[T] =>
  protected def entry: Entry[T] = new ConstructorEntry[T](identifier, scope, value)
}

private sealed trait FactoryEntryBuildable[T] extends EntryBuildable[T] {
  this: ScopeSetter[T] with HasScope with FactoryEntryHasValue[T] =>
  protected def entry: Entry[T] = new FactoryEntry[T](identifier, scope, value)
}

private[lightioc] sealed class ValueScopeSetter[T](identifier: Identifier, val value: () => T) extends ScopeSetter[T](identifier) with ScopeSettable with ValueEntryHasValue[T] {
  override def inSingletonScope: ValueScopeSetter[T] with SingletonScope with ValueEntryBuildable[T] = new ValueScopeSetter[T](identifier, value) with SingletonScope with ValueEntryBuildable[T]
  override def inTransientScope: ValueScopeSetter[T] with TransientScope with ValueEntryBuildable[T] = new ValueScopeSetter[T](identifier, value) with TransientScope with ValueEntryBuildable[T]
}

private[lightioc] sealed class ConstructorScopeSetter[T](identifier: Identifier, val value: Seq[Class[_]]) extends ScopeSetter[T](identifier) with ScopeSettable with ConstructorEntryHasValue[T] {
  override def inSingletonScope: ConstructorScopeSetter[T] with SingletonScope with ConstructorEntryBuildable[T] = new ConstructorScopeSetter[T](identifier, value) with SingletonScope with ConstructorEntryBuildable[T]
  override def inTransientScope: ConstructorScopeSetter[T] with TransientScope with ConstructorEntryBuildable[T] = new ConstructorScopeSetter[T](identifier, value) with TransientScope with ConstructorEntryBuildable[T]
}

private[lightioc] sealed class FactoryScopeSetter[T](identifier: Identifier, val value: Container.type => T) extends ScopeSetter[T](identifier) with ScopeSettable with FactoryEntryHasValue[T] {
  override def inSingletonScope: FactoryScopeSetter[T] with SingletonScope with FactoryEntryBuildable[T] = new FactoryScopeSetter[T](identifier, value) with SingletonScope with FactoryEntryBuildable[T]
  override def inTransientScope: FactoryScopeSetter[T] with TransientScope with FactoryEntryBuildable[T] = new FactoryScopeSetter[T](identifier, value) with TransientScope with FactoryEntryBuildable[T]
}

private[lightioc] final class ServiceScopeSetter[T, R](identifier: Identifier, val targetIdentifier: Identifier) extends ScopeSetter[T](identifier) with EntryBuildable[T] {
  protected def entry: ServiceEntry[T, R] = ServiceEntry(identifier, targetScope, targetIdentifier)
  private def targetScope = Container.getEntry(targetIdentifier).scope
}

