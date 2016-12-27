(ns yuubin.kafka
  (:import [org.apache.kafka.clients.consumer KafkaConsumer]))

;; TODO Remove randomness after testing.
(def default-consumer-config
  {"group.id" (str "yuubin-" (rand-int 10000))
   "auto.offset.reset" "earliest"
   "enable.auto.commit" "false"
   "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"
   "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"})

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
