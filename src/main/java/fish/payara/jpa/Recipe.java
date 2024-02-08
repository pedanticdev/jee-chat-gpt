package fish.payara.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity

public class Recipe extends AbstractEntity {

	private String recipeName;
	private String comment;

	@ElementCollection
	private List<String> ingredients = new ArrayList<>();

	@Column(length = 1000)
	@Convert(converter = ListToJsonConverter.class)
	private List<String> cookingSteps = new ArrayList<>();

}
