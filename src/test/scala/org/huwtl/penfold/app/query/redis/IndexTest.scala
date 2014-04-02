package org.huwtl.penfold.app.query.redis

import org.specs2.mutable.Specification
import org.huwtl.penfold.query.{Filter, Filters}
import org.specs2.matcher.DataTables

class IndexTest extends Specification with DataTables {

  "know when suitable for filters" in {
    "filters"                      | "index"                                | "suitable" |
    Nil                            ! Nil                                    ! true       |
    Nil                            ! List(indexField("a"))                  ! false      |
    List(filter("b"), filter("a")) ! List(indexField("a"), indexField("b")) ! true       |
    List(filter("A"))              ! List(indexField("a"))                  ! true       |
    List(filter("a"))              ! List(indexField("A"))                  ! true       |
    List(filter("b"), filter("a")) ! List(indexField("b"))                  ! false      |
    List(filter("a"))              ! List(indexField("b"))                  ! false      |
    List(filter("b"))              ! List(indexField("a"), indexField("b")) ! false      |> {
      (filters, index, suitable) => Index("", index).suitableFor(Filters(filters)) must beEqualTo(suitable)
    }
  }

  private def indexField(key: String) = IndexField(key, "")
  private def filter(key: String) = Filter(key, "val")
}
