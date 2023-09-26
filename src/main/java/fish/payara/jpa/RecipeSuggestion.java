package fish.payara.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeSuggestion implements Serializable {

	private List<Recipe> recipes = new ArrayList<>();
}
