package com.hlewis.eventfire.app.feed

import org.apache.abdera.protocol.server.impl.AbstractEntityCollectionAdapter
import com.hlewis.eventfire.domain.{Body, Cron, Header, Job}
import org.apache.abdera.protocol.server.RequestContext
import java.util.Date
import java.util
import org.apache.abdera.model.{Content, Person}
import org.apache.abdera.i18n.iri.IRI
import scala.collection.JavaConversions.asJavaCollection
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable

class JobCollectionAdapter extends AbstractEntityCollectionAdapter[Job] {

  private val nextId = new AtomicInteger(1000)

  private val jobs = mutable.Map[String, Job](("key", Job(Header("key", "test", Cron("1", "10", "*", "*", "*"), Map()), Body(Map("data" -> "value")))))

  override def getId(request: RequestContext) = "pending-jobs-feed"

  override def getTitle(request: RequestContext) = "Pending jobs"

  override def getAuthor(request: RequestContext) = "event-fire"

  override def getEntries(request: RequestContext) = asJavaCollection(jobs.values)

  override def getEntry(resourceName: String, request: RequestContext) = {
    jobs.get(resourceName).getOrElse(null)
  }

  override def getId(entry: Job) = entry.header.reference

  override def getName(entry: Job) = entry.header.reference

  override def getTitle(entry: Job) = "Job"

  override def getUpdated(entry: Job) = new Date()

  override def getAuthors(entry: Job, request: RequestContext) = {
    val author = request.getAbdera.getFactory.newAuthor()
    author.setName("Some author")
    util.Arrays.asList(author)
  }

  override def getContent(entry: Job, request: RequestContext) = entry.body.data.toString()

  override def postEntry(title: String, id: IRI, summary: String, updated: Date, authors: util.List[Person], content: Content, request: RequestContext) = {
    val job = Job(Header(nextId.getAndIncrement.toString, "test", Cron("1", "10", "*", "*", "*"), Map()), Body(Map("data" -> content.getText.trim)))
    jobs + (job.header.reference -> job)
    job
  }

  override def deleteEntry(resourceName: String, request: RequestContext) {
    jobs - (resourceName)
  }

  override def putEntry(entry: Job, title: String, updated: Date, authors: util.List[Person], summary: String, content: Content, request: RequestContext) {
    jobs + (entry.header.reference -> entry)
  }
}
