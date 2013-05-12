//package com.hlewis.eventfire.app.feed
//
//import org.apache.abdera.protocol.server.impl.AbstractEntityCollectionAdapter
//import com.hlewis.eventfire.domain.{Body, Cron, Header, Job}
//import org.apache.abdera.protocol.server.RequestContext
//import java.util.Date
//import java.util
//import org.apache.abdera.model._
//import org.apache.abdera.i18n.iri.IRI
//import scala.collection.JavaConversions._
//import java.util.concurrent.atomic.AtomicInteger
//import scala.collection.mutable
//import com.hlewis.eventfire.domain.Header
//import com.hlewis.eventfire.domain.Body
//import com.hlewis.eventfire.domain.Job
//import com.hlewis.eventfire.domain.Cron
//
//class JobCollectionAdapter extends AbstractEntityCollectionAdapter[Job] {
//
//  private val nextId = new AtomicInteger(1000)
//
//  private val jobs = mutable.Map[String, Job](("key", Job(Header("key", "test", Cron("1", "10", "*", "*", "*"), Map()), Body(Map("data" -> "value")))))
//
//  override def getId(request: RequestContext) = "pending-jobs-feed"
//
//  override def getTitle(request: RequestContext) = "Pending jobs"
//
//  override def getAuthor(request: RequestContext) = null
//
//  override def addFeedDetails(feed: Feed, request: RequestContext) {
//    super.addFeedDetails(feed: Feed, request: RequestContext)
//    feed.getEntries.toList.filter(_.getContentElement != null).foreach(entry => entry.setContentElement(null))
//  }
//
//  override def getEntries(request: RequestContext) = {
//    println (".........................")
//    asJavaCollection(jobs.values)
//  }
//
//  override def addEntryDetails(request: RequestContext, e: Entry, feedIri: IRI, entryObj: Job) = {
//    e.addLink("/alternative", "alternate")
//    super.addEntryDetails(request: RequestContext, e: Entry, feedIri: IRI, entryObj: Job)
//  }
//
//  override def buildGetEntryResponse(request: RequestContext, entry: Entry) = {
//    val response = super.buildGetEntryResponse(request, entry)
//    entry.setSource(null)
//    response
//  }
//
//  override def getEntry(resourceName: String, request: RequestContext) = {
//    jobs.get(resourceName).getOrElse(null)
//  }
//
//  override def getId(entry: Job) = entry.header.reference
//
//  override def getName(entry: Job) = entry.header.reference
//
//  override def getTitle(entry: Job) = "Job"
//
//  override def getUpdated(entry: Job) = new Date()
//
//  override def getContent(entry: Job, request: RequestContext) = {
//    val content = request.getAbdera.getFactory.newContent(Content.Type.TEXT)
//    content.setValue(entry.body.data.toString())
//    content
//  }
//
//  override def postEntry(title: String, id: IRI, summary: String, updated: Date, authors: util.List[Person], content: Content, request: RequestContext) = {
//    val job = Job(Header(nextId.getAndIncrement.toString, "test", Cron("1", "10", "*", "*", "*"), Map()), Body(Map("data" -> content.getText.trim)))
//    jobs + (job.header.reference -> job)
//    job
//  }
//
//  override def deleteEntry(resourceName: String, request: RequestContext) {
//    jobs - (resourceName)
//  }
//
//  override def putEntry(entry: Job, title: String, updated: Date, authors: util.List[Person], summary: String, content: Content, request: RequestContext) {
//    jobs + (entry.header.reference -> entry)
//  }
//}
