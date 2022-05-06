package com.example.application.views.searcher;

public class Fragment implements Comparable<Fragment> {
    String fragment;
    Float score;

    String begin, end;

    public Fragment(String fragment, Float score, String begin, String end) {
        this.fragment = fragment;
        this.score = score;
        this.begin = begin;
        this.end = end;
    }

    public Fragment(){
        this.fragment = "";
        this.score = 0.0f;
        this.begin = "";
        this.end = "";
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    @Override
    public int compareTo(Fragment fragment) {
        return this.getScore().compareTo(fragment.getScore());
    }
}
