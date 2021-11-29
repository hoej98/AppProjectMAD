package dk.au.mad21fall.appproject.group17.models;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {

    private String uid;
    private String name;
    private String email;
    private List<Item> shoppingItems = new ArrayList<Item>();
    private List<Recipe> recipes = new ArrayList<Recipe>();

    public User() {
        uid = UUID.randomUUID().toString();
    }

    public User(String name, String email, List<Item> shoppingItems, List<Recipe> recipes) {
        uid = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.shoppingItems = shoppingItems;
        this.recipes = recipes;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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


