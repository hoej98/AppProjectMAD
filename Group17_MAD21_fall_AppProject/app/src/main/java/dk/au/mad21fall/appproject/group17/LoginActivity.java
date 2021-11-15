package dk.au.mad21fall.appproject.group17;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.au.mad21fall.appproject.group17.models.Item;
import dk.au.mad21fall.appproject.group17.models.Recipe;
import dk.au.mad21fall.appproject.group17.models.User;

public class LoginActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersRef = database.getReference("Users");
    List<Recipe> recipes = new ArrayList<Recipe>();
    List<Item> shoppingItems = new ArrayList<Item>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shoppingItems.add(new Item("Banan","2 stk","http://dinmor.com"));
        shoppingItems.add(new Item("Avocado","100 stk","http://dinmor.com"));
        shoppingItems.add(new Item("Agurk","-1 stk","http://dinmor.com"));

        Map<String, User> users = new HashMap<>();
        User user = new User("dum@gmail.com");
        user.setShoppingItems(shoppingItems);
        user.setRecipes(recipes);
        users.put("User1", user);
        users.put("User2", new User("adadad@gmail.com"));

        usersRef.setValue(users);
    }
}