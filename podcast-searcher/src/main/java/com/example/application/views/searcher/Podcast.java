package com.example.application.views.searcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Podcast implements Comparable<Podcast>{

    String episode_name;
    String show_name;
    String transcript;
    String episode_uri;
    String pubDate;
    String enclosure;
    String publisher;

    int id;

    public Podcast(){
        this.episode_uri = "";
        this.episode_name = "";
        this.show_name = "";
        this.transcript = "";
        this.pubDate = "";
        this.enclosure = "";
        this.publisher = "";
        this.id = -1;
    }

    public Podcast(String episode_name, String show_name, String transcript, String episode_uri, String pubDate, String enclosure, String publisher) {
        this.episode_name = episode_name;
        this.transcript = transcript;
        this.show_name = show_name;
        this.episode_uri = episode_uri;
        this.pubDate = pubDate;
        this.enclosure = enclosure;
        this.publisher = publisher;
    }
    public Podcast(Podcast podcast){
        this.episode_name = podcast.getEpisode_name();
        this.show_name = podcast.getShow_name();
        this.transcript = podcast.getTranscript();
        this.episode_uri = podcast.getEpisode_uri();
        this.pubDate = podcast.getPubDate();
        this.enclosure =podcast.getEnclosure();
        this.publisher = podcast.getPublisher();
    }

    public String getEpisode_name() {
        return episode_name;
    }

    public void setEpisode_name(String episode_name) {
        this.episode_name = episode_name;
    }

    public String getShow_name() {
        return show_name;
    }

    public void setShow_name(String show_name) {
        this.show_name = show_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getEpisode_uri() {
        return episode_uri;
    }

    public void setEpisode_uri(String episode_uri) {
        this.episode_uri = episode_uri;
    }

    public String getPubDate(){ return  pubDate;}

    public void setPubDate(String pubDate) { this.pubDate = pubDate;}

    public String getEnclosure(){ return  enclosure;}

    public void setEnclosure(String enclosure) { this.enclosure = enclosure;}

    public String getPublisher(){ return  publisher;}

    public void setPublisher(String publisher) { this.publisher = publisher;}


    @Override
    public int compareTo(Podcast o) {
        Podcast tmp = (Podcast) o;
        //TODO
        return 0;
    }
}
