package com.aiwatch.media.db;

import java.util.List;

import io.objectbox.Box;

public class SettingsDao {

    public Settings putSettings(Settings settings){
        Box<Settings> settingsBox = ObjectBox.get().boxFor(Settings.class);
        //remove all existing entries
        settingsBox.removeAll();

        settingsBox.put(settings);
        return settings;
    }

    public Settings getSettings(){
        Box<Settings> settingsBox = ObjectBox.get().boxFor(Settings.class);
        List<Settings> settingsList = settingsBox.getAll();
        if(settingsList != null && !settingsList.isEmpty()){
            Settings settings = settingsList.get(0);
            return settings;
        }
        return null;
    }
}
