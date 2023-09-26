package fish.payara.views.main;

import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.orderedlayout.VVerticalLayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import fish.payara.GptRequestContext;
import fish.payara.GptService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

@PageTitle("Generate Images With ChatGPT")
@Route(value = "image-gen", layout = ParentAppLayout.class)
public class GptImageGenerator extends VVerticalLayout {

	TextArea imagePrompt;
	Image image;
	@Inject
	private GptService gptService;
	private Binder<GptRequestContext> binder;
	private Button generateImageButton;
	private Button resetButton;

	@PostConstruct
	private void init() {
		Component logoLayout = ComponentUtil.generateTitleComponent("images/ai-image-logo.png", "AI Image Maker");
		HorizontalLayout inputOutputLayout = new HorizontalLayout();

		imagePrompt = new TextArea("Enter Image Prompt");
		imagePrompt.setMaxLength(1000);
		imagePrompt.setTooltipText("Enter image generation prompt. Eg a white siamese cat");
		imagePrompt.setWidth("500px");
		imagePrompt.setHeight("500px");
		imagePrompt.setClearButtonVisible(true);

		binder = new Binder<>(GptRequestContext.class);
		binder.forField(imagePrompt)
				.asRequired("An image prompt is required")
				// .withValidator(prompt -> (prompt.length() < 3 || prompt.length() > 1000),
				// "The image generation prompt should be between 3 and 1000 characters")
				.bind(GptRequestContext::getPrompt, GptRequestContext::setPrompt);

		image = new Image();
		image.setAlt("AI generated image");
		image.setWidth("520px");
		image.setHeight("520px");
		image.setVisible(false);

		inputOutputLayout.setWidthFull();
		inputOutputLayout.setHeightFull();
		inputOutputLayout.add(imagePrompt, image);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		generateImageButton = new VButton("Generate")
				.withIcon(VaadinIcon.CAMERA.create())
				.withType(VButton.ButtonType.PRIMARY)
				.withSize(VButton.ButtonSize.LARGE)
				.withTooltip("Click to generate your AI image");

		generateImageButton.addClickListener(e -> generateImage());

		resetButton = new VButton()
				.withIcon(VaadinIcon.TRASH.create())
				.withClickListener(b -> resetFields());
		buttonLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		buttonLayout.add(generateImageButton, resetButton);

		VerticalLayout userInputLayout = new VerticalLayout();
		userInputLayout.add(inputOutputLayout, buttonLayout);

		add(logoLayout, userInputLayout);
	}

	private void generateImage() {
		GptRequestContext request = new GptRequestContext();
		if (binder.writeBeanIfValid(request)) {
			String imageUrl = gptService.generateImage(request);
			if (imageUrl != null && !imageUrl.isEmpty()) {

				image.setSrc(imageUrl);
				image.setVisible(true);
			} else {
				Notification.show("No image returned from OpenAI. Please Check your query and try again");
			}

		} else {
			image.setVisible(false);

		}
	}

	private void resetFields() {
		imagePrompt.clear();
		imagePrompt.setValue(null);
		image.setVisible(false);
	}
}
