package com.arekbednarz.utils;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.arekbednarz.utils.TestContainerNetworks.NETWORK;


/**
 * This is the Postgres container starter. It starts the Postgres and exposes its interfaces through
 * the localhost mapped ports. Check the console output for the specific URLs.
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostgresqlTestContainer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final Logger LOG = Logger.getLogger(PostgresqlTestContainer.class);

	private static final String IMAGE = "postgres:16.0";
	private static final String USERNAME = "postgres";
	private static final String PASSWORD = "secret";
	private static final String NETWORK_ALIAS = "postgres.docker";
	private static final String DATABASE_NAME = "movierental";

	@Container
	private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(IMAGE)
		.withUsername(USERNAME)
		.withPassword(PASSWORD)
		.withNetwork(NETWORK)
		.withDatabaseName(DATABASE_NAME)
		.withNetworkAliases(NETWORK_ALIAS)
		.withExposedPorts(5432)
		.waitingFor(Wait.forListeningPort())
		.withReuse(false)
		.withCommand("postgres", "-c", "max_connections=2000", "-c", "shared_buffers=256MB", "-c", "work_mem=16MB", "-c", "maintenance_work_mem=64MB");

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {

		LOG.infof("Starting PostgreSQL container, this may take a while...");
		container.start();
		LOG.infof("STARTED PostgreSQL container");

		var jdbcUrl = container.getJdbcUrl();
		var username = container.getUsername();
		var password = container.getPassword();

		final var properties = TestPropertyValues.of(
			"spring.datasource.url=" + jdbcUrl,
			"spring.datasource.username=" + username,
			"spring.datasource.password=" + password);
		properties.applyTo(applicationContext.getEnvironment());
	}
}
