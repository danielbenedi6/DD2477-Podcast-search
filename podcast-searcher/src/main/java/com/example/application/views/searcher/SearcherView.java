package com.example.application.views.searcher;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Searcher")
@Route(value = "searcher", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class SearcherView extends Div  {

    Grid<Podcast> grid = new Grid<>();
    private HorizontalLayout pageButtonsLayout;
    public static final int PAGE_SIZE = 10;
    private int currPage = 1;
    private int maxPage;
    Button arrowLeftButton, arrowRightButton;
    Div pageNumberDiv;
    List<Podcast> podcasts;

    public SearcherView() {
        podcasts = new ArrayList<>();
        addClassName("searcher-view");
        setSizeFull();
        grid.setHeight("85%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addComponentColumn(this::createPodcastCard);
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

        add(grid);
        add(pageButtonsLayout);
    }

    private HorizontalLayout createPodcastCard(Podcast podcast) {
        HorizontalLayout text_playButton_layout = new HorizontalLayout();

        TextArea podcastContent = new TextArea();
        podcastContent.setWidthFull();
        podcastContent.setLabel(podcast.getTitle());
        podcastContent.setValueChangeMode(ValueChangeMode.EAGER);
        podcastContent.setValue(podcast.getContent());
        podcastContent.setEnabled(false);
        podcastContent.getStyle().set("font-size", "14px");
        podcastContent.getStyle().set("font-weight", "bold");
        podcastContent.getStyle().set("font-color", "white");

        Button playButton = new Button();
        Icon playIcon = new Icon(VaadinIcon.PLAY_CIRCLE);
        playButton.setIcon(playIcon);
        playButton.getStyle().set("color","#1DB954");
        playButton.setHeightFull();
        playButton.addClickListener(click -> {
            playPodcastById(podcast.getId());
        });

        text_playButton_layout.add(podcastContent, playButton);
        text_playButton_layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        text_playButton_layout.setPadding(false);
        text_playButton_layout.setWidthFull();

        return text_playButton_layout;
    }
    private static Podcast createPodcast(String title, String content) {
        return new Podcast(title, content);
    }

    public ArrayList<Podcast> searchPodcasts(String query, int seconds){
        throw new NotImplementedException("Implement searchPodcasts function (SearcherView class)");
    }

    public void insertPodcast(Podcast podcast){
        List<Podcast> podcastList = new ArrayList<>();
        podcastList.add(podcast);
        grid.setItems(podcastList);
    }

    public void insertPodcastNtimes(int n){
        int cont = 0;
        List<Podcast> podcastList = new ArrayList<>();
        String lorem = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ips";
        String partialLorem = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.";
        String currentContent;
        while(cont<n){
            if (cont%3==0) {
                currentContent = (lorem);
            }
            else {
                currentContent = (partialLorem);
            }
            podcastList.add(new Podcast((cont)+" result title", currentContent));
            cont++;
        }
        splitAndShowResultsInPages(podcastList);
    }

    private void splitAndShowResultsInPages(List<Podcast> podcastList){
        podcasts = podcastList;
        int n = podcasts.size();
        int topPAGESIZEidx = Math.min(n, PAGE_SIZE);
        currPage = 1;
        maxPage = (int) Math.ceil((double) n/PAGE_SIZE);

        List<Podcast> topPAGESIZE = podcasts.subList(0, topPAGESIZEidx);
        grid.setItems(topPAGESIZE);
        grid.recalculateColumnWidths();

        updateButtonsAndPageDiv();
        pageButtonsLayout.setVisible(true);
        pageNumberDiv.setVisible(true);
        grid.setVisible(true);
    }

    private void updateButtonsAndPageDiv(){
        boolean enableLeft = currPage > 1;
        boolean enableRight = currPage < maxPage;
        arrowLeftButton.setEnabled(enableLeft);
        arrowRightButton.setEnabled(enableRight);
        pageNumberDiv.setText("Page: "+currPage+"/"+maxPage);
    }

    public void nextPage(){
        int start_idx = currPage*PAGE_SIZE;
        int end_idx = Math.min(podcasts.size(), (start_idx + PAGE_SIZE));
        grid.setItems(podcasts.subList(start_idx, end_idx));
        currPage++;
        updateButtonsAndPageDiv();
    }

    public void previousPage(){
        int start_idx = (currPage-1)*PAGE_SIZE;
        grid.setItems(podcasts.subList(start_idx, start_idx+PAGE_SIZE));
        currPage--;
        updateButtonsAndPageDiv();
    }

    private void playPodcastById(int id){
        System.out.println("Not implemented yet (play podcast)");
    }
}
