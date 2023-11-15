package fish.payara;

import java.time.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.theokanning.openai.service.OpenAiService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@ApplicationScoped
public class OpenAIFactory {

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
