package de.hka.ws2425.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    // TODO: Implement the ViewModel

    private MutableLiveData<String> result;

    public MainViewModel() {
        this.result = new MutableLiveData<>();
    }

    public void doSomething() {
        Log.d("MainViewModel","Button clicked");

        this.result.setValue("Some incredible good value!");
    }

    public LiveData<String> getResult(){
        return this.result;
    }
}