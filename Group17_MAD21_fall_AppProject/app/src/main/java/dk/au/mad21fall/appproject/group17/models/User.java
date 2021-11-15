package dk.au.mad21fall.appproject.group17.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {

    private String uid;
    private String email;
    private List<Item> shoppingItems;
    private List<Recipe> recipes;

    public User() {
        uid = java.util.UUID.randomUUID().toString();
    }

    public User(String email) {
        uid = java.util.UUID.randomUUID().toString();
        this.email = email;
        shoppingItems = new ArrayList<Item>();
        recipes = new ArrayList<Recipe>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Item> getShoppingItems() {
        return shoppingItems;
    }

    public void setShoppingItems(List<Item> shoppingItems) {
        this.shoppingItems = shoppingItems;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }
}


