package dk.au.mad21fall.appproject.group17.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dk.au.mad21fall.appproject.group17.Repository;
import dk.au.mad21fall.appproject.group17.R;
import dk.au.mad21fall.appproject.group17.adapters.RecipeAdapter;
import dk.au.mad21fall.appproject.group17.models.Recipe;
import dk.au.mad21fall.appproject.group17.models.User;
import dk.au.mad21fall.appproject.group17.viewmodels.RecipeListViewModel;

public class RecipeListActivity extends AppCompatActivity implements RecipeAdapter.IRecipeClickedListener{
    private static final String TAG = "RecipeListActivity";

    RecyclerView rcvList;
    RecipeAdapter adapter;
    RecipeListViewModel recipeListVM;

    ArrayList<Recipe> displayRecipes = new ArrayList<Recipe>();

    Button addButton;
    Button shoppingListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        adapter = new RecipeAdapter(this, this);
        rcvList = findViewById(R.id.recipeListRcvRecipe);
        rcvList.setLayoutManager(new LinearLayoutManager(this));
        rcvList.setAdapter(adapter);

        recipeListVM = new ViewModelProvider(this).get(RecipeListViewModel.class);
        recipeListVM.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                displayRecipes = (ArrayList<Recipe>) user.getRecipes();
                adapter.updateRecipeList(displayRecipes);
            }
        });

        addButton = findViewById(R.id.recipeListAddButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAddRecipeActivity();
            }
        });

        shoppingListButton = findViewById(R.id.recipeListShoppingButton);
        shoppingListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void startAddRecipeActivity() {
        Intent addRecipeIntent = new Intent(this, AddRecipeActivity.class);
        startActivity(addRecipeIntent);
    }

    @Override
    public void onRecipeClicked(int index) {
        Log.d(TAG, "du trykkede på: " + index);

        Recipe clickedRecipe = displayRecipes.get(index);

        Intent detailsRecipeIntent = new Intent(this, DetailRecipeActivity.class);
        detailsRecipeIntent.putExtra("recipeUid", clickedRecipe.getUid());
        startActivity(detailsRecipeIntent);
    }

}