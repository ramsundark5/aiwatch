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
      },

      updateStatus(state, action){
        const cameraConfig = action.payload;
        let existingCameraIndex = state.cameras.findIndex(current => current.id === cameraConfig.id); 
        if (existingCameraIndex >= 0) {
          state.cameras[existingCameraIndex].disconnected = cameraConfig.disconnected;
        }
      },
    }
});

// Extract the action creators object and the reducer
const { actions, reducer } = camerasSlice;
// Extract and export each action creator by name
export const { loadCameras, editCamera, deleteCamera, updateStatus } = actions;
// Export the reducer, either as a default or named export
export default reducer;


  