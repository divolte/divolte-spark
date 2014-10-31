organization  := "io.divolte"

name          := "divolte-spark"

version       := "0.1"

scalaVersion  := "2.10.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-target:jvm-1.7", "-feature")

// Experimental: improved incremental compilation.
incOptions    := incOptions.value.withNameHashing(true)

resolvers += "CDH" at "https://repository.cloudera.com/artifactory/cloudera-repos/"

// These dependencies assume many are supplied by the Spark execution container.
// TODO: How do we go about different Hadoop version deps?
// Should we provide a env var option for this?
val sparkV = "1.1.0"
val hadoopV = "2.3.0"
val avroV = "1.7.7"

libraryDependencies += "org.apache.spark"       %% "spark-core"            % sparkV  % "provided"

libraryDependencies += "org.apache.spark"       %% "spark-streaming"       % sparkV  % "provided"

libraryDependencies += "org.apache.spark"       %% "spark-streaming-kafka" % sparkV  % "provided"

libraryDependencies += "org.apache.hadoop"      %  "hadoop-client"       % hadoopV % "provided"

libraryDependencies += "org.apache.avro"        %  "avro"                  % avroV

libraryDependencies += "org.apache.avro"        %  "avro-mapred"           % avroV classifier "hadoop2" excludeAll(
    ExclusionRule(organization = "io.netty", name = "netty"),
    ExclusionRule(organization = "org.jboss.netty"),
    ExclusionRule(organization = "org.mortbay.jetty"),
    ExclusionRule(organization = "org.apache.velocity")
  )
