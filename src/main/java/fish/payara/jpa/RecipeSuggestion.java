package fish.payara.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NamedQuery(name = RecipeSuggestion.QUERY_NAME, query = "select r from RecipeSuggestion r where r.computedHashCode = :"
        + PointsOfInterestResponse.PARAM_NAME)
@NamedQuery(name = RecipeSuggestion.QUERY_NAME_ALL, query = "select r from RecipeSuggestion r where r.computedHashCode in :"
        + PointsOfInterestResponse.PARAM_NAME)
public class RecipeSuggestion extends AbstractEntity {
    public static final String QUERY_NAME = "RecipeSuggestion.getByKey";
    public static final String QUERY_NAME_ALL = "RecipeSuggestion.getAllByKey";

    private String recipePrompt;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Recipe> recipes = new ArrayList<>();
}
