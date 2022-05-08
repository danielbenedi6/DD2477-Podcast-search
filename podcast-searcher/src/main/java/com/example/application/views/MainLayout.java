package com.example.application.views;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import com.example.application.ElasticService;
import com.example.application.views.searcher.SearcherView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouterLink;
import org.elasticsearch.client.RestClient;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private SearcherView searcherView;
    private final ElasticService service;

    public Header header;
    public Div layoutAppName;
    public VerticalLayout verticalLayout;
    public VerticalLayout searchLayout;
    public H1 appName;
    public HorizontalLayout layoutSearch;
    public TextField searchField;
    public IntegerField secondsField;
    public Div secSuffix;
    public Div segmentPrefix;
    public Button searchButton;
    public Button correction;

    /**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, String iconClass, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            // Use Lumo classnames for various styling
            link.addClassNames("flex", "h-m", "items-center", "px-s", "relative", "text-secondary");
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames("font-medium", "text-s", "whitespace-nowrap");

            link.add(new LineAwesomeIcon(iconClass), text);
            add(link);
        }

        public Class<?> getView() {
            return view;
        }

        /**
         * Simple wrapper to create icons using LineAwesome iconset. See
         * https://icons8.com/line-awesome
         */
        @NpmPackage(value = "line-awesome", version = "1.3.0")
        public static class LineAwesomeIcon extends Span {
            public LineAwesomeIcon(String lineawesomeClassnames) {
                // Use Lumo classnames for suitable font size and margin
                addClassNames("me-s", "text-l");
                if (!lineawesomeClassnames.isEmpty()) {
                    addClassNames(lineawesomeClassnames);
                }
            }
        }

    }

    public MainLayout(ElasticService service) {
        this.service = service;
        addToNavbar(createHeaderContent());
        setContent(createResultsView());
    }

    private Component createResultsView(){
        searchLayout = new VerticalLayout();
        searchLayout.setHeight("100%");
        searcherView = new SearcherView();
        correction = new Button();
        correction.setVisible(false);
        correction.setWidthFull();
        correction.getStyle().set("background-color", "transparent");
        correction.getStyle().set("font-size", "16px");

        searchLayout.add(correction, searcherView);
        searchLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        return searchLayout;
    }

    private Component createHeaderContent() {
        header = new Header();
        header.addClassNames("bg-base", "border-b", "border-contrast-10", "box-border", "flex", "flex-col", "w-full");
        header.setWidthFull();
        verticalLayout = new VerticalLayout();

        layoutAppName = new Div();
        layoutAppName.addClassNames("flex", "h-xl", "items-center", "px-l");

        appName = new H1("Podcast Searcher");
        appName.getStyle().set("font-size", "42px");
        layoutAppName.add(appName);

        // search layout (textField, IntegerField, button) -> (query, seconds, search button)
        layoutSearch = new HorizontalLayout();
        layoutSearch.setWidthFull();
        layoutSearch.getStyle().set("align","center");
        // search field (podcast query)
        searchField = new TextField("");
        searchField.setClearButtonVisible(true);
        searchField.getStyle().set("width", "40%");
        searchField.setPrefixComponent(new Icon("search"));
        // IntegerField (duration in seconds)
        secondsField = new IntegerField();
        secondsField.setValue(4);
        secSuffix = new Div();
        secSuffix.setText("sec");
        segmentPrefix = new Div();
        segmentPrefix.setText("Duration: ");
        secondsField.setPrefixComponent(segmentPrefix);
        secondsField.setSuffixComponent(secSuffix);
        secondsField.setWidth("15%");
        // search button
        searchButton = new Button("Search");
        searchButton.addClickListener(click ->
        {
            String query = searchField.getValue();
            //searchField.clear();
            searchField.focus();
            correction.setVisible(false);
            searchQuery(query, secondsField.getValue());
        });
        searchButton.addClickShortcut(Key.ENTER);
        // add previous components to layoutSearch
        layoutSearch.add(searchField, secondsField, searchButton);
        layoutSearch.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        verticalLayout.add(layoutAppName);
        verticalLayout.addAndExpand(layoutSearch);
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.setSpacing(false);
        verticalLayout.getThemeList().set("spacing-xl", true);
        header.add(verticalLayout);
        return header;
    }

    private void searchQuery(String query, int seconds){
        ElasticService.Result result = service.search(query, seconds);
        if(result.suggestion.length() > 0){
            System.out.println("Did you mean: " + result.suggestion);
            correction.getElement().setText("Did you mean: " + result.suggestion);
            correction.addClickListener(click -> {
                searchField.setValue(result.suggestion);
                searchButton.click();
            });
            correction.setVisible(true);
            searchLayout.setVisible(true);
            searcherView.setVisible(false);
            setContent(searchLayout);
        }
        else {
            searcherView.setVisible(true);
            searcherView.splitAndShowResultsInPages(result);
            setContent(searcherView);
        }

    }

}
