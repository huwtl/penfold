import AssemblyKeys._ // put this at the top of the file

assemblySettings

jarName in assembly := "penfold.jar"

// your assembly settings here
mainClass in assembly := Some("com.qmetric.penfold.app.Main")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
    case PathList("about.html") => MergeStrategy.rename
    case x => old(x)
  }
}

// copy web resources to /webapp folder
resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map {
  (managedBase, base) =>
    val webappBase = base / "src" / "main" / "webapp"
    for {
      (from, to) <- webappBase ** "*" x rebase(webappBase, managedBase / "main" / "webapp")
    } yield {
      Sync.copy(from, to)
      to
    }
}

addArtifact(artifact in (Compile, assembly), assembly)
