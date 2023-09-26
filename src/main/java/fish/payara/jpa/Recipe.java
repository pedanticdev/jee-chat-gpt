package fish.payara.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Recipe implements Serializable {
	private String recipeName;
	private String comment;
	private List<String> ingredients = new ArrayList<>();
	private List<String> cookingSteps = new ArrayList<>();

}
