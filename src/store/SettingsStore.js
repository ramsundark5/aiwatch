import { createSlice } from 'redux-starter-kit';

const settingsSlice = createSlice({
    slice: 'settings',

    initialState: {
      isGoogleAccountConnected: false,
      isNotificationEnabled: false,
      isNoAdsPurchased: false,
      smartthingsAccessToken: null,
      smartthingsAccessTokenExpiry: null,
      smartAppEndpoint: null,
      isLoading: false,
      showDeviceLogs: false
    },
      
    reducers: {
      updateSettings(state, action){
        const updatedSettings = action.payload;
        for (const key in updatedSettings) {
            if (updatedSettings.hasOwnProperty(key)) {
              state[key] = updatedSettings[key];
            } 
        }
      },
    }
});

// Extract the action creators object and the reducer
const { actions, reducer } = settingsSlice;
// Extract and export each action creator by name
export const { loadSettings, updateSettings } = actions;
// Export the reducer, either as a default or named export
export default reducer;


  