package com.aiwatch.ai;

public enum Events {

    PERSON_DETECTED_EVENT,

    ANIMAL_DETECTED_EVENT,

    VEHICLE_DETECTED_EVENT,

    OTHER_DETECTED_EVENT;

    public String getName() {
        return toString().toUpperCase();
    }
}
