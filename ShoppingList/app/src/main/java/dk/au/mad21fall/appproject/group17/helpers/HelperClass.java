package dk.au.mad21fall.appproject.group17.helpers;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

// Helper class
public class HelperClass {
    // helper objects to help transfer data from API
    public class apiResponseObject{
        List<apiResponseItem> results;

        public List<apiResponseItem> getResults() {
            return results;
        }
        public void setResults(List<apiResponseItem> results) {
            this.results = results;
        }
    }
    public class apiResponseItem{
        Integer id;
        String name;
        String image;

        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getImage() {
            return image;
        }
        public void setImage(String image) {
            this.image = image;
        }
    }

    // helper function used to minimize the onscreen keyboard upon searching
    // From: https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext/11656129
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }
}
