# Exam Browser by zvlfahmi.

![nlogo](https://raw.githubusercontent.com/zvlfahmi/exambrowser/refs/heads/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp)

I made this "myself" because i was frustrated with my school's CBT app.

Shout out to IT Club's Programming Division (zvlfahmi and [slvr12](https://github.com/slvr12))

This app is the ~continuation~ rework of [MAN IT Club's CBT App](https://github.com/itclubmanmet/exambrowser-man), but i also made that app :P (because i'm a IT Club member)

## Compability 
Android 8 "Oreo" (API 26) to Android 15 "Vanilla Ice Cream" (API 35). <br>
To put simply, any phone released after 2018 should be able to run the app.

## "Feature"
as of right now, this app doesn't have that much feature but here's a list of them:

- "Kiosk" Mode (Pin) (added in v0.1b)
- Sound when exiting (added in v0.1b)
- No Bluetooth (added in v0.1b)
- Disable opening the app in Windowed mode (added in v0.1b)
- Blank screenshot and screenrecord (added in v0.2b)
- LOCKDOWN TIMER WHEN EXITING APP (added in v0.2b)
- Blacklisted Apps (added in v0.3b)
- Automatically update password and blacklist database from internet (added in v0.3b)

## Permissions used
- Camera
- Notification
- Usage Access (Required)
- (Only on Android 14 and above) Restricted Settings (Required)
##  Known issues
1. On Funtouch 15 (Vivo), the sound when the app exits doesn't play at all. But the other safety measures still work.
2. On MIUI 14 (Xiaomi), when you close the app by using gestures (Back + Menu button) it made the phone unresponsive unless you lock the screen with the power button

## Experimental features plans
I have a ton of new ideas to stop cheating in CBT Apps, but it's still in experimental stage.<br>
I always advocate the importance of privacy in my school, but this app may goes against what i stand for
- Live camera access
- (Requires the aforementioned feature) Utilizing Mediapipe and OpenCV to monitor eye movements**
- Remove the app dependencies for WebView and instead is a standalone app*

(*) Instead of making the app just displaying existing website using WebView, the app instead IS the way to do the exam by pulling question and answer to local and the app displays them.
(**) Kinda impossible, as OpenCV and Mediapipe requires too much resource. But it's doable!
