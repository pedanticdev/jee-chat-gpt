package fish.payara.views.main;

import java.io.Serializable;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import lombok.Getter;
import lombok.Setter;

import org.apache.poi.util.StringUtil;
import org.omnifaces.cdi.ViewScoped;

import fish.payara.GptService;
import fish.payara.jpa.RecipeSuggestion;

@Named
@ViewScoped
@Getter
@Setter
public class RecipeBean implements Serializable {

    @Inject
    GptService gptService;

    private String recipeRequest;

    RecipeSuggestion recipeSuggestion;

    public void generateRecipeSuggest() {
        if (StringUtil.isNotBlank(recipeRequest)) {
            recipeSuggestion = gptService.requestRecipe(recipeRequest);
            recipeRequest = null;
        } else {
            FacesContext.getCurrentInstance().
                    addMessage("errorMsg", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Enter ingredients", "Please enter a list of ingredients to get recipe suggestions"));
        }
    }


}
