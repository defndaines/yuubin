(ns yuubin.kafka
  (:import [org.apache.kafka.clients.consumer KafkaConsumer]
           [org.apache.kafka.clients.producer KafkaProducer ProducerRecord]))

(def default-consumer-config
  {"group.id" "yuubin-consumer"
   "auto.offset.reset" "earliest"
   "enable.auto.commit" "false"
   "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"
   "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"})

(def default-producer-config
  {"key.serializer" "org.apache.kafka.common.serialization.StringSerializer"
   "value.serializer" "org.apache.kafka.common.serialization.StringSerializer"})

(defn consumer
  "Initialize a Kafka Consumer with configuration and subscribed to the topic."
  [config topic]
  (doto (KafkaConsumer. config)
    (.subscribe [topic])))

(defn read-topic
  "Read (forever) from a consumer, passing each value through to the process function."
  [consumer process-fn]
  (while true
    (let [records (.poll consumer 200)]
      (doseq [record records]
        (process-fn (.value record))))
    (.commitSync consumer)))

(defn producer
  "Initialize a Kafka Producer with configuration."
  [config]
  (KafkaProducer. config))

(defn write-topic
  "Write a single value to a topic using the provided producer."
  [producer topic value]
  (.send producer (ProducerRecord. topic value)))
