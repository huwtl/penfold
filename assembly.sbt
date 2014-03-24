import AssemblyKeys._ // put this at the top of the file

assemblySettings

jarName in assembly := "penfold.jar"

// your assembly settings here
mainClass in assembly := Some("org.huwtl.penfold.app.Main")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
    case PathList("about.html") => MergeStrategy.rename
    case x => old(x)
  }
}

artifact in (Compile, assembly) ~= { art =>
  art.copy(`classifier` = Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)
