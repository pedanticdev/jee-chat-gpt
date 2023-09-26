package fish.payara.views.main;

import org.apache.commons.lang3.text.WordUtils;
import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.orderedlayout.VVerticalLayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
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
import fish.payara.jpa.RecipeSuggestion;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

@PageTitle("Get Recipe Suggestions From ChatGPT")
@Route(value = "recipe-suggestions", layout = ParentAppLayout.class)
public class RecipeView extends VVerticalLayout {

	TextArea recipePrompt;
	@Inject
	private GptService gptService;
	private Binder<GptRequestContext> binder;
	private Button generateRecipebutton;
	private VerticalLayout recipeLayout;
	private Button resetButton;

	@PostConstruct
	private void init() {
		Component logoLayout = ComponentUtil.generateTitleComponent("images/ai-image-logo.png", "ChatGPT Recipes");
		HorizontalLayout inputOutputLayout = new HorizontalLayout();
		recipeLayout = new VVerticalLayout();

		recipePrompt = new TextArea("Enter Recipe Prompt");
		recipePrompt.setMaxLength(1000);
		recipePrompt.setTooltipText("Enter a recipe prompt. For example, rice, chicken, garlic");
		recipePrompt.setWidth("500px");
		recipePrompt.setHeight("500px");
		recipePrompt.setClearButtonVisible(true);

		binder = new Binder<>(GptRequestContext.class);
		binder.forField(recipePrompt)
				.asRequired("Please enter a recipe prompt")
				// .withValidator(prompt -> (prompt.length() < 3 || prompt.length() > 1000),
				// "The image generation prompt should be between 3 and 1000 characters")
				.bind(GptRequestContext::getPrompt, GptRequestContext::setPrompt);

		inputOutputLayout.setWidthFull();
		inputOutputLayout.setHeightFull();
		inputOutputLayout.add(recipePrompt, recipeLayout);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		generateRecipebutton = new VButton("Get Recipes")
				.withIcon(VaadinIcon.COFFEE.create())
				.withType(VButton.ButtonType.PRIMARY)
				.withSize(VButton.ButtonSize.LARGE)
				.withTooltip("Click to get recipe suggestions");

		generateRecipebutton.addClickListener(e -> generateRecipe());

		resetButton = new VButton()
				.withIcon(VaadinIcon.TRASH.create())
				.withClickListener(b -> resetFields());
		buttonLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		buttonLayout.add(generateRecipebutton, resetButton);

		VerticalLayout userInputLayout = new VerticalLayout();
		userInputLayout.add(inputOutputLayout, buttonLayout);

		add(logoLayout, userInputLayout);

	}

	private void generateRecipe() {
		GptRequestContext request = new GptRequestContext();
		if (binder.writeBeanIfValid(request)) {
			RecipeSuggestion suggestion = gptService.requestRecipe(request.getPrompt());
			if (suggestion != null) {
				H1 header = new H1("Here are your recipe suggestions");

				recipeLayout.add(header);
				suggestion.getRecipes().forEach(r -> {

					VerticalLayout recipeDisplayLayout = new VVerticalLayout();
					H2 recipeTitle = new H2(r.getRecipeName());
					H3 recipeComment = new H3(r.getComment());

					recipeDisplayLayout.add(recipeTitle, recipeComment);

					Accordion ingredientsAccordion = new Accordion();

					VerticalLayout ingredientsLayout = new VVerticalLayout();
					for (String string : r.getIngredients()) {
						ingredientsLayout.add(new Span(WordUtils.capitalize(string)));
					}
					ingredientsAccordion.add("Ingredients", ingredientsLayout);
					recipeDisplayLayout.add(ingredientsAccordion);

					Accordion cookingStepAccording = new Accordion();

					VerticalLayout cookingStepLayout = new VVerticalLayout();
					for (int i = 0; i < r.getCookingSteps().size(); i++) {
						cookingStepLayout.add(new Span(i + 1 + ". " + r.getCookingSteps().get(i)));
					}
					cookingStepAccording.add("Recipe", cookingStepLayout);
					recipeDisplayLayout.add(cookingStepAccording);
					recipeLayout.add(recipeDisplayLayout);

				});
			} else {
				Notification.show("No recipe returned from ChatGPT. Please Check your query and try again");
			}

		} else {
			recipeLayout.removeAll();

		}
	}

	private void resetFields() {
		recipePrompt.clear();
		recipeLayout.removeAll();
	}
}
