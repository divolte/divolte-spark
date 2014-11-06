Divolte Spark
=============

This project contains some helpers for working with data collected by the
[Divolte Collector][1] using [Apache Spark][2].

QuickStart
----------

### Python

To use the helpers from PySpark you need to build a JAR containing the
helper classes and dependencies. As a prerequisite, you need to have
[SBT][3] installed.

    % git clone https://github.com/divolte/divolte-spark.git
    % cd divolte-spark
    % sbt assembly
    % export DIVOLTE_SPARK_JAR="$PWD"/target/scala-*/divolte-spark-assembly-*.jar

This will result in the JAR being placed in the current directory.

You can then start the PySpark REPL:

    % pyspark --jars "$DIVOLTE_SPARK_JAR" --driver-class-path "$DIVOLTE_SPARK_JAR"

To access the Divolte events that use the default schema you can use:

```python
# Assuming 'sc' is the Spark Context…
events = sc.newAPIHadoopFile(
    "hdfs:///path/to/avro/files/*.avro",
    'org.apache.avro.mapreduce.AvroKeyInputFormat',
    'org.apache.avro.mapred.AvroKey',
    'org.apache.hadoop.io.NullWritable',
    keyConverter='io.divolte.spark.pyspark.avro.AvroWrapperToJavaConverter').map(lambda (k,v): k)
# 'events' is now an RDD containing the events in the matching Avro files.
```

When using `spark-submit` to submit jobs, the JAR needs to be passed to
using *both* the `--jars` and `--driver-class-path` options.

### Scala

If building a Spark application, you can add Divolte Spark to your
dependencies by adding the following line to your SBT build:

    libraryDependencies += "io.divolte" %% "divolte-spark" % "0.1"

To load Divolte events as a Spark RDD:

```scala
import io.divolte.spark.avro._
import org.apache.avro.generic.IndexedRecord
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

val sc = new SparkContext()
val events = sc.newAvroFile[IndexedRecord](path)
```

At this point you can either elect to process the events as full
`Record` instances, or just extract specific fields for further processing:

```scala
val records = events.toRecords
// or
val eventFields = events.fields("sessionId", "location", "timestamp")
```

#### Spark Streaming

We also provide some helpers for using Spark Streaming to process events published
by Divolte via [Kafka][4]. This requires some additional dependencies:

    libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka" % sparkV excludeAll(
      ExclusionRule(organization = "org.apache.spark", name = "spark-streaming_2.10"),
      ExclusionRule(organization = "javax.jms")
    )

    libraryDependencies += "org.apache.kafka" %% "kafka" % "0.8.1.1" excludeAll(
      ExclusionRule(organization = "com.sun.jdmk"),
      ExclusionRule(organization = "com.sun.jmx"),
      ExclusionRule(organization = "javax.jms"),
      ExclusionRule(organization = "log4j")
    )

To process Divolte events from Kafka:

```scala

// Kafka configuration.
val consumerConfig = Map(
  "group.id"                -> "some-id-for-the-consumer-group",
  "zookeeper.connect"       -> "zookeeper-connect-string",
  "auto.commit.interval.ms" -> "5000",
  "auto.offset.reset"       -> "largest"
)
val topicSettings = Map("divolte" -> Runtime.getRuntime.availableProcessors())

val sc = new SparkContext()
val ssc = new StreamingContext(sc, Seconds(15))

// Establish the source event stream.
val stream = ssc.divolteStream[GenericRecord](consumerConfig, topicSettings, StorageLevel.MEMORY_ONLY)
```

As above, you then need to choose whether to process complete records or
just extract some specific fields:

```scala
val eventStream = stream.toRecords
// or
val locationStream = stream.fields("location")
```

The DStream contains key-value pairs where the key is the party ID
associated with the event, and the value is the event itself (or extracted
fields).

Examples
--------

Further examples demonstrating how to use Divolte Spark can be found in the
[Divolte Examples][5] project under the `spark/` and `pyspark` directories.

License
-------

This project and its artifacts are licensed under the terms of the [Apache
License, Version 2.0][6].

  [1]: divolte/divolte-collector                        "Divolte Collector"
  [2]: http://spark.apache.org/                         "Apache Spark"
  [3]: http://www.scala-sbt.org/                        "SBT"
  [4]: http://kafka.apache.org/                         "Apache Kafka"
  [5]: divolte/divolte-examples                         "Divolte Examples"
  [6]: http://www.apache.org/licenses/LICENSE-2.0.html  "Apache License 2.0"
