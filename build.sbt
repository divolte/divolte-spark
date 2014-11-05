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

import AssemblyKeys._

organization  := "io.divolte"

name          := "divolte-spark"

version       := "0.1-SNAPSHOT"

scalaVersion  := "2.10.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-target:jvm-1.7", "-feature")

// Experimental: improved incremental compilation.
incOptions    := incOptions.value.withNameHashing(nameHashing = true)

// Enable during development to access local maven artifacts.
//resolvers += Resolver.mavenLocal

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

libraryDependencies += "io.divolte"        %  "divolte-schema"        % "0.1"

assemblySettings
