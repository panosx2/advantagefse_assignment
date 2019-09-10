package com.basement.panosx2.moviedatabase.Objects;

/*
 * Created by panos on 5/9/2019
 */

public class Saved {

    private int id;
    private String poster, title, type, description;

    public Saved(int id, String poster, String title, String type, String description) {
        this.id = id;
        this.poster = poster;
        this.title = title;
        this.type = type;
        this.description = description;
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

    public String getDescription() {
        return description;
    }
}
