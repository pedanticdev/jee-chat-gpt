package fish.payara.ai;

import static java.time.Duration.ofSeconds;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import lombok.extern.java.Log;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import fish.payara.jpa.PointOfInterest;
import fish.payara.jpa.PointsOfInterestResponse;
import fish.payara.jpa.RecipeSuggestion;
import fish.payara.views.main.SearchCriteria;

@Log
@ApplicationScoped
public class LangChainChatService {
	@Inject
	@ConfigProperty(name = "gpt.model")
	String gptModel;

	@Inject
	@ConfigProperty(name = "OPEN_API_KEY")
	String apiKey;

	@Inject
	@ConfigProperty(name = "model.temperature")
	Double temperature;

	// StreamingChatLanguageModel model;

	@Inject
	OpenAiChatModel model;

	PoiChat poiChat;
	RecipeChat recipeChat;

	@PostConstruct
	void init() {

		poiChat = AiServices.create(PoiChat.class, model);
		recipeChat = AiServices.create(RecipeChat.class, model);

	}

	public PointsOfInterestResponse generatePoi(SearchCriteria searchCriteria) {
		String userMessage = String.format(Locale.ENGLISH, "I want to visit %s and have a budget of %,.2f dollars",
				searchCriteria.getCity(), searchCriteria.getBudget());
		PointsOfInterestResponse pointsOfInterestResponse = PointsOfInterestResponse.of();
		try (final Jsonb jsonb = JsonbBuilder.newBuilder().build()) {
			String responseString = poiChat.suggestPoi(userMessage);
			List<PointOfInterest> pointOfInterests = jsonb.fromJson(responseString,
					new TypeLiteral<List<PointOfInterest>>() {
					}.getType());
			pointsOfInterestResponse.setPointsOfInterest(pointOfInterests);
		} catch (final Exception e) {
			log.log(Level.SEVERE, "An error calling OpenAI", e);

		}

		return pointsOfInterestResponse;
	}

	public RecipeSuggestion generateRecipeSuggestion(String userMessage) {
		RecipeSuggestion recipeSuggestion = RecipeSuggestion.of();
		try (final Jsonb jsonb = JsonbBuilder.newBuilder().build()) {
			String aiResponse = recipeChat.generateRecipe(userMessage);
			recipeSuggestion = jsonb.fromJson(aiResponse, RecipeSuggestion.class);
			recipeSuggestion.setRecipePrompt(userMessage.toUpperCase(Locale.ENGLISH));

		} catch (final Exception e) {
			log.log(Level.SEVERE, "An error calling OpenAI", e);

		}
		return recipeSuggestion;
	}

}
