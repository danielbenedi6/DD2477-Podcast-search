package com.example.application.views.searcher;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Words {
    @JsonProperty("startTime")
    private String startTime;
    @JsonProperty("endTime")
    private String endTime;
    @JsonProperty("word")
    private String word;
    @JsonProperty("speakerTag")
    private String speakerTag;

    public Words(){
        this.startTime = "";
        this.endTime = "";
        this.word = "";
        this.speakerTag = "";
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getSpeakerTag() {
        return speakerTag;
    }

    public void setSpeakerTag(String speakerTag) {
        this.speakerTag = speakerTag;
    }
}
