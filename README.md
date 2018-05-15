kafka-consumer-groups.sh --bootstrap-server localhost:9092 --reset-offsets --group m2n-resolver2 --all-topics --to-earliest --execute

kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group m2n-resolver2

kafka-run-class.sh kafka.tools.GetOffsetShell  --broker-list localhost:9092 --topic betreuer-source 

kafka-topics.sh --zookeeper localhost:2181 --list

https://zz85.github.io/kafka-streams-viz/

