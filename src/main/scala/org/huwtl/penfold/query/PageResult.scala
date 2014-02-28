package org.huwtl.penfold.query

case class PageResult(jobs: List[JobRecord], earlierExists: Boolean, laterExists: Boolean)
