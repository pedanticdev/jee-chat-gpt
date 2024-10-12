package fish.payara.ai;

import static java.time.Duration.ofSeconds;

import com.theokanning.openai.service.OpenAiService;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class OpenAIFactory {

    @Produces @PersistenceContext EntityManager entityManager;

    @Inject
    @ConfigProperty(name = "open.api.key")
    String apiKey;

    @Inject
    @ConfigProperty(name = "gpt.model")
    String gptModel;

    @Inject
    @ConfigProperty(name = "model.temperature")
    Double temperature;

    @Inject
    @ConfigProperty(name = "openai.timeout")
    int apiTimeout;

    @Produces
    @Singleton
    public OpenAiService produceService() {
        return new OpenAiService(apiKey, Duration.ofSeconds(apiTimeout));
    }

    @Produces
    @Singleton
    public OpenAiChatModel produceModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                // .responseFormat("json_object")
                .modelName(gptModel)
                .temperature(temperature)
                .timeout(ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
