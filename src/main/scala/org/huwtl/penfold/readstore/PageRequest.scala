package org.huwtl.penfold.readstore

case class PageRequest(pageSize: Int, lastKnownPageDetails: Option[LastKnownPageDetails] = None)