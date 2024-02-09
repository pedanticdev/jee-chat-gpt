package fish.payara;

import java.time.Duration;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.theokanning.openai.service.OpenAiService;

import lombok.Getter;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@DataSourceDefinition(name = "java:app/cloud-postgres", className = "org.postgresql.ds.PGSimpleDataSource", url = "${MPCONFIG=db.url}", databaseName = "${MPCONFIG=db.name}", serverName = "${MPCONFIG=db.server}", user = "${MPCONFIG=db.user}", password = "${MPCONFIG=db.password}")
@Named
@Getter
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
	@Inject
	@ConfigProperty(name = "db.url")
	private String dbUrl;
	@Inject
	@ConfigProperty(name = "db.name")
	private String dbName;
	@Inject
	@ConfigProperty(name = "db.server")
	private String dbServer;
	@Inject
	@ConfigProperty(name = "db.user")
	private String dbUser;
	@Inject
	@ConfigProperty(name = "db.password")
	private String dbPassword;

	@Produces
	@Singleton
	public OpenAiService produceService() {
		return new OpenAiService(apiKey,
				Duration.ofSeconds(apiTimeout));
	}

}
