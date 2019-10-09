package com.aiwatch.postprocess;

import com.aiwatch.Logger;
import com.aiwatch.email.GmailSender;
import com.aiwatch.models.AlarmEvent;

public class EmailNotificationManager {

    private static final Logger LOGGER = new Logger();

    public void sendEmail(AlarmEvent alarmEvent){
        try{
            GmailSender gmailSender = new GmailSender("aiwatchmonitor", "");
            //gmailSender.addAttachment("");
            gmailSender.sendMail(alarmEvent.getMessage(), alarmEvent.getMessage(), "aiwatchmonitor", "ramsundark5@gmail.com");
        } catch (Exception e){
            LOGGER.e(e, "Error sending email");
        }
    }
}
