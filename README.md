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
- [x] Splash screen
- [ ] Face recognition
- [ ] Smartthings integration or IFTTT


Command to record video in chunks

String recordCommand = " -codec copy -flags +global_header -f segment -strftime 1 -segment_time 30 -segment_format_options movflags=+faststart -reset_timestamps 1 " + videoPath + "/" + cameraId +"-%Y%m%d_%H:%M:%S.mp4 ";

Other useful dev commands

adb shell input keyevent 82


Splash screen generator

https://www.norio.be/android-feature-graphic-generator/?config=%7B%22background%22%3A%7B%22color%22%3A%22%2388beff%22%2C%22gradient%22%3A%7B%22type%22%3A%22radial%22%2C%22radius%22%3A%22600%22%2C%22angle%22%3A%22vertical%22%2C%22color%22%3A%22%23000000%22%7D%7D%2C%22title%22%3A%7B%22text%22%3A%22aiwatch%22%2C%22position%22%3A168%2C%22color%22%3A%22%231fcbe2%22%2C%22size%22%3A144%2C%22font%22%3A%7B%22family%22%3A%22sans-serif%22%2C%22effect%22%3A%22bold%22%7D%7D%2C%22subtitle%22%3A%7B%22text%22%3A%22Make%20your%20ipcamera%20smart%22%2C%22color%22%3A%22%23c3e2df%22%2C%22size%22%3A58%2C%22offset%22%3A0%2C%22font%22%3A%7B%22family%22%3A%22sans-serif%22%2C%22effect%22%3A%22normal%22%7D%7D%2C%22image%22%3A%7B%22position%22%3A%220.5%22%2C%22positionX%22%3A%220.5%22%2C%22scale%22%3A%220.75%22%2C%22file%22%3A%7B%7D%7D%2C%22size%22%3A%22feature-graphic%22%7D