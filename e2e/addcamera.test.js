import CameraViewPage from './pageObjects/CameraViewPage';
import { Config } from './env';

describe('Add Camera', () => {
    beforeEach(async () => {
      await device.reloadReactNative();
    });
    
    it('should be able to add camera', async () => {
      await expect(element(by.id('monitorstatus'))).toBeVisible();
      await expect(element(by.id('addcamerafab'))).toBeVisible();
      //await new CameraViewPage().addManualCamera(Config.camera1);
    });
});