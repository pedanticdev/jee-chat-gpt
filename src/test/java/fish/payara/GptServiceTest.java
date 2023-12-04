package fish.payara;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import fish.payara.jpa.RecipeSuggestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

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
         String city = "TestCity";
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
     @Disabled
     void testRequestRecipe() {
         String recipeRequest = "Test Recipe";
         Integer cacheKey = recipeRequest.toUpperCase().hashCode();

         when(cacheController.isRecipeCached(cacheKey)).thenReturn(false);
         // ... Set up mock ChatCompletionResult ...

         when(openAiService.createChatCompletion(any(ChatCompletionRequest.class))).thenReturn(new ChatCompletionResult());

         RecipeSuggestion suggestion = gptService.requestRecipe(recipeRequest);

         verify(cacheController, times(1)).isRecipeCached(cacheKey);
         assertNotNull(suggestion);
     }

 }
