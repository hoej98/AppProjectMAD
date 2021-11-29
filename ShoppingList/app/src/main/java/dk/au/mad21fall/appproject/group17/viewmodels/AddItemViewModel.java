package dk.au.mad21fall.appproject.group17.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import dk.au.mad21fall.appproject.group17.Repository;
import dk.au.mad21fall.appproject.group17.models.Item;

public class AddItemViewModel extends ViewModel {
    Repository repo = Repository.getInstance();

    public void addItem(Item item){
        repo.addItem(item);
    }
}
