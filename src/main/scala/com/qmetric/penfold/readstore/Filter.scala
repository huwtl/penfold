package com.qmetric.penfold.readstore

object Filter {
  def apply(key: String, value: Option[String]): Filter = Filter(key, Set(value))
}

case class Filter(key: String, values: Set[Option[String]]) {
  def isMulti = values.size > 1
}
