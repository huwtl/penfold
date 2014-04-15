package org.huwtl.penfold.readstore

case class PageRequest(pageSize: Int, pageReference: Option[PageReference] = None)