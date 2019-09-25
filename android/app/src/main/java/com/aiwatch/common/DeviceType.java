package com.aiwatch.common;

public enum DeviceType {
    HANDSET ("Handset"),
    SMALLTABLET ("Small_Tablet"),
    LARGETABLET ("Large_Tablet"),
    TV ("Tv"),
    UNKNOWN ("unknown");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
