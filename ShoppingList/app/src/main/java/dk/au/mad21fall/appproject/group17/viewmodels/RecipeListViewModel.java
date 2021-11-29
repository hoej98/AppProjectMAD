package dk.au.mad21fall.appproject.group17.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import dk.au.mad21fall.appproject.group17.Repository;
import dk.au.mad21fall.appproject.group17.models.Item;
import dk.au.mad21fall.appproject.group17.models.User;

public class RecipeListViewModel extends ViewModel {
    Repository repo = Repository.getInstance();

    public LiveData<User> getUser(){
        return repo.getUser();
    }
}
