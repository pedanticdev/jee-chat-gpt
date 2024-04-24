package fish.payara.views.main;

import java.io.Serializable;

import fish.payara.ai.GptService;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import lombok.Getter;
import lombok.Setter;

import org.omnifaces.cdi.ViewScoped;

import fish.payara.ai.LangChainChatService;
import fish.payara.jpa.PointsOfInterestResponse;

@Named
@ViewScoped
@Getter
@Setter
public class PoiBean implements Serializable {

	@Inject
	GptService gptService;

	@Inject
	LangChainChatService langChainChatService;

	PointsOfInterestResponse response;

	SearchCriteria searchCriteria;

	public void search() {
		// response = gptService.suggestPointsOfInterest(searchCriteria);
		response = langChainChatService.generatePoi(searchCriteria);
		searchCriteria = SearchCriteria.of();
	}

	public void openNew() {
		searchCriteria = SearchCriteria.of();
	}
}
