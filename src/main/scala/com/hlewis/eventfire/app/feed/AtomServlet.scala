package com.hlewis.eventfire.app.feed

import org.apache.abdera.protocol.server.impl.{SimpleWorkspaceInfo, DefaultProvider}
import org.apache.abdera.protocol.server.servlet.AbderaServlet

class AtomServlet extends AbderaServlet {
  override def createProvider() = {
    val ca = new JobCollectionAdapter()
    ca.setHref("job")

    val wi = new SimpleWorkspaceInfo()
    wi.setTitle("Job Directory Workspace")
    wi.addCollection(ca)

    val provider = new DefaultProvider("/feed/")
    provider.addWorkspace(wi)

    provider.init(getAbdera, null)
    provider
  }
}
