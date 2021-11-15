package dk.au.mad21fall.appproject.group17.models;

import java.util.List;

public class Item {
    private String uid;
    private String name;
    private String note;
    private String pictureUrl;
    private Boolean isChecked;

    public Item() {
        uid = java.util.UUID.randomUUID().toString();
        isChecked = false;
    }

    public Item(String name, String note, String pictureUrl) {
        uid = java.util.UUID.randomUUID().toString();
        isChecked = false;
        this.name = name;
        this.note = note;
        this.pictureUrl = pictureUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }
}
