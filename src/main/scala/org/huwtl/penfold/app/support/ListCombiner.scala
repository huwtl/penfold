package org.huwtl.penfold.app.support

object ListCombiner {
  def combine[T](lists: List[List[T]]): List[List[T]] = {
    lists match {
      case Nil => List(Nil)
      case firstList :: remainingLists => {
        for {
          firstElem <- firstList
          combination <- combine(remainingLists)
        } yield firstElem :: combination
      }
    }
  }
}
