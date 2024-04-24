package fish.payara;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;

import fish.payara.ai.GptService;
import fish.payara.jpa.PointOfInterest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fish.payara.jpa.PointsOfInterestResponse;
import fish.payara.jpa.RecipeSuggestion;

class GptServiceTest {

	@InjectMocks
	private GptService gptService;

	@Mock(strictness = Mock.Strictness.LENIENT)
	private OpenAiService openAiService;

	@Mock(strictness = Mock.Strictness.LENIENT)
	private CacheController cacheController;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testSuggestPointsOfInterest_Cached() {
		var city = "TestCountry";
		BigDecimal budget = BigDecimal.valueOf(1000);
		int cacheKey = city.toUpperCase().hashCode() + budget.hashCode();

		when(cacheController.isPointOfInterestCached(cacheKey)).thenReturn(true);
		PointsOfInterestResponse cachedResponse = new PointsOfInterestResponse();
		when(cacheController.getResponse(cacheKey)).thenReturn(cachedResponse);

		PointsOfInterestResponse response = gptService.suggestPointsOfInterest(city, budget);

		verify(cacheController, times(1)).isPointOfInterestCached(cacheKey);
		verify(cacheController, times(1)).getResponse(cacheKey);
		assertSame(cachedResponse, response);
	}

	@Test
	void testRequestRecipe() {
		String recipeRequest = "Test Recipe";
		Integer cacheKey = recipeRequest.toUpperCase().hashCode();

		when(cacheController.isRecipeCached(cacheKey)).thenReturn(false);


		when(openAiService.createChatCompletion(any(ChatCompletionRequest.class)))
				.thenReturn(getChatCompletionResult());

		RecipeSuggestion suggestion = gptService.requestRecipe(recipeRequest);

		verify(cacheController, times(1)).isRecipeCached(cacheKey);
		assertNotNull(suggestion);
		assertFalse(suggestion.getRecipes().isEmpty());

	}

	private ChatCompletionResult getChatCompletionResult() {
		// ... Set up mock ChatCompletionResult ...
		ChatCompletionResult chatCompletionResult = new ChatCompletionResult();
		ChatCompletionChoice chatCompletionChoice = new ChatCompletionChoice();
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setContent("""
				{
				  "recipePrompt": "recipePrompt_63a8f92c9264",
				  "recipes": [
				    {
				      "recipeName": "recipeName_345f57860fc0",
				      "comment": "comment_11207063afb4",
				      "ingredients": [
				        "ingredients_5c1406ff064a"
				      ],
				      "cookingSteps": [
				        "cookingSteps_0c190d39564e"
				      ],
				      "id": 0,
				      "computedHashCode": 0
				    }
				  ],
				  "id": 0,
				  "computedHashCode": 0
				}
				
				""");
		chatCompletionChoice.setMessage(chatMessage);
		chatCompletionResult.setChoices(List.of(chatCompletionChoice));
		return chatCompletionResult;
	}

}
