package dk.au.mad21fall.appproject.group17.helpers;

import android.content.Context;
import android.util.AttributeSet;

// subclass of Spinner class
// used to make onItemSelected be called even if the already selected item is selected again
// inspired by: https://stackoverflow.com/questions/19734099/android-spinner-onitemselected-not-called-with-the-same-item
public class RecipeSpinner extends androidx.appcompat.widget.AppCompatSpinner {
    // constructors just calling parents constructors
    public RecipeSpinner(Context context) {
        super(context);
    }
    public RecipeSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // setSelection is overridden to add the functionality
    @Override
    public void
    setSelection(int position, boolean animate) {
        // variable saving whether the selected item is same as the currently selected item
        boolean sameItem = (position == getSelectedItemPosition());
        super.setSelection(position, animate);
        // if so, manually call onItemSelectedListener as this is not done automatically
        if (sameItem) {
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }
    // same as above, but without animate
    @Override
    public void
    setSelection(int position) {
        // variable saving whether the selected item is same as the currently selected item
        boolean sameItem = (position == getSelectedItemPosition());
        super.setSelection(position);
        // if so, manually call onItemSelectedListener as this is not done automatically
        if (sameItem) {
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }
}