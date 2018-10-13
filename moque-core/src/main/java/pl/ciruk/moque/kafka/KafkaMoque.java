package pl.ciruk.moque.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;

public class KafkaMoque {
    private final KafkaProducer<String, String> producer;
    private final KafkaConsumer<String, String> consumer;

    public KafkaMoque(Properties properties) {
        producer = new KafkaProducer<>(properties);
        consumer = new KafkaConsumer<>(properties);
    }
}
