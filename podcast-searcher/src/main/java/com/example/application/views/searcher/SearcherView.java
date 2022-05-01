package com.example.application.views.searcher;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.ClickEvent;
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

import javax.sound.sampled.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Searcher")
@Route(value = "searcher", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class SearcherView extends Div  {

    Grid<Podcast> grid = new Grid<>();
    private HorizontalLayout pageButtonsLayout;
    public static final int PAGE_SIZE = 10;
    private int currPage = 0;
    private int maxPage;
    Button arrowLeftButton, arrowRightButton;
    Div pageNumberDiv;
    public List<Podcast> podcasts;

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
        podcastContent.setLabel("[Episode name: " + podcast.getEpisode_name() + "]---[Publisher: " + podcast.publisher + "]---[Episode date: " + podcast.getPubDate() + "]");
        podcastContent.setValueChangeMode(ValueChangeMode.EAGER);
        //podcastContent.setValue(podcast.getTranscript());
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
            playPodcast(podcast.getEnclosure());
        });


        text_playButton_layout.add(podcastContent, playButton);
        text_playButton_layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        text_playButton_layout.setPadding(false);
        text_playButton_layout.setWidthFull();

        return text_playButton_layout;
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
            //podcastList.add(new Podcast((cont)+" result title", (cont)+" show name",currentContent, "https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
            cont++;
        }
        splitAndShowResultsInPages(podcastList);
    }

    public void splitAndShowResultsInPages(List<Podcast> podcastList){
        podcasts = podcastList;
        int n = podcasts.size();
        int topPAGESIZEidx = Math.min(n, PAGE_SIZE);
        currPage = 0;
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
        boolean enableLeft = currPage > 0;
        boolean enableRight = currPage < (maxPage-1);
        arrowLeftButton.setEnabled(enableLeft);
        arrowRightButton.setEnabled(enableRight);
        pageNumberDiv.setText("Page: "+(currPage+1)+"/"+maxPage);
    }

    public void nextPage(){
        currPage++;
        int start_idx = currPage*PAGE_SIZE;
        int end_idx = Math.min(podcasts.size(), (start_idx + PAGE_SIZE));
        grid.setItems(podcasts.subList(start_idx, end_idx));
        updateButtonsAndPageDiv();
    }

    public void previousPage(){
        currPage--;
        int start_idx = currPage*PAGE_SIZE;
        grid.setItems(podcasts.subList(start_idx, start_idx+PAGE_SIZE));
        updateButtonsAndPageDiv();
    }

    private void playPodcast(String enclosure){
        getUI().get().getPage().open(enclosure);
        System.out.println("redirect to play!");
    }
}
