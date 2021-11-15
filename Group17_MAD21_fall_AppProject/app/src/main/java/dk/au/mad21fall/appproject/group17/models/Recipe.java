package dk.au.mad21fall.appproject.group17.models;

import java.util.List;

public class Recipe {
    private String uid;
    private String name;
    private String directions;
    private List<Item> ingredientItems;
    private String pictureUrl;

    public Recipe() {
        uid = java.util.UUID.randomUUID().toString();
    }

    public Recipe(String name, String directions, List<Item> ingredientItems, String pictureUrl) {
        uid = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.directions = directions;
        this.ingredientItems = ingredientItems;
        this.pictureUrl = pictureUrl;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirections() {
        return directions;
    }

    public void setDirections(String directions) {
        this.directions = directions;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<Item> getIngredientItems() {
        return ingredientItems;
    }

    public void setIngredientItems(List<Item> ingredientItems) {
        this.ingredientItems = ingredientItems;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
