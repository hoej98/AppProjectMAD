package dk.au.mad21fall.appproject.group17.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dk.au.mad21fall.appproject.group17.R;
import dk.au.mad21fall.appproject.group17.models.Recipe;
import dk.au.mad21fall.appproject.group17.models.User;
import dk.au.mad21fall.appproject.group17.viewmodels.AddItemViewModel;
import dk.au.mad21fall.appproject.group17.viewmodels.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int REQUEST_LOGIN = 2364;

    private FirebaseAuth auth;

    Button signInButton;

    LoginViewModel loginVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        loginVM = new ViewModelProvider(this).get(LoginViewModel.class);

        signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOn();
            }
        });
    }

    private void signOn() {
        // if user is logged in, goes to shoppingListActivity
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User uid: " + currentUser.getUid());
            Log.d(TAG, "User name: " + currentUser.getDisplayName());
            Log.d(TAG, "User email: " + currentUser.getEmail());

            Toast.makeText(this, getResources().getString(R.string.loginWelcomeBack) + " " + currentUser.getDisplayName(), Toast.LENGTH_SHORT).show();
            goToShoppingList();
        }
        // else starts the firebase login activity
        else {
            // provider telling which login builder to use - in this case only email
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build()
            );

            // starts the login activity build by Firebase
            startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    REQUEST_LOGIN);
        }
    }

    // once returned from login activity, checks everything is ok and sends the user to shoppingList
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                // checks if it is the first time the user logs in, which case they get added to the database collection
                FirebaseUser currentUser = auth.getCurrentUser();
                IdpResponse response = IdpResponse.fromResultIntent(data);

                if (response.isNewUser()){
                    User newUser = new User();
                    newUser.setUid(currentUser.getUid());
                    newUser.setEmail(currentUser.getEmail());
                    newUser.setName(currentUser.getDisplayName());

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("Users").document(newUser.getUid()).set(newUser);
                    Log.d(TAG, "New user added with ID: " + newUser.getUid());
                }

                // after successful login, goes on to shoppingList
                Toast.makeText(this, getResources().getString(R.string.loginWelcome) + " " + currentUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                goToShoppingList();
            }
        }
    }

    // Starts new activity that sends the user to the shoppingList activity, if they are logged in.
    private void goToShoppingList() {
        // loads the logged in user
        loginVM.loadUserData();

        Intent shoppingListIntent = new Intent(this, ShoppingListActivity.class);
        startActivity(shoppingListIntent);
        // also finishes this activity, as this is no longer used
        finish();
    }
}