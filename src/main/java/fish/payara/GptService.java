package fish.payara;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import fish.payara.jpa.PointOfInterest;
import fish.payara.jpa.PointsOfInterestResponse;
import lombok.extern.java.Log;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.service.OpenAiService;

import fish.payara.jpa.RecipeSuggestion;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

@ApplicationScoped
@Log
public class GptService {

	private static final String SYSTEM_TASK_MESSAGE = """
			You are an API server that responds in a JSON format.
			Don't say anything else. Respond only with the JSON.

			The user will provide you with a city name and available budget. Considering the budget limit, you must suggest a list of places to visit.
			Allocate 30% of the budget to restaurants and bars.
			Allocate another 30% to shows, amusement parks, and other sightseeing.
			And dedicate the remainder of the budget to shopping. Remember, the user must spend 90-100% of the budget. Do NOT go above 100% of the budget.

			Respond in a JSON format, including an array named 'places'. Each item of the array is another JSON object that includes 'place_name' as a text,
			'place_short_info' as a text, and 'place_visit_cost' as a number.

			Don't add anything else in the end after you respond with the JSON.
			""";
	private static final String RECIPE_SYSTEM_TASK_MESSAGE = """
			You are an API server that responds in a JSON format.
			Don't say anything else. Respond only with the JSON.

			The structure of your response should be as follows

			{
			  "recipes": [
			    {
			      "recipeName": "",
			      "comment": "",
			      "ingredients": [],
			      "cookingSteps": []

			    }
			  ]
			}

			A JSON object that has an array of "recipes"
			Each recipe is an object with the following fields:
			"recipeName" - Name of the recipe
			"comment" - Any free-form comment you have about recipe. For eg where it originated from.
			"ingredients" - An array of ingredients for the recipe. List each ingredient as a string in this array.
			"cookingSteps" - An array of string, each string being a step in the recipe.

			The user will provide you with a prompt of ingredients they have. Based on the given prompt containing one or more ingredients,
			suggest recipes that can be prepared with the ingredients. Your response should fit in the above given JSON object.
			The user may prompt you with just the ingredients or some other custom instruction.
			Whatever the prompt is, look for ingredients and make your suggestions based on that and any other context you can understand from the prompt.

			Remember you are an API server, you only respond in JSON.


			Don't add anything else in the end after you respond with the JSON.
			""";
	@Inject
	@ConfigProperty(name = "gpt.model")
	private String gptModel;
	@Inject
	private OpenAiService openAiService;
	@Inject
	private CacheController cacheController;

	public PointsOfInterestResponse suggestPointsOfInterest(final String city, final BigDecimal budget) {

		int cacheKey = generateKey(city, budget);

		if (cacheController.isPointOfInterestCached(cacheKey)) {
			return cacheController.getResponse(cacheKey);
		}
		try {
			String request = String.format(Locale.ENGLISH, "I want to visit %s and have a budget of %,.2f dollars",
					city, budget);
			var poi = sendMessage(request);
			List<PointOfInterest> poiList = generaPointsOfInterest(poi);
			PointsOfInterestResponse response = new PointsOfInterestResponse();
			response.setPointsOfInterest(poiList);
			response.setCityName(city.toUpperCase(Locale.ENGLISH));
			cacheController.cachePoi(cacheKey, response);
			return response;
		} catch (Exception e) {
			log.log(Level.SEVERE, "An error occurred getting point of interest suggestions ", e);

			PointsOfInterestResponse response = new PointsOfInterestResponse();
			response.setError(e.getMessage());

			return response;
		}
	}

	public String generateImage(final GptRequestContext request) {
		CreateImageRequest imageRequest = CreateImageRequest.builder()
				.n(request.getNumberOfImages())
				.prompt(request.getPrompt())
				.size(request.getSize())
				.build();
		try {
			ImageResult image = openAiService.createImage(imageRequest);
			List<Image> data = image.getData();
			if (data != null && !data.isEmpty()) {
				return data.get(0).getUrl();
			}

		} catch (final Exception e) {
			log.log(Level.SEVERE, "An exception occurred calling the OpenAI service", e);
		}
		return null;

	}

	private String sendMessage(final String message) {

		ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
				.builder()
				.model(gptModel)
				.temperature(0.8)
				.messages(
						List.of(
								new ChatMessage("system", SYSTEM_TASK_MESSAGE),
								new ChatMessage("user", message)))
				.build();
		StringBuilder builder = new StringBuilder();
		log.log(Level.ALL, "Calling open AI service with the query " + chatCompletionRequest.toString());
		ChatCompletionResult chatCompletion = openAiService.createChatCompletion(chatCompletionRequest);

		chatCompletion.getChoices().forEach(choice -> builder.append(choice.getMessage().getContent()));

		return builder.toString();
	}

	public RecipeSuggestion requestRecipe(final String recipe) {
		RecipeSuggestion suggestion = null;
		Integer cacheKey = generateKey(recipe, null);

		if (cacheController.isRecipeCached(cacheKey)) {
			return cacheController.getCachedRecipeSuggestion(cacheKey);
		}

		try (final Jsonb jsonb = JsonbBuilder.newBuilder().build()) {
			ChatCompletionRequest recipeRequest = ChatCompletionRequest
					.builder()
					.model(gptModel)
					.temperature(0.8)
					.messages(
							List.of(
									new ChatMessage("system", RECIPE_SYSTEM_TASK_MESSAGE),
									new ChatMessage("user", recipe)))
					.build();
			StringBuilder recipeString = new StringBuilder();
			ChatCompletionResult response = openAiService.createChatCompletion(recipeRequest);
			response.getChoices().forEach(choice -> recipeString.append(choice.getMessage().getContent()));

			log.log(Level.INFO, String.format("JSON response from GPT %s", recipeString));

			suggestion = jsonb.fromJson(recipeString.toString(), RecipeSuggestion.class);
			suggestion.setRecipePrompt(recipe.toUpperCase(Locale.ENGLISH));
			cacheController.cacheRecipeSuggestion(cacheKey, suggestion);

			return suggestion;
		} catch (final Exception e) {
			log.log(Level.SEVERE, "An error calling OpenAI", e);

		}

		return suggestion;

	}

	private List<PointOfInterest> generaPointsOfInterest(String json) {
		try (final JsonReader reader = Json.createReader(new StringReader(json))) {

			JsonObject jsonObjectResponse = reader.readObject();
			JsonArray placesArray = jsonObjectResponse.getJsonArray("places");

			List<PointOfInterest> poiList = new ArrayList<>(placesArray.size());

			for (int i = 0; i < placesArray.size(); i++) {
				JsonObject jsonObject = placesArray.getJsonObject(i);
				PointOfInterest poi = PointOfInterest
						.builder()
						.info(jsonObject.getString("place_short_info"))
						.cost(BigDecimal.valueOf(jsonObject.getInt("place_visit_cost")))
						.name(jsonObject.getString("place_name"))
						.build();

				poiList.add(poi);
			}

			return poiList;
		}
	}

	private Integer generateKey(final String value, final BigDecimal budget) {
		if (budget != null) {
			return value.toUpperCase(Locale.ENGLISH).hashCode() + budget.hashCode();
		}
		return value.toUpperCase(Locale.ENGLISH).hashCode();
	}

}