package com.example.application.views.searcher;

public class Podcast {

    String title, content;
    int id;

    public Podcast(String title, String content){
        this.title = title;
        this.content = content;
    }
    public Podcast(Podcast podcast){
        this.title = podcast.getTitle();
        this.content = podcast.getContent();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
