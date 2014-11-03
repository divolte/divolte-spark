import AssemblyKeys._

organization  := "io.divolte"

name          := "divolte-spark"

version       := "0.1-SNAPSHOT"

scalaVersion  := "2.10.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-target:jvm-1.7", "-feature")

// Experimental: improved incremental compilation.
incOptions    := incOptions.value.withNameHashing(nameHashing = true)

// These dependencies assume many are supplied by the Spark execution container.
// TODO: How do we go about different Hadoop version deps?
// Should we provide a env var option for this?
val sparkV = "1.1.0"
val hadoopV = "2.3.0"
val avroV = "1.7.7"

libraryDependencies += "org.apache.spark"  %% "spark-core"            % sparkV  % "provided"

libraryDependencies += "org.apache.spark"  %% "spark-streaming"       % sparkV  % "provided"

libraryDependencies += "org.apache.spark"  %% "spark-streaming-kafka" % sparkV  % "provided"

libraryDependencies += "org.apache.hadoop" %  "hadoop-client"         % hadoopV % "provided"

libraryDependencies += "org.apache.avro"   %  "avro"                  % avroV

libraryDependencies += "org.apache.avro"   %  "avro-mapred"           % avroV classifier "hadoop2" excludeAll
  ExclusionRule(organization = "org.apache.avro", name = "avro-ipc")

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList(ps @ _*) if ps.last endsWith "pom.properties" => MergeStrategy.discard
    case x => old(x)
  }
}
