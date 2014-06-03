package com.qmetric.penfold.domain.model.patch

trait PatchOperation {
  val path: String
  val pathParts = path.split("/").filter(p => p.nonEmpty).toList
  def exec(existing: Map[String, Any]): Map[String, Any]
}
