package com.aiwatch.postprocess;

import com.aiwatch.Logger;
import com.aiwatch.email.GmailSender;
import com.aiwatch.media.db.SettingsDao;
import com.aiwatch.models.AlarmEvent;
import com.aiwatch.models.Settings;

public class EmailNotificationManager {

    private static final Logger LOGGER = new Logger();

    public void sendEmail(AlarmEvent alarmEvent){
        try{
            SettingsDao settingsDao = new SettingsDao();
            Settings settings = settingsDao.getSettings();
            if(settings != null && settings.getEmailUsername() != null && settings.getEmailPassword() != null && settings.getReceiverEmailUsername() != null){
                GmailSender gmailSender = new GmailSender(settings.getEmailUsername(), settings.getEmailPassword());
                gmailSender.addAttachment(alarmEvent.getThumbnailPath(), "event.png");
                gmailSender.sendMail(alarmEvent.getMessage(), alarmEvent.getMessage(), "aiwatchmonitor", settings.getReceiverEmailUsername());
            }
        } catch (Exception e){
            LOGGER.e(e, "Error sending email");
        }
    }
}
