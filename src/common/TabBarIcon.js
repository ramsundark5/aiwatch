import React from 'react';
import Ionicons from 'react-native-vector-icons/Ionicons';

export const tabBarIcon = name => ({ tintColor, horizontal }) => (
    <Ionicons name={name} color={tintColor} size={horizontal ? 17 : 24} />
);