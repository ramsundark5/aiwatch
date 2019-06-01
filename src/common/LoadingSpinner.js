
import React from 'react';
import { ActivityIndicator } from 'react-native-paper';

const withSpinner = Comp => ({ isLoading, children, ...props }) => {
  if (isLoading) {
    return (
        <ActivityIndicator animating={isLoading} size={36} 
             style={{flex: 1, justifyContent: 'center'}} />
    );
  } else {
    return (
      <Comp {...props}>
        {children}
      </Comp>
    )
  }
};

export default withSpinner;