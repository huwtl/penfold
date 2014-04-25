package org.huwtl.penfold.domain.patch

case class Patch(operations: List[PatchOperation]) {
  def exec(existing: Map[String, Any]): Map[String, Any] = {
    operations.foldLeft(existing)((lastApplied, nextOp) => nextOp.exec(lastApplied))
  }
}
