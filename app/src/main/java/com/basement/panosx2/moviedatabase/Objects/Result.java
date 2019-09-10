package com.basement.panosx2.moviedatabase.Objects;

/*
 * Created by panos on 4/9/2019
 */

public class Result {
    private int id, rate;
    private String poster, title, type;

    public Result(int id, String poster, String title, String type, int rate) {
        this.id = id;
        this.poster = poster;
        this.title = title;
        this.type = type;
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    public String getPoster() {
        return poster;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public int getRate() {
        return rate;
    }
}