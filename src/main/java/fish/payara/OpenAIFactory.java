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
public class OpenAIFactory {

	@Produces
	@PersistenceContext
	EntityManager entityManager;

	@Inject
	@ConfigProperty(name = "OPEN_API_KEY")
	String apiKey;

	@Inject
	@ConfigProperty(name = "openai.timeout")
	int apiTimeout;

	@Produces
	@Singleton
	public OpenAiService produceService() {
		return new OpenAiService(apiKey,
				Duration.ofSeconds(apiTimeout));
	}

}
