const EDIT_CAMERA_SCROLL_VIEW = 'EDIT_CAMERA_SCROLL_VIEW';
const SCROLL_SPEED = 100;
export default class CameraViewPage {
    get addCameraFAB() {
        return element(by.label("ADD_CAMERA_FAB"));
    }

    get addCameraButton() {
        return element(by.label("ADD_MANUAL_CAMERA"));
    }

    get scanCameraButton() {
        return element(by.label("SCAN_CAMERA"));
    }

    get addCameraTestButton(){
        return element(by.id("ADD_CAMERA_BUTTON"));
    }

    get cameraNameTextField() {
        return element(by.id("CAMERA_NAME"));
    }

    get cameraBrandTextField() {
        return element(by.id("CAMERA_BRAND"));
    }

    get cameraModelextField() {
        return element(by.id("CAMERA_MODEL"));
    }

    get videoUrlTextField() {
        return element(by.id("VIDEO_URL"));
    }

    get usernameTextField() {
        return element(by.id("USERNAME"));
    }

    get passwordTextField() {
        return element(by.id("PASSWORD"));
    }

    get testConectionButton() {
        return element(by.id("TEST_CONNECTION"));
    }

    get saveCameraButton() {
        return element(by.id("SAVE_CAMERA"));
    }

    async addManualCamera(cameraConfig) {
       /* await expect(this.addCameraFAB).toBeVisible();
       await this.addCameraFAB.tap();
       await expect(this.addCameraButton).toBeVisible();
       await this.addCameraButton.tap(); */

       await expect(this.addCameraTestButton).toBeVisible();
       await this.addCameraTestButton.tap();

       await expect(this.cameraNameTextField).toBeVisible();
       await this.cameraNameTextField.replaceText(cameraConfig.name);
       await this.cameraBrandTextField.replaceText(cameraConfig.brand);
       await this.cameraModelextField.replaceText(cameraConfig.model);

       await waitFor(this.videoUrlTextField).toBeVisible().whileElement(by.id(EDIT_CAMERA_SCROLL_VIEW)).scroll(300, 'down');
       await this.videoUrlTextField.tap();
       await this.videoUrlTextField.replaceText(cameraConfig.videoUrl);

       await waitFor(this.usernameTextField).toBeVisible().whileElement(by.id(EDIT_CAMERA_SCROLL_VIEW)).scroll(SCROLL_SPEED, 'down');
       await this.usernameTextField.tap();
       await this.usernameTextField.replaceText(cameraConfig.username);

       await waitFor(this.passwordTextField).toBeVisible().whileElement(by.id(EDIT_CAMERA_SCROLL_VIEW)).scroll(SCROLL_SPEED, 'down');
       await this.passwordTextField.tap();
       await this.passwordTextField.typeText(cameraConfig.password);

       await waitFor(this.saveCameraButton).toBeVisible().whileElement(by.id(EDIT_CAMERA_SCROLL_VIEW)).scroll(SCROLL_SPEED, 'down');
       //await this.saveCameraButton.tap();
    }
}