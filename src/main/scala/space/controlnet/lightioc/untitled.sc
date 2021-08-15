import scala.collection.mutable

val map = mutable.Map("abc" -> 1)
map.remove("bcd")

map += "bcd" -> 2

map.remove("bcd")