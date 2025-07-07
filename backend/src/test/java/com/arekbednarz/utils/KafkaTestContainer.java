package com.arekbednarz.utils;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import static com.arekbednarz.utils.TestContainerNetworks.NETWORK;


@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaTestContainer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final Logger LOG = Logger.getLogger(KafkaTestContainer.class);

	private static final String NETWORK_ALIAS = "kafka.docker";

	private static DockerImageName image = DockerImageName.parse("apache/kafka:3.7.0")
		.asCompatibleSubstituteFor("apache/kafka");

	@Container
	private static final KafkaContainer container = new KafkaContainer(image)
		.withNetwork(NETWORK)
		.withNetworkAliases(NETWORK_ALIAS)
		.withExposedPorts(9092)
		.withReuse(false);

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		LOG.infof("Starting Kafka container...");
		container.start();
		LOG.infof("STARTED Kafka container");

		String bootstrapServers = container.getBootstrapServers();

		TestPropertyValues.of(
			"spring.kafka.bootstrap-servers=" + bootstrapServers).applyTo(applicationContext.getEnvironment());
	}
}
