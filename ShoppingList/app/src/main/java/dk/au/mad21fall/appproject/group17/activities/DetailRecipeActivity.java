package dk.au.mad21fall.appproject.group17.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import dk.au.mad21fall.appproject.group17.Repository;
import dk.au.mad21fall.appproject.group17.R;
import dk.au.mad21fall.appproject.group17.adapters.DetailRecipeAdapter;
import dk.au.mad21fall.appproject.group17.models.Item;
import dk.au.mad21fall.appproject.group17.models.Recipe;
import dk.au.mad21fall.appproject.group17.models.User;
import dk.au.mad21fall.appproject.group17.viewmodels.DetailRecipeViewModel;

public class DetailRecipeActivity extends AppCompatActivity {
    private static final String TAG = "DetailRecipeActivity";
    private static final int EDIT_RECIPE = 1111;

    DetailRecipeAdapter adapter;
    RecyclerView rcvList;
    DetailRecipeViewModel detailRecipeVM;

    Recipe detailedRecipe;

    TextView detailsRecipeName;
    TextView detailsRecipeInstructions;
    ImageView detailsRecipeImageView;
    Button detailsRecipeCancelButton;
    Button detailsRecipeEditButton;
    Button detailsRecipeAddToSLButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_recipe);

        adapter = new DetailRecipeAdapter(this);
        rcvList = findViewById(R.id.detailsRecipeRcv);
        rcvList.setLayoutManager(new LinearLayoutManager(this));
        rcvList.setAdapter(adapter);

        Intent data = getIntent();
        String recipeUid = data.getStringExtra("recipeUid");

        setupUI();

        detailRecipeVM = new ViewModelProvider(this).get(DetailRecipeViewModel.class);
        detailRecipeVM.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                // find the recipe from the intent data
                ArrayList<Recipe> userRecipes = (ArrayList<Recipe>) user.getRecipes();
                for (Recipe recipe : userRecipes)
                    if (recipe.getUid().equals(recipeUid)) {
                        detailedRecipe = recipe;
                    }

                // sets the initial values for the given recipe
                setValues();
            }
        });
    }

    private void setValues() {
        // update item rcv list
        adapter.updateItemList((ArrayList<Item>) detailedRecipe.getIngredientItems());
        // update the various fields with correct values
        detailsRecipeName.setText(detailedRecipe.getName());
        detailsRecipeInstructions.setText(detailedRecipe.getDirections());
        detailsRecipeInstructions.setInputType(0);
        // updates the imageView with the picture of the recipe
        Glide.with(this)
                .load(detailedRecipe.getPictureUrl())
                .placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(detailsRecipeImageView);
    }

    private void setupUI() {
        detailsRecipeName = findViewById(R.id.detailsRecipeName);
        detailsRecipeImageView = findViewById(R.id.detailsRecipeImage);
        detailsRecipeCancelButton = findViewById(R.id.detailsRecipeCancelButton);
        detailsRecipeEditButton = findViewById(R.id.detailsRecipeEditButton);
        detailsRecipeAddToSLButton = findViewById(R.id.detailsRecipeAddToSLButton);
        detailsRecipeInstructions = findViewById(R.id.detailsRecipeInstructions);

        detailsRecipeCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
        detailsRecipeEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit();
            }
        });
        detailsRecipeAddToSLButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToShoppingList();
            }
        });
    }

    private void addToShoppingList() {
        detailRecipeVM.addListOfItems(detailedRecipe.getIngredientItems());
        Toast.makeText(this, R.string.detailsRecipeIngredientsToast, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void edit() {
        Intent editRecipeIntent = new Intent(this, EditRecipeActivity.class);
        editRecipeIntent.putExtra("editRecipeUid", detailedRecipe.getUid());
        startActivityForResult(editRecipeIntent, EDIT_RECIPE);
    }

    private void cancel() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // upon return from edit, automatically closes details activity and goes back to recipe list
        if (requestCode == EDIT_RECIPE && resultCode == RESULT_OK){
            setResult(RESULT_OK);
            finish();
        }
    }
}