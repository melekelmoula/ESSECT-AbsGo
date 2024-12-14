package com.example.essectmobile.Firebase;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Map;

public class FirebaseHelper {

    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Save FCM Token
    public static void saveProfessorFcmToken(String uid, final SaveTokenCallback callback) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String fcmToken = task.getResult();
                if (fcmToken != null) {
                    db.collection("users").document(uid)
                            .update("fcmToken", fcmToken)
                            .addOnSuccessListener(aVoid -> callback.onSuccess("FCM token saved successfully"))
                            .addOnFailureListener(e -> callback.onFailure("Error saving FCM token: " + e.getMessage()));
                }
            } else {
                callback.onFailure("Error getting FCM token: " + task.getException().getMessage());
            }
        });
    }

    // Fetch Notifications
    public static void fetchNotifications(String uid, final FetchNotificationsCallback callback) {
        db.collection("notification")
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            Object notificationsObj = document.get("notifications");
                            if (notificationsObj != null && notificationsObj instanceof List) {
                                List<Map<String, Object>> notifications = (List<Map<String, Object>>) notificationsObj;
                                callback.onSuccess(notifications);
                            } else {
                                callback.onFailure("No notifications array found");
                            }
                        } else {
                            callback.onFailure("Document not found");
                        }
                    } else {
                        callback.onFailure("Error fetching notifications");
                    }
                });
    }

    // Update Reclamation Status in Notification
    public static void updateNotificationReclamation(String userId, String absenceno, final NotificationUpdateCallback callback) {
        db.collection("notification")
                .document(userId)
                .get()
                .addOnSuccessListener(notificationDoc -> {
                    if (notificationDoc.exists()) {
                        List<Map<String, Object>> notifications = (List<Map<String, Object>>) notificationDoc.get("notifications");

                        if (notifications != null && !notifications.isEmpty()) {
                            for (Map<String, Object> notification : notifications) {
                                if (absenceno.equals(notification.get("AbsenceNo"))) {
                                    notification.put("reclamation", "pending");
                                    db.collection("notification")
                                            .document(userId)
                                            .update("notifications", notifications)
                                            .addOnSuccessListener(aVoid -> callback.onSuccess("Notification updated"))
                                            .addOnFailureListener(e -> callback.onFailure("Error updating notification: " + e.getMessage()));
                                    break;
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Error fetching notifications: " + e.getMessage()));
    }

    // Reclamation Submission
    public static void submitReclamation(String reclamationId, Map<String, Object> reclamation, final ReclamationCallback callback) {
        db.collection("reclamations")
                .document(reclamationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("reclamations")
                                .document(reclamationId)
                                .update("reclamations", FieldValue.arrayUnion(reclamation))
                                .addOnSuccessListener(aVoid -> callback.onSuccess("Reclamation added successfully"))
                                .addOnFailureListener(e -> callback.onFailure("Error updating reclamation: " + e.getMessage()));
                    } else {
                        db.collection("reclamations")
                                .document(reclamationId)
                                .set(Map.of("reclamations", List.of(reclamation)))
                                .addOnSuccessListener(aVoid -> callback.onSuccess("Reclamation submitted successfully"))
                                .addOnFailureListener(e -> callback.onFailure("Error saving reclamation: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Error fetching document: " + e.getMessage()));
    }

    // Callback Interfaces
    public interface SaveTokenCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface FetchNotificationsCallback {
        void onSuccess(List<Map<String, Object>> notifications);
        void onFailure(String error);
    }

    public interface NotificationUpdateCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface ReclamationCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
}