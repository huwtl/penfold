package com.qmetric.penfold.domain.model.patch

case class Replace(path: String, value: Value) extends PatchOperation{
  override def exec(existing: Map[String, Any]): Map[String, Any] = Add(path, value).exec(Remove(path).exec(existing))
}
