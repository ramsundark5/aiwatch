package com.aiwatch.media.db;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Settings {

    @Id
    private long id;
    private boolean isGoogleAccountConnected;
    private boolean isNotificationEnabled;
    private boolean isNoAdsPurchased;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isGoogleAccountConnected() {
        return isGoogleAccountConnected;
    }

    public void setGoogleAccountConnected(boolean googleAccountConnected) {
        isGoogleAccountConnected = googleAccountConnected;
    }

    public boolean isNotificationEnabled() {
        return isNotificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        isNotificationEnabled = notificationEnabled;
    }

    public boolean isNoAdsPurchased() {
        return isNoAdsPurchased;
    }

    public void setNoAdsPurchased(boolean noAdsPurchased) {
        isNoAdsPurchased = noAdsPurchased;
    }
}
