# start producer
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test

# start consumer
bin/kafka-consolve-consumer.sh --BootstrapServers localhost:9092 --topic test

# start producer with key/value parser
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test --property "parse.key=true" --property "key.separator=:"

# send stream data in via producer
for (( i=1; i<=10; i++ )); do echo "msg $i" | kafka-console-producer --broker-list localhost:9092 --topic test; done;
