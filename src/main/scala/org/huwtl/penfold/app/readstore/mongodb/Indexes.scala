package org.huwtl.penfold.app.readstore.mongodb

case class Indexes(private val customIndexes: List[Index]) {
  private val statusField = IndexField("status")

  private val sortIndexFields = List(IndexField("sort"), IndexField("_id"))

  private val idVersionIndex = Index(List(IndexField("_id"), IndexField("version")))

  private val queueIndex = Index(List(IndexField("queue"), statusField) ::: sortIndexFields)

  private val statusIndex = Index(statusField :: sortIndexFields)

  val all = idVersionIndex :: queueIndex :: statusIndex :: augmentCustomIndexes

  private def augmentCustomIndexes = {
    val augmentedCustomIndexes = for {
      customIndex <- customIndexes
      enhancement <- List(queueIndex, statusIndex)
    } yield Index(enhancement.fields ::: customIndex.fields ::: sortIndexFields)

    customIndexes ::: augmentedCustomIndexes
  }
}
