package com.example.essectmobile.Professor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.essectmobile.Firebase.FirebaseHelper;

import java.util.List;
import java.util.Map;

public class ProfessorMainViewModel extends ViewModel {

    private MutableLiveData<List<Map<String, Object>>> notifications = new MutableLiveData<>();

    public LiveData<List<Map<String, Object>>> getNotifications() {
        return notifications;
    }

    public void fetchNotifications(String uid) {
        FirebaseHelper.fetchNotifications(uid, new FirebaseHelper.FetchNotificationsCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> notificationsList) {
                notifications.setValue(notificationsList);
            }

            @Override
            public void onFailure(String error) {
                notifications.setValue(null);
            }
        });
    }
}
