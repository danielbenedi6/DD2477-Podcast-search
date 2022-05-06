package com.example.application.views.searcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Word {
    @JsonProperty("startTime")
    private String startTime;
    @JsonProperty("endTime")
    private String endTime;
    @JsonProperty("word")
    private String word;



    public Word(){
        this.startTime = "";
        this.endTime = "";
        this.word = "";
    }

    public Word(String startTime, String endTime, String word) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.word = word;
    }

    public String getStartTime() {
        return startTime;
    }

    public Double getStartTimeAsDouble(){
        return Double.parseDouble(StringUtils.chop(startTime));
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public Double getEndTimeAsDouble(){
        return Double.parseDouble(StringUtils.chop(endTime));
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

}
