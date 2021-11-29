package dk.au.mad21fall.appproject.group17.viewmodels;

import androidx.lifecycle.ViewModel;

import dk.au.mad21fall.appproject.group17.Repository;
import dk.au.mad21fall.appproject.group17.models.Item;
import dk.au.mad21fall.appproject.group17.models.Recipe;

public class AddRecipeViewModel extends ViewModel {
    Repository repo = Repository.getInstance();

    public void addRecipe(Recipe recipe){
        repo.addRecipe(recipe);
    }
}
