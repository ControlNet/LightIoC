package space.controlnet.lightioc.main

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec
import space.controlnet.lightioc.conf.DisableAllString
import space.controlnet.lightioc.{ AutowiredTest, Container, DynamicRegistryTest, OperatorsTest, StaticRegistryTest }

class DisableAllStringIdTest extends AnyFunSpec with BeforeAndAfterAll with DisableAllString
  with DynamicRegistryTest
  with StaticRegistryTest
  with AutowiredTest
  with OperatorsTest {

  override protected def afterAll(): Unit = Container.reset()
}