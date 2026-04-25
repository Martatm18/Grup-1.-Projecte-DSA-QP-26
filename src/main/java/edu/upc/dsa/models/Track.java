package edu.upc.dsa.models;

import edu.upc.dsa.util.RandomUtils;

public class Track {

    String id;
    String title;
    String singer;
    static int lastId;

    public Track() {
        this.setId(RandomUtils.getId());
    }
    public Track(String title, String singer) {
        this(null, title, singer);
    }

    public Track(String id, String title, String singer) {
        this();
        if (id != null) this.setId(id);
        this.setSinger(singer);
        this.setTitle(title);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id=id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    @Override
    public String toString() {
        return "Track [id="+id+", title=" + title + ", singer=" + singer +"]";
    }

}