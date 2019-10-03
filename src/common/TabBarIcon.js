import React from 'react';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';

export const tabBarIcon = name => ({ tintColor, horizontal }) => (
    <Icon name={name} color={tintColor} size={horizontal ? 17 : 24} />
);