package fish.payara.jpa;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RecipeSuggestion implements Serializable {

    private List<Recipe> recipes = new ArrayList<>();
}
