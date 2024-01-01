package streaming

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.Path
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.{BasePathBucketAssigner, SimpleVersionedStringSerializer}
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.{SlidingEventTimeWindows, SlidingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time

import java.util.concurrent.TimeUnit

class AverageAggregate extends AggregateFunction[St, ((String, Double), Int), (String, Double)] {
  override def createAccumulator(): ((String, Double), Int) = (("", 0.0), 0)
  override def add(value: St, accumulator: ((String, Double), Int)): ((String, Double), Int) = ((value.id, accumulator._1._2 + value.price), accumulator._2 + 1)
  override def getResult(accumulator: ((String, Double), Int)): (String, Double) = (accumulator._1._1, accumulator._1._2 / accumulator._2)
  override def merge(a: ((String, Double), Int), b: ((String, Double), Int)): ((String, Double), Int) = ((a._1._1, a._1._2 + b._1._2), a._2 + b._2)
}
object SlidingWindow {
  def main(args: Array[String]): Unit = {
    // Load application.conf in resources
    val config: Config = ConfigFactory.load()
    // set up the execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    class CustomBasePathBucketAssigner[T](basePath: String) extends BasePathBucketAssigner[T]() {
      override def getBucketId(in: T, context: BucketAssigner.Context): String = {
        // get a unique path for each element in the data stream
        val element = in.asInstanceOf[(String, Double)]
        val name = element._1
        s"$basePath\\$name"
      }

      override def getSerializer: SimpleVersionedStringSerializer = {
        SimpleVersionedStringSerializer.INSTANCE
      }
    }
    val sink: StreamingFileSink[(String, Double)] = StreamingFileSink
      .forRowFormat(new Path(config.getString("OUTPUT_FOLDER")), new SimpleStringEncoder[(String, Double)]("UTF-8"))
      .withBucketAssigner(new CustomBasePathBucketAssigner(basePath = config.getString("OUTPUT_FOLDER") + "\\output"))
      .withRollingPolicy(
        DefaultRollingPolicy.builder()
          .withRolloverInterval(TimeUnit.MINUTES.toMillis(15))
          .withInactivityInterval(TimeUnit.MINUTES.toMillis(5))
          .withMaxPartSize(1024 * 1024 * 1024)
          .build())
      .build()


    val dataStream: DataStream[St] = env.addSource(new RapidApi)
    dataStream
      .keyBy( _.id )
      .window(SlidingProcessingTimeWindows.of(Time.seconds(10), Time.seconds(5)))
      .aggregate(new AverageAggregate)
      .addSink(sink)

    // execute program
    env.execute("Flink Scala API")
  }
}
