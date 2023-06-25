package fish.payara;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.cache.Cache;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

@ApplicationScoped
public class TripsAdvisorService {


    private OpenAiService openAiService;

    @Inject
    @ConfigProperty(name = "open.api.key")
    private String apiKey;

    @Inject
    @ConfigProperty(name = "openai.timeout")
    private int apiTimeout;
    @Inject
    Cache<Integer, PointsOfInterestResponse> cache;

    private static final String GPT_MODEL = "gpt-3.5-turbo";

    private static final String SYSTEM_TASK_MESSAGE = """
            You are an API server that responds in a JSON format.
            Don't say anything else. Respond only with the JSON.

            The user will provide you with a city name and available budget. Considering the budget limit, you must suggest a list of places to visit.
            Allocate 30% of the budget to restaurants and bars.
            Allocate another 30% to shows, amusement parks, and other sightseeing.
            And dedicate the remainder of the budget to shopping. Remember, the user must spend 90-100% of the budget.

            Respond in a JSON format, including an array named 'places'. Each item of the array is another JSON object that includes 'place_name' as a text,
            'place_short_info' as a text, and 'place_visit_cost' as a number.

            Don't add anything else in the end after you respond with the JSON.
            """;

    @PostConstruct
    public void initGptService() {
        openAiService = new OpenAiService(apiKey,
                Duration.ofSeconds(apiTimeout));

        System.out.println("Connected to the OpenAI API");
    }

    public PointsOfInterestResponse suggestPointsOfInterest(String city, BigDecimal budget) {

        int cacheKey = generateKey(city, budget);

        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        try {
            String request = String.format("I want to visit %s and have a budget of %g dollars", city, budget);
            var poi = sendMessage(request);


            List<PointOfInterest> poiList = generaPointsOfInterest(poi);


            PointsOfInterestResponse response = new PointsOfInterestResponse();
            response.setPointsOfInterest(poiList);

            cache.put(cacheKey, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();

            PointsOfInterestResponse response = new PointsOfInterestResponse();
            response.setError(e.getMessage());

            return response;
        }
    }

    private String sendMessage(String message) {

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(GPT_MODEL)
                .temperature(0.8)
                .messages(
                        List.of(
                                new ChatMessage("system", SYSTEM_TASK_MESSAGE),
                                new ChatMessage("user", message)))
                .build();

        StringBuilder builder = new StringBuilder();
        ChatCompletionResult chatCompletion = openAiService.createChatCompletion(chatCompletionRequest);

        chatCompletion.getChoices().forEach(choice -> builder.append(choice.getMessage().getContent()));

        return builder.toString();
    }

    private List<PointOfInterest> generaPointsOfInterest(String json) {
        try(JsonReader reader = Json.createReader(new StringReader(json))) {

            JsonObject jsonObjectResponse = reader.readObject();
            JsonArray placesArray = jsonObjectResponse.getJsonArray("places");


            List<PointOfInterest> poiList = new ArrayList<>(placesArray.size());

            for (int i = 0; i < placesArray.size(); i++) {
                JsonObject jsonObject = placesArray.getJsonObject(i);
                PointOfInterest poi =
                        PointOfInterest
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

    private Integer generateKey(final String city, final BigDecimal budget) {
        return city.toUpperCase(Locale.ENGLISH).hashCode() + budget.hashCode();
    }
}