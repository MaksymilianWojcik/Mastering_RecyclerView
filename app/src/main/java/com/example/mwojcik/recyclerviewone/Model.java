package com.example.mwojcik.recyclerviewone;

public class Model {
    int id;
    String title;
    String description;

    public Model(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Model(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
