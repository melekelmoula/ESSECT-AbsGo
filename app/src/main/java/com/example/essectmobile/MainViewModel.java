package com.example.essectmobile;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MainViewModel extends AndroidViewModel {

    private MainModel mainModel;
    private MutableLiveData<String> userRoleLiveData;
    private MutableLiveData<String> deviceTokenLiveData;

    public MainViewModel(Application application) {
        super(application);
        mainModel = new MainModel(application);
        userRoleLiveData = new MutableLiveData<>();
        deviceTokenLiveData = new MutableLiveData<>();
    }

    // Login user
    public void loginUser(String email, String password) {
        mainModel.loginUser(email, password, userRoleLiveData);
    }

    // Check current user
    public void checkCurrentUser() {
        mainModel.checkCurrentUser(userRoleLiveData);
    }

    // Fetch device token
    public void fetchDeviceToken() {
        mainModel.fetchDeviceToken(deviceTokenLiveData);
    }

    // Get user role LiveData
    public LiveData<String> getUserRoleLiveData() {
        return userRoleLiveData;
    }

    // Get device token LiveData
    public LiveData<String> getDeviceTokenLiveData() {
        return deviceTokenLiveData;
    }
}
