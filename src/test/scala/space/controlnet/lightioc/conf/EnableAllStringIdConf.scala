package space.controlnet.lightioc.conf

import space.controlnet.lightioc.Container

trait EnableAllStringIdConf {
  Container.init("space.controlnet.lightioc", allStringId = true)
}
