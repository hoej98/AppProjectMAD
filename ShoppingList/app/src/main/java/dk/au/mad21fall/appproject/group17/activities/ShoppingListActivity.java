package dk.au.mad21fall.appproject.group17.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.lang.reflect.Array;
import java.util.ArrayList;

import dk.au.mad21fall.appproject.group17.NotificationService;
import dk.au.mad21fall.appproject.group17.R;
import dk.au.mad21fall.appproject.group17.adapters.ItemAdapter;
import dk.au.mad21fall.appproject.group17.models.Item;
import dk.au.mad21fall.appproject.group17.models.User;
import dk.au.mad21fall.appproject.group17.viewmodels.ShoppingListViewModel;

public class ShoppingListActivity extends AppCompatActivity implements ItemAdapter.IItemClickedListener {
    private static final String TAG = "ShoppingListActivity";

    RecyclerView rcvList;
    ItemAdapter adapter;
    ShoppingListViewModel shoppingListVM;

    ArrayList<Item> displayItems = new ArrayList<Item>();
    ArrayList<Item> markedForDeletion = new ArrayList<Item>();

    Button addButton;
    Button removeButton;
    Button recipeButton;
    Button logOutButton;
    Button selectAllButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        adapter = new ItemAdapter(this, this);
        rcvList = findViewById(R.id.shoppingListRcv);
        rcvList.setLayoutManager(new LinearLayoutManager(this));
        rcvList.setAdapter(adapter);

        shoppingListVM = new ViewModelProvider(this).get(ShoppingListViewModel.class);
        shoppingListVM.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                // gets items from the users shoppinglist
                displayItems = (ArrayList<Item>) user.getShoppingItems();
                // if the item was previously marked, readd to list
                for (Item item : displayItems) {
                    if (item.getChecked()) {
                        markedForDeletion.add(item);
                    }
                }
                // update list
                adapter.updateItemList(displayItems);
            }
        });

        setupUI();

        Intent foregroundServiceIntent = new Intent(this, NotificationService.class);
        startService(foregroundServiceIntent);
    }

    private void setupUI() {
        addButton = findViewById(R.id.shoppingListAdd);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mainVM.addRandomItem();
                startAddItemActivity();
            }
        });
        removeButton = findViewById(R.id.shoppingListRemove);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeMarkedItems();
            }
        });
        recipeButton = findViewById(R.id.shoppingListRecipes);
        recipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecipeListActivity();
            }
        });
        logOutButton = findViewById(R.id.shoppingListLogOut);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLoginActivity();
                finish();
            }
        });
        selectAllButton = findViewById(R.id.shoppingListSelectAll);
        selectAllButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                selectAll();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void selectAll() {
        // if all items are already checked, instead uncheck all
        if (displayItems.stream().allMatch(check -> check.getChecked() == true)){
            for (Item item : displayItems) {
                item.setChecked(false);
                markedForDeletion.clear();
            }
        // else, check all and mark for deletion
        } else{
            for (Item item : displayItems) {
                item.setChecked(true);
                markedForDeletion = new ArrayList<Item>(displayItems);
            }
        }

        adapter.updateItemList(displayItems);
    }

    private void removeMarkedItems() {
        if (markedForDeletion.isEmpty()) {
            Toast.makeText(this, R.string.shoppingListRemoveToast, Toast.LENGTH_SHORT).show();
        } else {
            // removes all checkmarks
            for (Item item : displayItems) {
                item.setChecked(false);
            }
            // remove all marked items from the list
            for (Item item : markedForDeletion) {
                displayItems.remove(item);
            }
            // update list
            adapter.updateItemList(displayItems);
            // clear the marked list
            markedForDeletion.clear();

            // update in database
            shoppingListVM.updateItemList(displayItems);
            Toast.makeText(this, R.string.shoppingListConfirmRemoveToast, Toast.LENGTH_SHORT).show();
        }
    }

    private void startAddItemActivity() {
        Intent addItemIntent = new Intent(this, AddItemActivity.class);
        startActivity(addItemIntent);
    }

    private void startLoginActivity() {
        // signs out before relaunching the login activity
        FirebaseAuth.getInstance().signOut();
        shoppingListVM.signOut();

        Intent loginActivity = new Intent(this, LoginActivity.class);
        startActivity(loginActivity);
        finish();
    }

    private void startRecipeListActivity() {
        Intent recipeListIntent = new Intent(this, RecipeListActivity.class);
        startActivity(recipeListIntent);
    }

    @Override
    public void onItemClicked(int index) {
        Log.d(TAG, "du trykkede på: " + index);

        Item clickedItem = displayItems.get(index);

        Intent editItemActivity = new Intent(this, EditItemActivity.class);
        editItemActivity.putExtra("itemUid", clickedItem.getUid());
        startActivity(editItemActivity);
    }

    @Override
    public void onCheckBoxClicked(int index) {
        // gets the checkbox on the item clicked
        Item checkedItem = displayItems.get(index);
        // inverts the checkbox status
        checkedItem.setChecked(!checkedItem.getChecked());
        // if true (checked) add to mark for deletion, otherwise remove mark
        if (checkedItem.getChecked()) {
            markedForDeletion.add(checkedItem);
        } else {
            markedForDeletion.remove(checkedItem);
        }

        // used for testing
        Log.d(TAG, "Clicked on: " + index + " - Currently marked: " + markedForDeletion.size());
        for (Item item : markedForDeletion) {
            Log.d(TAG, "Name: " + item.getName());
        }
    }
}