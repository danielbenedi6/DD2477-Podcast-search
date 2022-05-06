package com.example.application.views.searcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Clip {
    @JsonProperty("words")
    private List<Word> words;


    @JsonProperty("transcript")
    private String transcript;

    public Clip(){
        words = new ArrayList<>();
        transcript = "";
    }
    public Clip(List<Word> words, String transcript) {
        this.words = words;
        this.transcript = transcript;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }
    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }
}
