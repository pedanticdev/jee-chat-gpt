package fish.payara;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class GptRequestContext {
	@NotEmpty
	@Size(min = 3, max = 1000)
	private String prompt;

	private String size = "512x512";
	private int numberOfImages = 1;

}
