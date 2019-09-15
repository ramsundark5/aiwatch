export default class CameraViewPage {
    get addCameraFAB() {
        return element(by.id("ADD_CAMERA_FAB"));
    }

    get addCameraButton() {
        return element(by.id("ADD_MANUAL_CAMERA"));
    }

    get scanCameraButton() {
        return element(by.id("SCAN_CAMERA"));
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
       //await expect(this.addCameraFAB).toBeVisible();
       await this.addCameraFAB.tap();
       await expect(this.addCameraButton).toBeVisible();
       await this.addCameraButton.tap();
       await expect(this.cameraNameTextField).toBeVisible();
       await this.cameraNameTextField.typeText(cameraConfig.name);
       await this.cameraBrandTextField.typeText(cameraConfig.brand);
       await this.cameraModelextField.typeText(cameraConfig.model);
       await this.videoUrlTextField.typeText(cameraConfig.videoUrl);
       await this.usernameTextField.typeText(cameraConfig.username);
       await this.passwordTextField.typeText(cameraConfig.password);
       await this.saveCameraButton.tap();
    }
}