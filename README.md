# aiwatch

Todo

- [ ] Region of interest
- [x] Video player fullscreen
- [x] Sync to dropbox/google drive
- [x] Firebase integration and sync
- [x] Styling cleanup
- [x] Background service
- [x] Reconnect logic
- [x] UI add camera -> test connection
- [x] Crash reporting and logging
- [x] Ads
- [x] Payment
- [x] code obfuscation
- [x] Remote Notifications
- [ ] Encryption
- [x] Notification icons
- [x] Sync events and camera config
- [ ] Splash screen
- [ ] Face recognition
- [ ] Smartthings integration or IFTTT


Command to record video in chunks

String recordCommand = " -codec copy -flags +global_header -f segment -strftime 1 -segment_time 30 -segment_format_options movflags=+faststart -reset_timestamps 1 " + videoPath + "/" + cameraId +"-%Y%m%d_%H:%M:%S.mp4 ";

Other useful dev commands

adb shell input keyevent 82
