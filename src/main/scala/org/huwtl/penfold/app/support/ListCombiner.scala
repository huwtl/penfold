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

  def combineNamed[T](namedLists: List[(String, List[T])]): List[List[(String, T)]] = {
    val names = namedLists.map(_._1)
    val lists = namedLists.map(_._2)
    val combined = combine[T](lists)
    combined.map(combination => names.zip(combination))
  }
}
