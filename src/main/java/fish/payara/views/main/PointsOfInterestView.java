package fish.payara.views.main;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamReceiver;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.Lumo;
import fish.payara.PointOfInterest;
import fish.payara.PointsOfInterestResponse;
import fish.payara.ReportRequestContext;
import fish.payara.TripsAdvisorService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.vaadin.firitin.components.DynamicFileDownloader;
import org.vaadin.firitin.components.orderedlayout.VVerticalLayout;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

@PageTitle("Trip On Budget")
@Route("")
public class PointsOfInterestView extends VVerticalLayout {

    @Inject
    private TripsAdvisorService tripsAdvisorService;
    @Inject
    private ReportService reportService;

    private Grid<PointOfInterest> grid;
    private Binder<SearchCriteria> binder;
    private Button searchButton;
    DynamicFileDownloader pdfDownload;
    HorizontalLayout userInputLayout;
    private TextField totalTextField;
    ProgressBar spinner;
    SearchCriteria searchCriteria;
    PointsOfInterestResponse response;

    @PostConstruct
    private void init() {

        UI.getCurrent().getElement().setAttribute("theme", Lumo.LIGHT);

        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setWidthFull();
        logoLayout.setAlignItems(Alignment.BASELINE);

        Label title = new Label("BudgetJourney");
        title.getStyle().set("font-weight", "bold");
        title.getStyle().set("font-size", "20px");

        Image logo = new Image("/images/trip_on_budget.png", "");
        logo.setMaxWidth("55px");

        logoLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        logoLayout.add(logo, title);

        // Create the binder for the search criteria
        binder = new Binder<>(SearchCriteria.class);

        userInputLayout = new HorizontalLayout();
        userInputLayout.setDefaultVerticalComponentAlignment(Alignment.END);

        // Create the text fields for the search criteria
        TextField cityField = new TextField("Your next destination:");
        cityField.setWidth("300px");
        cityField.getStyle().set("margin-right", "10px");
        cityField.setPlaceholder("city name");

        binder.forField(cityField)
                .asRequired("City name is required")
                .bind(SearchCriteria::getCity, SearchCriteria::setCity);

        IntegerField budgetField = new IntegerField("Your budget:");
        budgetField.setWidth("300px");
        budgetField.getStyle().set("margin-right", "10px");
        budgetField.setPlaceholder("dollars");

        binder.forField(budgetField)
                .asRequired("Budget is required")
                .withValidator(budget -> budget > 0, "Budget must be greater than zero")
                .bind(SearchCriteria::getBudget, SearchCriteria::setBudget);

        // Create the search button
        searchButton = new Button("Go!");
        searchButton.getStyle().set("margin-top", "10px");
        searchButton.addClickListener(e -> searchPointsOfInterest());
        searchButton.setDisableOnClick(true);


        userInputLayout.add(cityField, budgetField, searchButton);

        add(logoLayout, userInputLayout);
        totalTextField = new TextField();
        totalTextField.setVisible(false);
        // Create the grid to display the points of interest
        grid = new Grid<>();
        grid.addColumn(PointOfInterest::getName).setHeader("Place").setFlexGrow(1).setSortable(true);
        grid.addColumn(PointOfInterest::getInfo).setHeader("Info").setFlexGrow(2);
        Grid.Column<PointOfInterest> costColumn = grid.addColumn(v -> renderCost(v.getCost()));
        costColumn.setFooter(totalTextField);
        costColumn.setHeader("Price").setFlexGrow(0).setSortable(true).setTextAlign(ColumnTextAlign.END);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        add(grid);

        spinner = new ProgressBar();
        spinner.setIndeterminate(true);
        spinner.setVisible(false);

        add(spinner);
    }

    private String renderCost(BigDecimal cost) {
        String formattedCost = NumberFormat.getCurrencyInstance(Locale.US).format(cost);
        return formattedCost.replaceAll("\\.00", "");
    }

    private void searchPointsOfInterest() {
        // Bind the search criteria to the binder
        spinner.setVisible(true);
        searchCriteria = new SearchCriteria();
        UI current = UI.getCurrent();
        current.setPollInterval(50);

        if (binder.writeBeanIfValid(searchCriteria)) {
            // Call the suggestPointsOfInterest method and update the grid with the results

            response = tripsAdvisorService
                    .suggestPointsOfInterest(searchCriteria.getCity(), searchCriteria.getBudget());

            if (response.getError() != null) {
                showErrorMessage(String.format("Failed loading data from OpenAI GPT: %n%s", response.getError()));
            } else {
//                if (pdfDownload.isAttached()) {
//                    userInputLayout.remove(pdfDownload);
//                }
                grid.setItems(response.getPointsOfInterest());
                totalTextField.setVisible(true);
                totalTextField.setValue(renderCost(response.getTotalCost()));
                downloadAsPDF();
                userInputLayout.add(pdfDownload);

                current.setPollInterval(-1);
                spinner.setVisible(false);
            }

            searchButton.setEnabled(true);
        }
    }

    private void downloadAsPDF() {
        pdfDownload = new DynamicFileDownloader(" pdf", "itinerary_" + LocalDateTime.now(ZoneOffset.UTC) + ".pdf",
                out -> {

                    ReportRequestContext requestContext = new ReportRequestContext();
                    requestContext.setResponse(response);
                    requestContext.setSearchCriteria(searchCriteria);
                    requestContext.setOutputStream(out);
                    reportService.writeAsPdf(requestContext);
                });
        pdfDownload.addComponentAsFirst(VaadinIcon.DOWNLOAD.create());
        pdfDownload.setDisableOnClick(true);
    }

    @Getter
    @Setter
    public static class SearchCriteria {
        private String city;
        private int budget;
    }

    private void showErrorMessage(String errorMessage) {
        Notification notification = new Notification();
        notification.setText(errorMessage);
        notification.setDuration(10_000); // Set the duration to 10 seconds
        notification.setPosition(Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.open();
    }

}
