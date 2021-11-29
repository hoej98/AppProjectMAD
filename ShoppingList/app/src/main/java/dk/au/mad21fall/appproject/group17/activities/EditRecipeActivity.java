package dk.au.mad21fall.appproject.group17.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import dk.au.mad21fall.appproject.group17.Repository;
import dk.au.mad21fall.appproject.group17.R;
import dk.au.mad21fall.appproject.group17.adapters.EditRecipeAdapter;
import dk.au.mad21fall.appproject.group17.models.Item;
import dk.au.mad21fall.appproject.group17.models.Recipe;
import dk.au.mad21fall.appproject.group17.models.User;
import dk.au.mad21fall.appproject.group17.viewmodels.EditRecipeViewModel;

public class EditRecipeActivity extends AppCompatActivity implements EditRecipeAdapter.IEditRecipeItemClickedListener {
    private static final String TAG = "EditRecipeActivity";
    private static final int EDIT_RECIPE_ITEM = 11111;
    private static final int ADD_RECIPE_ITEM = 1111;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_IMAGE = 11;
    private static final String storageBase = "gs://test123-c52e1.appspot.com/";

    EditRecipeAdapter adapter;
    RecyclerView rcvList;
    EditRecipeViewModel editRecipeVM;

    Recipe initialRecipe;

    TextView editRecipeName;
    EditText editRecipeInstructions;
    ImageView editRecipeImageView;
    Button editRecipeCancelButton;
    Button editRecipeRemoveButton;
    Button editRecipeSaveButton;
    Button editRecipeAddIngredientButton;

    Bitmap recipeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        adapter = new EditRecipeAdapter(this, this);
        rcvList = findViewById(R.id.editRecipeRcv);
        rcvList.setLayoutManager(new LinearLayoutManager(this));
        rcvList.setAdapter(adapter);

        Intent data = getIntent();
        String recipeUid = data.getStringExtra("editRecipeUid");

        setupUI();

        editRecipeVM = new ViewModelProvider(this).get(EditRecipeViewModel.class);
        editRecipeVM.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                // find the recipe from the intent data
                ArrayList<Recipe> userRecipes = (ArrayList<Recipe>) user.getRecipes();
                for (Recipe recipe : userRecipes)
                    if (recipe.getUid().equals(recipeUid)) {
                        initialRecipe = recipe;
                    }

                // sets the initial values for the given recipe
                setValues();
            }
        });
    }

    private void setupUI() {
        editRecipeName = findViewById(R.id.editRecipeName);
        editRecipeImageView = findViewById(R.id.editRecipeImage);
        editRecipeCancelButton = findViewById(R.id.editRecipeCancelButton);
        editRecipeRemoveButton = findViewById(R.id.editRecipeRemoveButton);
        editRecipeSaveButton = findViewById(R.id.editRecipeSaveButton);
        editRecipeAddIngredientButton = findViewById(R.id.editRecipeAddIngredientButton);
        editRecipeInstructions = findViewById(R.id.editRecipeInstructions);

        editRecipeCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
        editRecipeRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove();
            }
        });
        editRecipeSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
        editRecipeAddIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addIngredient();
            }
        });
        editRecipeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addImage();
            }
        });
    }

    private void addImage() {
        AlertDialog imageDialog = setupImageDialog(this, initialRecipe);
        imageDialog.show();
    }

    // setup method for the image dialog
    private AlertDialog setupImageDialog(Context cont, Recipe newRecipe) {
        // dialog box prompting the user to enter recipe name
        AlertDialog imageDialog = null;
        AlertDialog.Builder imageDialogBuilder = new AlertDialog.Builder(cont);
        imageDialogBuilder.setTitle(R.string.addRecipeImageDialogName);

        // Set up the "Camera" button
        imageDialogBuilder.setPositiveButton(R.string.addRecipeImageDialogYes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if user chooses "Camera", start camera activity to take a picture
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    // if no camera, notify user and close dialog
                    dialog.cancel();
                    Toast.makeText(getApplicationContext(), R.string.addRecipeCameraError, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up the "Storage" button
        imageDialogBuilder.setNegativeButton(R.string.addRecipeImageDialogNeutral, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if user chooses "Storage", start storage activity to select a picture
                Intent selectFromStorageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                selectFromStorageIntent.setType("image/*");
                try {
                    startActivityForResult(selectFromStorageIntent, SELECT_IMAGE);
                } catch (ActivityNotFoundException e) {
                    // if no storage found, notify user and close dialog
                    dialog.cancel();
                    Toast.makeText(getApplicationContext(), R.string.addRecipeStorageError, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up the "No thanks" button
        imageDialogBuilder.setNeutralButton(R.string.addRecipeImageDialogNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if no, save the recipe with no image
                dialog.cancel();
            }
        });
        // create imageDialog
        imageDialog = imageDialogBuilder.create();
        return imageDialog;
    }

    private void cancel() {
        Toast.makeText(this, R.string.addRecipeCancelledToast, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void remove() {
        editRecipeVM.removeRecipe(initialRecipe);
        Toast.makeText(this, R.string.editRecipeRemovedToast, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void save() {
        Toast.makeText(this, R.string.editRecipeSavedToast, Toast.LENGTH_SHORT).show();
        // sets the edited name/directions for the recipe
        initialRecipe.setName(editRecipeName.getText().toString());
        initialRecipe.setDirections(editRecipeInstructions.getText().toString());
        // list gets saved to initialRecipe in onActivityResult, if it gets edited
        // uploads the image to the firebase storage, gets the downloadUrl, adds the edited Recipe
        // to the db and finishes the activity
        uploadToFirebaseStorage();
    }

    private void addIngredient() {
        Intent addItem = new Intent(this, AddRecipeItemActivity.class);
        startActivityForResult(addItem, ADD_RECIPE_ITEM);
    }

    private void setValues() {
        // update item rcv list
        adapter.updateItemList((ArrayList<Item>) initialRecipe.getIngredientItems());
        // update the various fields with correct values
        editRecipeName.setText(initialRecipe.getName());
        editRecipeInstructions.setText(initialRecipe.getDirections());
        // updates the imageView with the picture of the recipe
        Glide.with(this)
                .load(initialRecipe.getPictureUrl())
                .placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(editRecipeImageView);
    }

    @Override
    public void onEditRecipeItemClicked(int index) {
        Intent editItem = new Intent(this, EditRecipeItemActivity.class);

        Item clickedItem = initialRecipe.getIngredientItems().get(index);

        editItem.putExtra("fromEditRecipe", true);
        editItem.putExtra("itemUid", clickedItem.getUid());
        editItem.putExtra("itemName", clickedItem.getName());
        editItem.putExtra("itemAmount", clickedItem.getAmount());
        editItem.putExtra("itemPicture", clickedItem.getPictureUrl());
        startActivityForResult(editItem, EDIT_RECIPE_ITEM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if returned from edit recipe item, remove/edit the item
        if (requestCode == EDIT_RECIPE_ITEM && resultCode == RESULT_OK && data != null) {
            boolean removeItem = data.getBooleanExtra("removeItem", false);
            boolean updateItem = data.getBooleanExtra("updateItem", false);
            // check if removeItem is set to true, in which case the item should be removed
            if (removeItem) {
                // finds the item to remove
                for (Item item : initialRecipe.getIngredientItems()) {
                    if (item.getUid().equals(data.getStringExtra("itemToRemoveUid"))) {
                        // removes the item from ingredient list and updates list
                        initialRecipe.getIngredientItems().remove(item);
                        adapter.updateItemList((ArrayList<Item>) initialRecipe.getIngredientItems());
                    }
                }
                // else if update item is true, the item should be updated instead
            } else if (updateItem) {
                for (Item item : initialRecipe.getIngredientItems()) {
                    if (item.getUid().equals(data.getStringExtra("itemToUpdateUid"))) {
                        // find the item in ingredient list and updates it
                        item.setName(data.getStringExtra("updatedItemName"));
                        item.setAmount(data.getStringExtra("updatedItemAmount"));
                        item.setPictureUrl(data.getStringExtra("updatedItemPicture"));
                        item.setChecked(false);

                        adapter.updateItemList((ArrayList<Item>) initialRecipe.getIngredientItems());
                        // item has been found, so break out of the for-loop
                        break;
                    }
                }
            } else {
                // if neither of them are true, do nothing upon return (e.g. if user cancelled)
            }
        }
        // else if it returns from AddRecipeItemActivity, add a new item
        else if (requestCode == ADD_RECIPE_ITEM && resultCode == RESULT_OK && data != null) {
            boolean addItem = data.getBooleanExtra("addItem", false);
            if (addItem) {
                Item newItem = new Item();
                newItem.setUid(data.getStringExtra("itemToAddUid"));
                newItem.setName(data.getStringExtra("addItemName"));
                newItem.setAmount(data.getStringExtra("addItemAmount"));
                newItem.setPictureUrl(data.getStringExtra("addItemPicture"));
                newItem.setChecked(false);

                initialRecipe.getIngredientItems().add(newItem);
                adapter.updateItemList((ArrayList<Item>) initialRecipe.getIngredientItems());
            }
        }
        // else if it returns with Camera/storage, add new image
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            recipeImage = (Bitmap) extras.get("data");

            Glide.with(this)
                    .load(recipeImage)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(editRecipeImageView);
        } else if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                recipeImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                Glide.with(this)
                        .load(recipeImage)
                        .placeholder(R.drawable.default_image)
                        .error(R.drawable.default_image)
                        .into(editRecipeImageView);
            } catch (IOException e) {
                Toast.makeText(this, R.string.editRecipeImageErrorToast, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    // adds the given bitmap into storage, adds the download URL to the newRecipe item and adds that to firebase db
    private void uploadToFirebaseStorage() {
        if (recipeImage != null) {
            // creates a byte array output stream for the image
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            recipeImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageByteData = byteArrayOutputStream.toByteArray();

            // https reference of where the picture should be stored, based on recipe uid
            StorageReference httpsReference = FirebaseStorage
                    .getInstance()
                    .getReferenceFromUrl(storageBase + initialRecipe.getUid() + ".jpg");

            // creates the task and adds listeners
            UploadTask uploadTask = httpsReference.putBytes(imageByteData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // if something went wrong, the user is notified and activity is finished
                    // without saving the recipe
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.editRecipeImageErrorToast) + exception.toString(), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // if it succeeded, get the download link from the ref
                    httpsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // add the download link to the recipe, update the recipe in db and finish
                            initialRecipe.setPictureUrl(uri.toString());

                            editRecipeVM.updateRecipe(initialRecipe);

                            setResult(RESULT_OK);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Something went wrong " + e);
                        }
                    });
                }
            });
        } else{
            // if image hasn't been changed, update other values and save
            editRecipeVM.updateRecipe(initialRecipe);
            setResult(RESULT_OK);
            finish();
        }
    }
}