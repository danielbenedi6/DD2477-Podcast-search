package com.example.application.views.searcher;

import com.example.application.ElasticService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.text.SimpleDateFormat;
import java.util.List;

@PageTitle("Searcher")
@Route(value = "searcher", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class SearcherView extends Div  {

    Grid<Podcast> grid = new Grid<>();
    private HorizontalLayout pageButtonsLayout;
    public static final int PAGE_SIZE = 2;
    public static final int MAX_FRAGMENTS = 10;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private int currPage = 0;
    private int maxPage;
    Button arrowLeftButton, arrowRightButton;
    Div pageNumberDiv;

    Button correction;
    Paragraph time;

    public ElasticService.Result data;

    private MainLayout parent;

    public SearcherView(MainLayout parent) {
        this();
        this.parent = parent;
    }

    public SearcherView() {
        data = new ElasticService.Result();
        addClassName("searcher-view");
        setSizeFull();
        grid.setHeight("85%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addComponentColumn(this::createPodcast);
        grid.setVerticalScrollingEnabled(true);

        arrowLeftButton = new Button("Prev", new Icon(VaadinIcon.ARROW_LEFT));
        arrowRightButton = new Button("Next", new Icon(VaadinIcon.ARROW_RIGHT));
        arrowRightButton.setIconAfterText(true);
        arrowRightButton.addClickListener(click -> nextPage());
        arrowLeftButton.addClickListener(click -> previousPage());

        pageNumberDiv = new Div();
        pageNumberDiv.setVisible(false);

        pageButtonsLayout = new HorizontalLayout();
        pageButtonsLayout.setHeight("15%");
        pageButtonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        pageButtonsLayout.add(arrowLeftButton, pageNumberDiv, arrowRightButton);
        pageButtonsLayout.setVisible(false);
        pageButtonsLayout.setPadding(true);
        pageButtonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        time = new Paragraph();
        time.setVisible(false);
        time.getStyle().set("width", "100%");
        time.getStyle().set("font-size", "8px");
        time.getStyle().set("font-color", "lightgray");

        correction = new Button();
        correction.setWidthFull();
        correction.setText("");
        correction.setVisible(false);
        correction.getStyle().set("background-color", "transparent");
        correction.getStyle().set("font-size", "16px");
        correction.addClickListener(click -> {
            parent.searchField.setValue(data.suggestion.replace("(<b>|</b>)", ""));
            parent.searchButton.click();
        });

        add(correction);
        add(time);
        add(grid);
        add(pageButtonsLayout);
    }

    private HorizontalLayout createPodcast(Podcast podcast) {
        HorizontalLayout all = new HorizontalLayout();
        VerticalLayout title_play_content = new VerticalLayout();
        HorizontalLayout title_play = new HorizontalLayout();

        Image image = new Image(podcast.getImage(), podcast.getShow_name());

        Html title = new Html("<div><h3 style='margin: 0px;'>" + podcast.getEpisode_name() + "</h3><br>" +
                                "<h6 style='margin: 0px;'>" + podcast.getPublisher() + " - " + DATE_FORMAT.format(podcast.getPubDate()) + "</h6></div>");


        StringBuilder contentSB = new StringBuilder();
        for(int i = 0; i < Math.min(podcast.getResultFragments().size(), MAX_FRAGMENTS); i++) {
            Fragment f = podcast.getResultFragments().get(podcast.getResultFragments().size()-i-1);
            contentSB.append("(")
                    .append(f.getBegin())
                    .append(",")
                    .append(f.getEnd())
                    .append(") ")
                    .append(f.getFragment())
                    .append("<br>");
        }

        Html content = new Html("<div>" + contentSB + "</div>");
        content.getElement().getStyle().set("font-size", "14px");
        content.getElement().getStyle().set("font-color", "white");
        content.getElement().getStyle().set("border-style", "solid");
        content.getElement().getStyle().set("border-width", "1px");
        content.getElement().getStyle().set("border-color", "silver");
        content.getElement().getStyle().set("background-color", "rgb(35,51,72)");
        content.getElement().getStyle().set("padding", "5px");
        content.getElement().getStyle().set("width", "100%");

        Button playButton = new Button();
        Icon playIcon = new Icon(VaadinIcon.PLAY_CIRCLE);
        playButton.setIcon(playIcon);
        playButton.getStyle().set("color","#1DB954");
        playButton.setHeightFull();
        playButton.addClickListener(click -> {
            playPodcast(podcast.getEpisode_uri());
        });

        title_play.add(title, playButton);
        title_play_content.add(title_play, content);
        all.add(image, title_play_content);

        return all;
    }

    public void splitAndShowResultsInPages(ElasticService.Result data){
        this.data = data;
        int n = data.podcasts.size();
        int topPAGESIZEidx = Math.min(n, PAGE_SIZE);
        currPage = 0;
        maxPage = (int) Math.ceil((double) n/PAGE_SIZE);

        time.setText(n + " podcasts found in "+ data.time + " milliseconds");
        time.setVisible(true);

        if(this.data.suggestion.length() > 0){
            correction.setText("Did you mean: " + this.data.suggestion);
            correction.setVisible(true);
        }

        List<Podcast> topPAGESIZE = data.podcasts.subList(0, topPAGESIZEidx);
        grid.setItems(topPAGESIZE);
        grid.recalculateColumnWidths();
        updateButtonsAndPageDiv();
        pageButtonsLayout.setVisible(true);
        pageNumberDiv.setVisible(true);
        grid.setVisible(true);
    }

    private void updateButtonsAndPageDiv(){
        boolean enableLeft = currPage > 0;
        boolean enableRight = currPage < (maxPage-1);
        arrowLeftButton.setEnabled(enableLeft);
        arrowRightButton.setEnabled(enableRight);
        pageNumberDiv.setText("Page: "+(currPage+1)+"/"+maxPage);
    }

    public void nextPage(){
        currPage++;
        int start_idx = currPage*PAGE_SIZE;
        int end_idx = Math.min(data.podcasts.size(), (start_idx + PAGE_SIZE));
        grid.setItems(data.podcasts.subList(start_idx, end_idx));
        updateButtonsAndPageDiv();
    }

    public void previousPage(){
        currPage--;
        int start_idx = currPage*PAGE_SIZE;
        grid.setItems(data.podcasts.subList(start_idx, start_idx+PAGE_SIZE));
        updateButtonsAndPageDiv();
    }

    private void playPodcast(String uri){
        getUI().get().getPage().open(uri);
        System.out.println("redirect to play!");
    }
}
