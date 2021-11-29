package dk.au.mad21fall.appproject.group17.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpResponse;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import dk.au.mad21fall.appproject.group17.Repository;
import dk.au.mad21fall.appproject.group17.R;
import dk.au.mad21fall.appproject.group17.adapters.AddItemAdapter;
import dk.au.mad21fall.appproject.group17.helpers.HelperClass;
import dk.au.mad21fall.appproject.group17.models.Item;
import dk.au.mad21fall.appproject.group17.viewmodels.AddItemViewModel;

public class AddItemActivity extends AppCompatActivity implements AddItemAdapter.IAddItemClickedListener {
    private static final String TAG = "AddItemActivity";
    private static final String key = "8b470efa70454f8abb68d26f9a634030";
    private static final String base = "https://api.spoonacular.com/";
    private static final String pictureBase = "https://spoonacular.com/cdn/ingredients_100x100/";

    SearchView addItemSearchView;
    EditText addItemAmount;
    Button addItemSearchButton;
    RecyclerView addItemRcvList;
    Button addItemCancelButton;
    Button addItemAddButton;

    // items found from the api search and name to search by
    ArrayList<Item> foundItems;
    String searchItemName;
    // item selected from the list
    Item selectedItem;

    RequestQueue queue;

    RecyclerView rcvList;
    AddItemAdapter adapter;
    AddItemViewModel addItemVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);

        adapter = new AddItemAdapter(this, this);
        rcvList = findViewById(R.id.addItemRcv);
        rcvList.setLayoutManager(new LinearLayoutManager(this));
        rcvList.setAdapter(adapter);

        addItemVM = new ViewModelProvider(this).get(AddItemViewModel.class);

        setupUI(savedInstanceState);
    }

    // gets all the buttons/textViews from the layout
    private void setupUI(Bundle savedInstanceState) {
        addItemSearchView = findViewById(R.id.addItemSearchView);
        addItemAmount = findViewById(R.id.addItemAmount);
        addItemSearchButton = findViewById(R.id.addItemSearchButton);
        addItemRcvList = findViewById(R.id.addItemRcv);
        addItemCancelButton = findViewById(R.id.addItemCancelButton);
        addItemAddButton = findViewById(R.id.addItemAddButton);

        addItemCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
        addItemAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemToUser();
            }
        });
        addItemSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translate(addItemSearchView.getQuery().toString());
            }
        });

        // opens the search field on start
        addItemSearchView.onActionViewExpanded();
        addItemSearchView.clearFocus();

        // if not null (phone rotated) redo search to retain list of items
        if (savedInstanceState != null) {
            searchItemName = savedInstanceState.getString("searchName");
            // ensure search field isn't empty/null
            if (searchItemName != null && !searchItemName.isEmpty()) {
                // no need to translate this search, as searchItemName should always be english
                String url = base + "food/ingredients/search?apiKey=" + key + "&query=" + searchItemName + "&number=5";
                Log.d(TAG, url);
                sendRequest(url);
            }
        }
    }

    private void cancel() {
        Toast.makeText(this, R.string.addItemCancelledToast, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void addItemToUser() {
        if (addItemSearchView.getQuery().toString().isEmpty()) {
            Toast.makeText(this, R.string.addItemNameToast, Toast.LENGTH_SHORT).show();
        } else if (addItemAmount.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.addItemAmountToast, Toast.LENGTH_SHORT).show();
        } else if (selectedItem == null) {
            Toast.makeText(this, R.string.addItemSelectItemToast, Toast.LENGTH_SHORT).show();
        } else {
            String amount = addItemAmount.getText().toString();
            selectedItem.setAmount(amount);
            selectedItem.setChecked(false);

            addItemVM.addItem(selectedItem);
            Toast.makeText(this, selectedItem.getName() + " " + getResources().getString(R.string.addItemConfirmedToast), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
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

    @Override
    public void onAddItemClicked(int index) {
        Log.d(TAG, "Clicked on: " + index);
        for (Item item : foundItems) {
            item.setChecked(false);
        }
        foundItems.get(index).setChecked(true);
        adapter.updateItemList(foundItems);
        selectedItem = foundItems.get(index);
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("searchName", searchItemName);
        super.onSaveInstanceState(outState);
    }
}
