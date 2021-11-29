package dk.au.mad21fall.appproject.group17;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.au.mad21fall.appproject.group17.models.Item;
import dk.au.mad21fall.appproject.group17.models.Recipe;
import dk.au.mad21fall.appproject.group17.models.User;

public class Repository {
    private static final String TAG = "Repository";

    private static Repository instance;

    // LiveData users array with getter/setter
    MutableLiveData<User> user;
    MutableLiveData<List<Recipe>> recipes;

    public static Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }
        return instance;
    }

    public LiveData<User> getUser() {
        if (user == null) {
            loadUserData();
        }
        return user;
    }

    private void loadUserData() {
        user = new MutableLiveData<User>();

        // get currently logged in user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // logs used to test/ensure right user
            Log.d(TAG, "User uid: " + currentUser.getUid());
            Log.d(TAG, "User name: " + currentUser.getDisplayName());
            Log.d(TAG, "User email: " + currentUser.getEmail());

            // db access
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users")
                    .whereEqualTo("uid", currentUser.getUid())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshotUser, @Nullable FirebaseFirestoreException error) {
                            // first doc taken - should only be 1 document in snapshot, since search is made with uid
                            DocumentSnapshot doc = snapshotUser.getDocuments().get(0);
                            // ensure not null before updating user
                            User updatedUser = doc.toObject(User.class);
                            if (updatedUser != null) {
                                user.setValue(updatedUser);
                            }
                        }
                    });
        }
    }

    // resets the user on logout
    public void signOut(){
        user = null;
    }

    public void addItem(Item item) {
        // gets user and updates user with new item
        User tempCurrentUser = this.getUser().getValue();
        tempCurrentUser.getShoppingItems().add(item);



        // database access, updates db user with new item
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(tempCurrentUser.getUid()).set(tempCurrentUser);
        Log.d(TAG, "Item added with ID: " + item.getUid());
    }

    public void editItem(Item initialItem, Item newItem) {
        // gets user and updates user with new item
        User tempCurrentUser = this.getUser().getValue();
        int index = tempCurrentUser.getShoppingItems().indexOf(initialItem);
        tempCurrentUser.getShoppingItems().set(index, newItem);

        // database access, updates db user with new item
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(tempCurrentUser.getUid()).set(tempCurrentUser);
        Log.d(TAG, "Item: " + initialItem.getUid() + "replaced with: " + newItem.getUid());
    }

    public void removeItem(Item item) {
        // gets user and updates user with new item
        User tempCurrentUser = this.getUser().getValue();
        tempCurrentUser.getShoppingItems().remove(item);

        // database access, updates db user with new item
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(tempCurrentUser.getUid()).set(tempCurrentUser);
        Log.d(TAG, "Item removed with ID: " + item.getUid());
    }

    public void updateItemList(List<Item> items) {
        // gets user and updates user with new item
        User tempCurrentUser = this.getUser().getValue();
        tempCurrentUser.setShoppingItems(items);

        // database access, updates db user with new item
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(tempCurrentUser.getUid()).set(tempCurrentUser);
        Log.d(TAG, "Items updated on user: " + tempCurrentUser.getUid());
    }

    public void addListOfItems(List<Item> newItems) {
        // gets user and updates user with new item
        User tempCurrentUser = this.getUser().getValue();
        ArrayList<Item> currentItems = (ArrayList<Item>) tempCurrentUser.getShoppingItems();
        currentItems.addAll(newItems);
        tempCurrentUser.setShoppingItems(currentItems);

        // database access, updates db user with new item
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(tempCurrentUser.getUid()).set(tempCurrentUser);
        Log.d(TAG, "Items updated on user: " + tempCurrentUser.getUid());
    }

    public void addRecipe(Recipe newRecipe) {
        // gets user and updates user with new item
        User tempCurrentUser = this.getUser().getValue();
        tempCurrentUser.getRecipes().add(newRecipe);

        // database access, updates db user with new recipe
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(tempCurrentUser.getUid()).set(tempCurrentUser);
        Log.d(TAG, "Recipes updated on user: " + tempCurrentUser.getUid());
    }

    public void removeRecipe(Recipe newRecipe) {
        // gets user and updates user with new item
        User tempCurrentUser = this.getUser().getValue();
        tempCurrentUser.getRecipes().remove(newRecipe);

        // database access, updates db user with new recipe
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(tempCurrentUser.getUid()).set(tempCurrentUser);
        Log.d(TAG, "Recipes updated on user: " + tempCurrentUser.getUid());
    }

    public void updateRecipe(Recipe newRecipe) {
        // gets user and updates user with new item
        User tempCurrentUser = this.getUser().getValue();

        int index = 0;
        for (Recipe recipe : tempCurrentUser.getRecipes()) {
            if (recipe.getUid().equals(newRecipe.getUid())) {
                index = tempCurrentUser.getRecipes().indexOf(recipe);
            }
        }

        tempCurrentUser.getRecipes().set(index, newRecipe);

        // database access, updates db user with new item
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(tempCurrentUser.getUid()).set(tempCurrentUser);
        Log.d(TAG, "Recipe: " + newRecipe.getUid() + " has been updated");
    }
}
