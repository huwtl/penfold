package com.qmetric.penfold.readstore

import com.qmetric.penfold.readstore.QueryParamType.{NumericType, StringType}

trait Filter {
  val key: String
  val dataType: QueryParamType = StringType
}

case class Equals(key: String, value: String, override val dataType: QueryParamType = StringType) extends Filter

case class LessThan(key: String, value: String, override val dataType: QueryParamType = NumericType) extends Filter

case class GreaterThan(key: String, value: String, override val dataType: QueryParamType = NumericType) extends Filter

case class In(key: String, values: Set[String], override val dataType: QueryParamType = StringType) extends Filter