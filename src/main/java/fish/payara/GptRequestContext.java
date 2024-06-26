package fish.payara;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GptRequestContext {
	@NotEmpty
	@Size(min = 3, max = 1000)
	private String prompt;

	private String size = "512x512";
	private int numberOfImages = 1;

	public static GptRequestContext of() {
		return new GptRequestContext();
	}

}
