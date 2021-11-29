package dk.au.mad21fall.appproject.group17.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import dk.au.mad21fall.appproject.group17.Repository;
import dk.au.mad21fall.appproject.group17.R;
import dk.au.mad21fall.appproject.group17.helpers.HelperClass;
import dk.au.mad21fall.appproject.group17.helpers.RecipeSpinner;
import dk.au.mad21fall.appproject.group17.models.Item;
import dk.au.mad21fall.appproject.group17.models.Recipe;
import dk.au.mad21fall.appproject.group17.viewmodels.AddRecipeViewModel;

public class AddRecipeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
    private static final String TAG = "AddRecipeActivity";
    private static final String key = "8b470efa70454f8abb68d26f9a634030";
    private static final String base = "https://api.spoonacular.com/";
    private static final String pictureBase = "https://spoonacular.com/cdn/ingredients_100x100/";
    private static final String storageBase = "gs://test123-c52e1.appspot.com/";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_IMAGE = 11;

    EditText addRecipeIngredient;
    EditText addRecipeAmount;
    EditText addRecipeInstructions;
    Button addRecipeAddIngredientButton;
    Button addRecipeAddButton;
    Button addRecipeCancelButton;
    RecipeSpinner addRecipeSpinner;
    AddRecipeViewModel addRecipeVM;

    ArrayList<String> recipeItemsNames = new ArrayList<>();
    ArrayList<Item> recipeItems = new ArrayList<>();
    String searchName;
    String searchAmount;
    RequestQueue queue;

    // Adapter used for spinner
    ArrayAdapter<String> adapter;
    // check used for spinner onItemSelected
    Boolean userAction = false;
    Recipe newRecipe = new Recipe();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        addRecipeVM = new ViewModelProvider(this).get(AddRecipeViewModel.class);

        setupUI(savedInstanceState);
    }

    private void setupUI(Bundle savedInstanceState) {
        addRecipeIngredient = findViewById(R.id.addRecipeIngredient);
        addRecipeAmount = findViewById(R.id.addRecipeAmount);
        addRecipeInstructions = findViewById(R.id.addRecipeInstructions);
        addRecipeAddIngredientButton = findViewById(R.id.addRecipeIngredientButton);
        addRecipeAddButton = findViewById(R.id.addRecipeAddButton);
        addRecipeCancelButton = findViewById(R.id.addRecipeCancelButton);
        addRecipeSpinner = findViewById(R.id.addRecipeSpinner);

        // set initial values for spinner/adapter
        ArrayList<String> reversedArray = new ArrayList<String>(recipeItemsNames);
        Collections.reverse(reversedArray);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, reversedArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addRecipeSpinner.setAdapter(adapter);
        addRecipeSpinner.setOnTouchListener(this);
        addRecipeSpinner.setOnItemSelectedListener(this);

        addRecipeAddIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addRecipeIngredient.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.addRecipeIngredientToast, Toast.LENGTH_SHORT).show();
                } else if (addRecipeAmount.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.addRecipeAmountToast, Toast.LENGTH_SHORT).show();
                } else {
                    translate(addRecipeIngredient.getText().toString());
                    searchAmount = addRecipeAmount.getText().toString();
                }
            }
        });

        addRecipeAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRecipe();
            }
        });

        addRecipeCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        if (savedInstanceState != null){
            ArrayList<Item> tempItems = savedInstanceState.getParcelableArrayList("recipeItems");
            if (tempItems != null && !tempItems.isEmpty()){
                recipeItems = tempItems;
                for (Item item : recipeItems){
                    recipeItemsNames.add(item.getName());
                }
                refreshSpinner();
            }
        }
    }

    private void addRecipe() {
        if (recipeItems == null || recipeItems.isEmpty()) {
            Toast.makeText(this, R.string.addRecipeRecipeToast, Toast.LENGTH_SHORT).show();
        } else {
            // methods used to set up and show the image dialog and name dialog respectively
            AlertDialog imageDialog = setupImageDialog(this, newRecipe);
            setupAndShowNameDialog(this, imageDialog, newRecipe);
        }
    }

    // setup method for the image dialog
    private AlertDialog setupImageDialog(Context cont, Recipe newRecipe){
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
                } catch (ActivityNotFoundException e){
                    // if no camera, simply save without
                    dialog.cancel();
                    Toast.makeText(getApplicationContext(), R.string.addRecipeCameraError, Toast.LENGTH_SHORT).show();

                    addRecipeVM.addRecipe(newRecipe);
                    // closes the activity afterwards
                    setResult(RESULT_OK);
                    ((Activity) cont).finish();
                }
            }
        });

        // Set up the "Storage" button
        imageDialogBuilder.setNegativeButton(R.string.addRecipeImageDialogNeutral, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if user chooses "Storage", start storage activity to select a picture
                Intent selectFromStorageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                selectFromStorageIntent.setType("image/*");
                try {
                    startActivityForResult(selectFromStorageIntent, SELECT_IMAGE);
                } catch (ActivityNotFoundException e){
                    // if no storage found, simply save without
                    dialog.cancel();
                    Toast.makeText(getApplicationContext(), R.string.addRecipeStorageError, Toast.LENGTH_SHORT).show();

                    addRecipeVM.addRecipe(newRecipe);
                    // closes the activity afterwards
                    setResult(RESULT_OK);
                    ((Activity) cont).finish();
                }
            }
        });

        // Set up the "No thanks" button
        imageDialogBuilder.setNeutralButton(R.string.addRecipeImageDialogNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if no, save the recipe with no image
                dialog.cancel();

                addRecipeVM.addRecipe(newRecipe);

                // closes the activity afterwards
                setResult(RESULT_OK);
                ((Activity) cont).finish();
            }
        });
        // create imageDialog
        imageDialog = imageDialogBuilder.create();
        return imageDialog;
    }

    private void setupAndShowNameDialog(Context cont, AlertDialog imageDialog, Recipe newRecipe){
        // dialog box prompting the user to enter recipe name
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.addRecipeDialogName);

        // input field for the user to write the name
        EditText input = new EditText(this);
        builder.setView(input);

        // Set up the OK/Cancel buttons
        builder.setPositiveButton(R.string.addRecipeDialogOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(R.string.addRecipeDialogCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        // overwrite the OK button such that it is only closed when recipe has been successfully added.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.addRecipeAddName, Toast.LENGTH_SHORT).show();
                } else {
                    // save the recipe if the player clicks OK
                    newRecipe.setName(input.getText().toString());
                    newRecipe.setIngredientItems(recipeItems);
                    newRecipe.setDirections(addRecipeInstructions.getText().toString());

                    // dismiss current dialog and open dialog asking for image
                    dialog.dismiss();
                    imageDialog.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if request code is REQUEST_IMAGE_CAPTURE, must have returned from Camera intent
        // and handle that accordingly
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            uploadToFirebaseStorage(imageBitmap);
        }
        // else if SELECT_IMAGE, must have returned form storage intent, and handle accordingly
        else if(requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                uploadToFirebaseStorage(imageBitmap);
            } catch (IOException e) {
                Toast.makeText(this, R.string.addRecipeError, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    // adds the given bitmap into storage, adds the download URL to the newRecipe item and adds that to firebase db
    private void uploadToFirebaseStorage(Bitmap imageBitmap){
        // creates a byte array output stream for the image
        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageByteData = byteArrayOutputStream.toByteArray();

        // https reference of where the picture should be stored
        StorageReference httpsReference = FirebaseStorage
                .getInstance()
                .getReferenceFromUrl(storageBase + newRecipe.getUid() + ".jpg");

        // creates the task and adds listeners
        UploadTask uploadTask = httpsReference.putBytes(imageByteData);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // if something went wrong, the user is notified and activity is finished
                // without saving the recipe
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.addRecipeError) + exception.toString(), Toast.LENGTH_SHORT).show();
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
                        // add the download link to the recipe, add recipe to db and finish
                        newRecipe.setPictureUrl(uri.toString());

                        addRecipeVM.addRecipe(newRecipe);

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
    }

    private void cancel() {
        Toast.makeText(this, R.string.addRecipeCancelledToast, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void sendRequest(String url) {
        if (queue == null) {
            queue = Volley.newRequestQueue(this);
        }

        StringRequest sr = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response);

                        parseJson(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err) {
                Log.d(TAG, "Something went wrong: ", err);
            }
        }
        );

        queue.add(sr);
    }

    private void parseJson(String json) {
        // sets up the GsonBuilder
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        // converts response json to helper object
        HelperClass.apiResponseObject responseItems = gson.fromJson(json, HelperClass.apiResponseObject.class);

        // converts items in helper object to Item objects
        Item newItem = new Item();
        if (!responseItems.getResults().isEmpty()) {
            // get found item from response
            HelperClass.apiResponseItem apiItem = responseItems.getResults().get(0);
            // set values on Item object
            newItem.setName(apiItem.getName());
            newItem.setPictureUrl(pictureBase + apiItem.getImage());
            newItem.setAmount(searchAmount);
        } else {
            newItem.setName(searchName);
            newItem.setAmount(searchAmount);
        }

        recipeItems.add(newItem);
        recipeItemsNames.add(newItem.getName());
        refreshSpinner();

        Log.d(TAG, "Size of item array: " + recipeItems.size());

        // clears input fields and hides onscreen keyboard for convenience
        addRecipeIngredient.setText(null);
        addRecipeAmount.setText(null);
        HelperClass.hideSoftKeyboard(this);
    }

    // refresh spinner with new values
    public void refreshSpinner(){
        // reverse list to ensure newest item on top
        ArrayList<String> reversedList = new ArrayList<String>(recipeItemsNames);
        Collections.reverse(reversedList);
        // clears current list and adds new
        adapter.clear();
        adapter.addAll(reversedList);
        adapter.notifyDataSetChanged();
    }

    // since onItemSelected is called in many cases (such as when initialized),
    // a check is made to only react on actions made by the user
    // so when an item is touched by the user, set variable to true
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        userAction = true;
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        // as mentioned above (onTouch), should only react to user inputs
        if (userAction){
            // since the list of item names is reversed, get reversed recipeItems
            ArrayList<Item> reversedItemArray = new ArrayList<Item>(recipeItems);
            Collections.reverse(reversedItemArray);
            Item itemToDelete = reversedItemArray.get(position);

            // dialog box prompting the user to enter recipe name
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.addRecipeItemDelete) + " " + itemToDelete.getName() + "?");

            // Set up the OK/Cancel buttons
            builder.setPositiveButton(R.string.addRecipeDialogOK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    recipeItems.remove(itemToDelete);
                    recipeItemsNames.remove(itemToDelete.getName());

                    refreshSpinner();
                }
            });
            builder.setNegativeButton(R.string.addRecipeDialogCancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // create and show dialog
            AlertDialog dialog = builder.create();
            dialog.show();

            // once done, sets variable to false again
            userAction = false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // do nothing
    }

    // used to translate text for api search
    public void translate(String text) {
        // options used to set source language (danish) and destination language (english)
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.DA)
                        .setTargetLanguage(FirebaseTranslateLanguage.EN)
                        .build();
        final FirebaseTranslator Translator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        // downloads the language models
        Translator.downloadModelIfNeeded()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "translator models downloaded");
                        // if succeeded, translate the text
                        translateText(Translator, text);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // if fails, print error message
                        Log.d(TAG, "Something went wrong: " + e);
                    }
                });
    }

    public void translateText(FirebaseTranslator langTranslator, String text){
        // translate text with the given translator
        langTranslator.translate(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                Log.d(TAG, text + " = " + translatedText);
                                // if translation succeeds, make the api call with the translated text
                                searchName = translatedText;
                                String url = base + "food/ingredients/search?apiKey=" + key + "&query=" + searchName + "&number=1";
                                Log.d(TAG, url);
                                sendRequest(url);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Something went wrong: " + e);
                            }
                        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("recipeItems", recipeItems);
        super.onSaveInstanceState(outState);
    }}

