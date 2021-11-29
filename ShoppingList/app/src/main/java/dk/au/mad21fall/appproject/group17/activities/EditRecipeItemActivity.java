package dk.au.mad21fall.appproject.group17.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import dk.au.mad21fall.appproject.group17.Repository;
import dk.au.mad21fall.appproject.group17.R;
import dk.au.mad21fall.appproject.group17.adapters.EditItemAdapter;
import dk.au.mad21fall.appproject.group17.helpers.HelperClass;
import dk.au.mad21fall.appproject.group17.models.Item;

// copy of EditItemActivity, but adjusted to work with EditRecipe instead of ShoppingList.
public class EditRecipeItemActivity extends AppCompatActivity implements EditItemAdapter.IEditItemClickedListener {
    private static final String TAG = "EditRecipeItemActivity";
    private static final String key = "8b470efa70454f8abb68d26f9a634030";
    private static final String base = "https://api.spoonacular.com/";
    private static final String pictureBase = "https://spoonacular.com/cdn/ingredients_100x100/";

    SearchView editItemSearchView;
    EditText editItemAmount;
    RecyclerView editItemRcvList;
    Button editItemSearchButton;
    Button editItemCancelButton;
    Button editItemRemoveButton;
    Button editItemSaveButton;

    EditItemAdapter adapter;
    RecyclerView rcvList;

    RequestQueue queue;

    ArrayList<Item> foundItems = new ArrayList<Item>();
    Item selectedItem;
    String searchItemName;
    Item initialItem;

    Intent data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ingredient);

        adapter = new EditItemAdapter(this, this);
        rcvList = findViewById(R.id.editItemRcv);
        rcvList.setLayoutManager(new LinearLayoutManager(this));
        rcvList.setAdapter(adapter);

        data = getIntent();

        setupUIForEditRecipe(savedInstanceState);
    }

    // identical setup to EditItemActivity, but different methods for getting initial item
    // and methods of buttons
    private void setupUIForEditRecipe(Bundle savedInstanceState) {
        editItemSearchView = findViewById(R.id.editItemSearchView);
        editItemAmount = findViewById(R.id.editItemAmount);
        editItemRcvList = findViewById(R.id.editItemRcv);
        editItemSearchButton = findViewById(R.id.editItemSearchButton);
        editItemCancelButton = findViewById(R.id.editItemCancelButton);
        editItemRemoveButton = findViewById(R.id.editItemRemoveButton);
        editItemSaveButton = findViewById(R.id.editItemSaveButton);

        editItemSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translate(editItemSearchView.getQuery().toString());
            }
        });
        editItemCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelFromEditRecipe();
            }
        });
        editItemRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFromEditRecipe();
            }
        });
        editItemSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFromEditRecipe();
            }
        });

        // gets data from intent instead of from db, as we do not want to update anything yet
        initialItem = new Item();
        initialItem.setUid(data.getStringExtra("itemUid"));
        initialItem.setName(data.getStringExtra("itemName"));
        initialItem.setAmount(data.getStringExtra("itemAmount"));
        initialItem.setPictureUrl(data.getStringExtra("itemPicture"));

        // populates the search/amount fields with the clicked items value
        editItemSearchView.onActionViewExpanded();
        editItemSearchView.setQuery(initialItem.getName(), true);
        editItemSearchView.clearFocus();
        editItemAmount.setText(initialItem.getAmount());
        // and the rcv with the initial item
        foundItems.add(initialItem);
        adapter.updateItemList(foundItems);

        // if not null (phone rotated) redo search to retain list of items
        if (savedInstanceState != null) {
            searchItemName = savedInstanceState.getString("searchName");
            if (searchItemName != null && !searchItemName.isEmpty()) {
                // no need to translate this search, as searchItemName should always be english
                String url = base + "food/ingredients/search?apiKey=" + key + "&query=" + searchItemName + "&number=5";
                Log.d(TAG, url);
                sendRequest(url);
            }
        }
    }

    // if cancelled, simply returns to RecipeEditActivity without doing anything
    private void cancelFromEditRecipe() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("removeItem", false);
        resultIntent.putExtra("updateItem", false);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    // if saved, sends a resultIntent with all the updated values of the selected item
    // nothing saved to db before EditRecipeActivity saves it
    private void saveFromEditRecipe() {
        if (editItemAmount.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.editItemAmountToast, Toast.LENGTH_SHORT).show();
        } else if (selectedItem == null) {
            Toast.makeText(this, R.string.editItemNoChangeToast, Toast.LENGTH_SHORT).show();
            cancelFromEditRecipe();
        } else {
            String amount = editItemAmount.getText().toString();
            selectedItem.setAmount(amount);
            selectedItem.setChecked(false);

            // returns a boolean telling editRecipeActivity to instead update the item
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updateItem", true);
            resultIntent.putExtra("itemToUpdateUid", initialItem.getUid());
            resultIntent.putExtra("updatedItemName", selectedItem.getName());
            resultIntent.putExtra("updatedItemAmount", selectedItem.getAmount());
            resultIntent.putExtra("updatedItemPicture", selectedItem.getPictureUrl());

            Toast.makeText(this, selectedItem.getName() + " " + getResources().getString(R.string.editItemEditedToast), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void removeFromEditRecipe() {
        // returns a boolean telling EditRecipeActivity to remove the item with the uid
        // again nothing is removed from db before EditRecipeActivity saves
        Intent resultIntent = new Intent();
        resultIntent.putExtra("removeItem", true);
        resultIntent.putExtra("itemToRemoveUid", initialItem.getUid());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    // same api call/search as EditItemActivity
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
        ArrayList<Item> items = new ArrayList<Item>();
        for (HelperClass.apiResponseItem item : responseItems.getResults()) {
            Item newItem = new Item();
            newItem.setName(item.getName());
            newItem.setPictureUrl(pictureBase + item.getImage());
            items.add(newItem);
        }

        Item unknownItem = new Item();
        unknownItem.setName(searchItemName);
        unknownItem.setAmount(getResources().getString(R.string.addItemUnknownItem));
        items.add(unknownItem);

        // displays the items found from API
        foundItems = items;
        adapter.updateItemList(foundItems);

        // hides onscreen keyboard for convenience
        HelperClass.hideSoftKeyboard(this);
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
                                searchItemName = translatedText;
                                String url = base + "food/ingredients/search?apiKey=" + key + "&query=" + searchItemName + "&number=5";
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
    public void onEditItemClicked(int index) {
        Log.d(TAG, "Clicked: " + index);
        for (Item item : foundItems) {
            item.setChecked(false);
        }
        foundItems.get(index).setChecked(true);
        adapter.updateItemList(foundItems);
        selectedItem = foundItems.get(index);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("searchName", searchItemName);
        super.onSaveInstanceState(outState);
    }
}