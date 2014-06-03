package com.qmetric.penfold.readstore

case class PageRequest(pageSize: Int, pageReference: Option[PageReference] = None)