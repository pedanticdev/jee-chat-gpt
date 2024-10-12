package fish.payara.views.main;

import fish.payara.ai.GptService;
import fish.payara.ai.LangChainChatService;
import fish.payara.jpa.PointsOfInterestResponse;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.omnifaces.cdi.ViewScoped;

@Named
@ViewScoped
@Getter
@Setter
public class PoiBean implements Serializable {

    @Inject GptService gptService;

    @Inject LangChainChatService langChainChatService;

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
