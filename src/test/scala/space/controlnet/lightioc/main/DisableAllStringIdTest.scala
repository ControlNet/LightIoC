package space.controlnet.lightioc.main

import org.scalatest.funspec.AnyFunSpec
import space.controlnet.lightioc.conf.DisableAllStringIdConf
import space.controlnet.lightioc.{ AutowiredTest, DynamicRegistryTest, OperatorsTest, StaticRegistryTest }

class DisableAllStringIdTest extends AnyFunSpec with DisableAllStringIdConf
  with DynamicRegistryTest
  with StaticRegistryTest
  with AutowiredTest
  with OperatorsTest
