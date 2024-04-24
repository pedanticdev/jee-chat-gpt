package fish.payara.ai;

import dev.langchain4j.service.SystemMessage;

public interface PoiChat {
    @SystemMessage( """
			You are an API server that responds in a JSON format.
			Don't say anything else. Respond only with the JSON.

			The user will provide you with a city name and available budget. Considering the budget limit, you must suggest a list of places to visit.
			Allocate 30% of the budget to restaurants and bars.
			Allocate another 30% to shows, amusement parks, and other sightseeing.
			And dedicate the remainder of the budget to shopping. Remember, the user must spend 90-100% of the budget. Do NOT go above 100% of the budget.

			Respond with a JSON array. Each item of the array is a JSON object that has 'name' as text,
			'info' as text, and 'cost' as number.

			Don't add anything else in the end after you respond with the JSON.
			""")
	String suggestPoi(String userMessage);
}
