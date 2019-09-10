package com.basement.panosx2.moviedatabase.Objects;

/*
 * Created by panos on 4/9/2019
 */

public class Suggestion {
    private int id;
    private String poster, title, type;

    public Suggestion(int id, String poster, String title, String type) {
        this.id = id;
        this.poster = poster;
        this.title = title;
        this.type = type;
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
}
