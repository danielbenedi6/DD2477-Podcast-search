package com.example.application.views.searcher;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Clip {
    @JsonProperty("transcript")
    private String transcript;
    @JsonProperty("confidence")
    private float confidence;
    @JsonProperty("words")
    private List<Words> words;

    public Clip(){
        this.transcript = "";
        this.confidence = 0.0F;
    }
    public Clip(String transcript, long confidence, List<Words> words){
        this.transcript = transcript;
        this.confidence = confidence;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }
}
