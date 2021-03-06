import CameraViewPage from './pageObjects/CameraViewPage';
import { Config } from './env';

describe('Add Camera', () => {
    beforeEach(async () => {
    });
    
    it('should be able to add camera', async () => {
      await expect(element(by.id('monitorstatus'))).toBeVisible();
      await new CameraViewPage().addManualCamera(Config.camera1);
    });
});