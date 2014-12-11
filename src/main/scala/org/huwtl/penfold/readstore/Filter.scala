package org.huwtl.penfold.readstore

import org.huwtl.penfold.readstore.QueryParamType.{NumericType, StringType}

trait Filter {
  val key: String
  val dataType: QueryParamType = StringType
}

case class EQ(key: String, value: String, override val dataType: QueryParamType = StringType) extends Filter

case class LT(key: String, value: String, override val dataType: QueryParamType = NumericType) extends Filter

case class GT(key: String, value: String, override val dataType: QueryParamType = NumericType) extends Filter

case class IN(key: String, values: Set[String], override val dataType: QueryParamType = StringType) extends Filter