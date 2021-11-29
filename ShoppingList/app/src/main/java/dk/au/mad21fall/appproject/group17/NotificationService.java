package dk.au.mad21fall.appproject.group17;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.au.mad21fall.appproject.group17.models.Recipe;
import dk.au.mad21fall.appproject.group17.models.User;

public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    public static final String SERVICE_CHANNEL = "notificationServiceChannel";
    public static final int NOTIFICATION_ID = 60;

    NotificationManager notificationManager;
    ExecutorService execService;

    boolean readyToStart = false;
    boolean canContinue = false;

    int notificationCounter = 0;

    ArrayList<Recipe> recipes = new ArrayList<Recipe>();

    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Repository repo = Repository.getInstance();

        // gets the recipes from the current user
        repo.getUser().observeForever(new Observer<User>() {
            @Override
            public void onChanged(User user) {
                recipes = (ArrayList<Recipe>) user.getRecipes();

                // ensures it doesn't start until data has been received from firebase
                readyToStart = true;
                Log.d(TAG, "Data received, ready to start");
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(SERVICE_CHANNEL, "Notification Service", NotificationManager.IMPORTANCE_LOW);
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        // creates a default notification to start the service with
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), SERVICE_CHANNEL)
                .setContentTitle("Recipes")
                .setContentText("Get recipe recommendations here!")
                .setSmallIcon(R.drawable.kurv)
                .setTicker("New recommendation!")
                .build();

        startForeground(NOTIFICATION_ID, notification);

        if (!canContinue) {
            canContinue = true;
            sendNotification();
        }

        return START_STICKY;
    }

    private void sendNotification() {
        if (execService == null) {
            execService = Executors.newSingleThreadExecutor();
        }

        execService.submit(new Runnable() {
            @Override
            public void run() {
                // only starts if it has the data from firebase
                if (readyToStart) {
                    notificationCounter++;
                    Log.d(TAG, "Notifications sent so far: " + notificationCounter);

                    try {
                        // gets a random recipe from index in users recipe list
                        Random rand = new Random();
                        int index = rand.nextInt(recipes.size());
                        Recipe randRecipe = recipes.get(index);

                        // creates notification based on the random recipe
                        Notification notification = new NotificationCompat.Builder(getApplicationContext(), SERVICE_CHANNEL)
                                .setContentTitle("New recommendation!")
                                .setContentText("Recommended recipe: " + randRecipe.getName())
                                .setSmallIcon(R.drawable.kurv)
                                .setTicker("New recommendation!")
                                .build();

                        // starts/updates the notification with the random recipe
                        notificationManager.notify(NOTIFICATION_ID, notification);

                        // waits for 60 seconds before doing it over again
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "something went wrong", e);
                    }
                }

                // keeps sending the notifications
                if (canContinue) {
                    sendNotification();
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        readyToStart = false;
        canContinue = false;
        super.onDestroy();
    }
}
