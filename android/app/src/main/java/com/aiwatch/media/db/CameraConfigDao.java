package com.aiwatch.media.db;

import com.aiwatch.Logger;

import java.util.List;

import io.objectbox.Box;

public class CameraConfigDao {

    private static final Logger LOGGER = new Logger();

    public CameraConfig putCamera(CameraConfig cameraConfig){
        Box<CameraConfig> cameraConfigBox = ObjectBox.get().boxFor(CameraConfig.class);
        cameraConfigBox.put(cameraConfig);
        return cameraConfig;
    }

    public List<CameraConfig> getAllCameras(){
        Box<CameraConfig> cameraConfigBox = ObjectBox.get().boxFor(CameraConfig.class);
        List<CameraConfig> cameras = cameraConfigBox.getAll();
        return cameras;
    }

    public CameraConfig getCamera(long id){
        Box<CameraConfig> cameraConfigBox = ObjectBox.get().boxFor(CameraConfig.class);
        CameraConfig cameraConfig = cameraConfigBox.get(id);
        return cameraConfig;
    }

    public void deleteCamera(long cameraId){
        Box<CameraConfig> cameraConfigBox = ObjectBox.get().boxFor(CameraConfig.class);
        cameraConfigBox.remove(cameraId);
    }

    public void updateCameraStatus(long cameraId, boolean disconnected){
        try{
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            CameraConfig cameraToUpdate = cameraConfigDao.getCamera(cameraId);
            cameraToUpdate.setDisconnected(disconnected);
            cameraConfigDao.putCamera(cameraToUpdate);
        }catch(Exception e){
            LOGGER.e(e, "error updating camera status");
        }
    }
}
