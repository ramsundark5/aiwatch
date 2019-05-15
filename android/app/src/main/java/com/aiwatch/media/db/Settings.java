package com.aiwatch.media.db;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Settings {

    @Id
    private long id;
    private boolean isGoogleAccountConnected;
    private boolean notificationEnabled;

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
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
}
