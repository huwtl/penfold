package org.huwtl.penfold.domain.patch

case class Replace(path: String, value: Value) extends PatchOperation{
  override def exec(existing: Map[String, Any]): Map[String, Any] = ???
}
