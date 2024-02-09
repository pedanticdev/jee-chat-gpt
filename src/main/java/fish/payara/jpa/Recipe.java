package fish.payara.jpa;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

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
