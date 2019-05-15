package com.aiwatch.media.db;

import java.util.Date;
import java.util.List;

import io.objectbox.Box;

public class AlarmEventDao {
    public AlarmEvent putEvent(AlarmEvent alarmEvent){
        Box<AlarmEvent> alarmEventBox = ObjectBox.get().boxFor(AlarmEvent.class);
        alarmEventBox.put(alarmEvent);
        return alarmEvent;
    }

    public List<AlarmEvent> getEventsForDateRange(final Date endDate, final Date startDate){
        Box<AlarmEvent> alarmEventBox = ObjectBox.get().boxFor(AlarmEvent.class);
        List<AlarmEvent> alarmEvents = alarmEventBox.query()
                .between(AlarmEvent_.date, endDate, startDate)
                .orderDesc(AlarmEvent_.id)
                .build().find();
        return alarmEvents;
    }

    public AlarmEvent getEvent(long id){
        Box<AlarmEvent> alarmEventBox = ObjectBox.get().boxFor(AlarmEvent.class);
        AlarmEvent alarmEvent = alarmEventBox.get(id);
        return alarmEvent;
    }

    public void deleteEvent(long id){
        Box<AlarmEvent> alarmEventBox = ObjectBox.get().boxFor(AlarmEvent.class);
        alarmEventBox.remove(id);
    }
}
