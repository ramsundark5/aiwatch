import { createSlice } from 'redux-starter-kit';

const camerasSlice = createSlice({
    slice: 'cameras',

    initialState: {
      isFull: false,
      cameras: []
    },
      
    reducers: {
      loadCameras(state, action){
        state.cameras = action.payload;
      },

      editCamera(state, action) {
        const updatedCameraConfig = action.payload;
        let existingCameraIndex = state.cameras.findIndex(current => current.id === updatedCameraConfig.id); 
        if (existingCameraIndex === -1) {
            state.cameras.push(updatedCameraConfig);
        } else {
            state.cameras[existingCameraIndex] = updatedCameraConfig;
        }
      },

      deleteCamera (state, action) {
        let selectedCameraIndex = state.cameras.findIndex(current => current.id === action.payload); 
        state.cameras.splice(selectedCameraIndex, 1); 
      }
    }
});

// Extract the action creators object and the reducer
const { actions, reducer } = camerasSlice;
// Extract and export each action creator by name
export const { loadCameras, editCamera, deleteCamera } = actions;
// Export the reducer, either as a default or named export
export default reducer;


  