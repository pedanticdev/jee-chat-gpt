package fish.payara.views.main;

import fish.payara.ai.GptService;
import fish.payara.ai.LangChainChatService;
import fish.payara.jpa.RecipeSuggestion;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.util.StringUtil;
import org.omnifaces.cdi.ViewScoped;

@Named
@ViewScoped
@Getter
@Setter
public class RecipeBean implements Serializable {

    @Inject GptService gptService;
    @Inject LangChainChatService langChainChatService;

    private String recipeRequest;

    RecipeSuggestion recipeSuggestion;

    public void generateRecipeSuggest() {
        if (StringUtil.isNotBlank(recipeRequest)) {
            recipeSuggestion = langChainChatService.generateRecipeSuggestion(recipeRequest);
            recipeRequest = null;
        } else {
            FacesContext.getCurrentInstance()
                    .addMessage(
                            "errorMsg",
                            new FacesMessage(
                                    FacesMessage.SEVERITY_ERROR,
                                    "Enter ingredients",
                                    "Please enter a list of ingredients to get recipe"
                                            + " suggestions"));
        }
    }
}
