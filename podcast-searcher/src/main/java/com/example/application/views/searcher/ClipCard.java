package com.example.application.views.searcher;

import java.util.Date;

public class ClipCard {
    private String transcript;
    private String episode_name;
    private Date pubDate;
    private String enclosure;
    private String publisher;
    private float duration;
    private float startTime;
    private float endTime;

    public  ClipCard(){

    }
    public ClipCard(String transcript, String episode_name, Date pubDate, String enclosure, String publisher, float duration, float startTime, float endTime){
        this.transcript = transcript;
        this.episode_name = episode_name;
        this.pubDate = pubDate;
        this.enclosure = enclosure;
        this.publisher = publisher;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setEpisode_name(String episode_name) {
        this.episode_name = episode_name;
    }

    public String getEpisode_name() {
        return episode_name;
    }

    public void setEnclosure(String enclosure) {
        this.enclosure = enclosure;
    }

    public String getEnclosure() {
        return enclosure;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public float getDuration() {
        return duration;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public float getEndTime() {
        return endTime;
    }

    public void setEndTime(float endTime) {
        this.endTime = endTime;
    }

    public float getStartTime() {
        return startTime;
    }
}
