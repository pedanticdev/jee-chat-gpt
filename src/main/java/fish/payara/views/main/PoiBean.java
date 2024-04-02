package fish.payara.views.main;

import fish.payara.GptService;
import fish.payara.jpa.PointsOfInterestResponse;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.omnifaces.cdi.ViewScoped;

import java.io.Serializable;
@Named
@ViewScoped
@Getter
@Setter
public class PoiBean implements Serializable {

    @Inject
    GptService gptService;

    PointsOfInterestResponse response;

    SearchCriteria searchCriteria;

    @PostConstruct
    void init() {
    }

    public void search() {
        response = gptService.suggestPointsOfInterest(searchCriteria);
        searchCriteria = SearchCriteria.of();
    }

    public void openNew() {
        searchCriteria = SearchCriteria.of();
    }
}
