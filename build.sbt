//
// Copyright 2014 GoDataDriven B.V.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

organization  := "io.divolte"
name          := "divolte-spark"
version       := "0.2-SNAPSHOT"
homepage      := Some(url("https://github.com/divolte/divolte-schema"))
licenses      := Seq("The Apache License, Version 2.0" ->
                     url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
scalaVersion  := "2.10.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-target:jvm-1.7", "-feature")

// Experimental: improved incremental compilation.
incOptions    := incOptions.value.withNameHashing(nameHashing = true)

// Enable during development to access local maven artifacts.
//resolvers += Resolver.mavenLocal

// These dependencies assume many are supplied by the Spark execution container.
// TODO: How do we go about different Hadoop version deps?
// Should we provide a env var option for this?
val sparkV = "1.2.0"
val hadoopV = "2.4.0"
val avroV = "1.7.7"

libraryDependencies += "org.apache.spark"  %% "spark-core"            % sparkV  % Provided
libraryDependencies += "org.apache.spark"  %% "spark-streaming"       % sparkV  % Provided
libraryDependencies += "org.apache.spark"  %% "spark-streaming-kafka" % sparkV  % Provided
libraryDependencies += "org.apache.hadoop" %  "hadoop-client"         % hadoopV % Provided
libraryDependencies += "org.apache.avro"   %  "avro"                  % avroV
libraryDependencies += "org.apache.avro"   %  "avro-mapred"           % avroV classifier "hadoop2" excludeAll
  ExclusionRule(organization = "org.apache.avro", name = "avro-ipc")
libraryDependencies += "io.divolte"        %  "divolte-schema"        % "0.1"

assemblySettings

// We're publishing to Sonatype's OSSRH.
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

// OSSRH is maven-based.
publishMavenStyle := true

// Ensure that test artifacts are never published.
publishArtifact in Test := false

// Ensure that the repositories for any optional dependencies are not included.
pomIncludeRepository := { _ => false }

// Required metadata for publication to OSSRH.
pomExtra :=
  <inceptionYear>2014</inceptionYear>
  <organization>
    <name>GoDataDriven B.V.</name>
    <url>http://godatadriven.com/</url>
  </organization>
  <scm>
    <connection>scm:git:git@github.com:divolte/divolte-schema.git</connection>
    <developerConnection>scm:git:git@github.com:divolte/divolte-schema.git</developerConnection>
    <url>git@github.com:divolte/divolte-schema.git</url>
    <tag>HEAD</tag>
  </scm>
  <developers>
    <developer>
      <name>Friso van Vollenhoven</name>
      <email>frisovanvollenhoven@godatadriven.com</email>
      <organization>GoDataDriven B.V.</organization>
      <organizationUrl>http://godatadriven.com</organizationUrl>
    </developer>
    <developer>
      <name>Andrew Snare</name>
      <email>andrewsnare@godatadriven.com</email>
      <organization>GoDataDriven B.V.</organization>
      <organizationUrl>http://godatadriven.com</organizationUrl>
    </developer>
    <developer>
      <name>Kris Geusebroek</name>
      <email>krisgeusebroek@godatadriven.com</email>
      <organization>GoDataDriven B.V.</organization>
      <organizationUrl>http://godatadriven.com</organizationUrl>
    </developer>
  </developers>
