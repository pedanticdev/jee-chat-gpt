package fish.payara;

import java.time.Duration;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.theokanning.openai.service.OpenAiService;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@DataSourceDefinition(name = "java:app/cloud-postgres",
		className = "org.postgresql.ds.PGSimpleDataSource",
		url = "${MPCONFIG=DB_URL}",
		databaseName = "${MPCONFIG=DB_NAME}",
		serverName = "${MPCONFIG=DB_SERVER}",
		user = "${MPCONFIG=DB_USER}",
		password = "${MPCONFIG=DB_PASSWORD}",
		properties = {"targetServerType=primary"})
public class OpenAIFactory {

	@Produces
	@PersistenceContext
	EntityManager entityManager;
	@Inject
	@ConfigProperty(name = "OPEN_API_KEY")
	private String apiKey;
	@Inject
	@ConfigProperty(name = "openai.timeout")
	private int apiTimeout;

	@Produces
	@Singleton
	public OpenAiService produceService() {
		return new OpenAiService(apiKey,
				Duration.ofSeconds(apiTimeout));
	}

}
