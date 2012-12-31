android-ski-app
===============
### Overview ###

This is an Android application designed for use while skiing.
It includes the following primary features:
 * Music Control via Magnetic Sensing
 * Periodic Time Announcements via TTS (Text to Speech)

### Music Control via Magnetic Sensing ###

This is the app's primary feature. When magnetic control is active,
the phone will sense the ambient magnetic field using the phone's magnetometer.
When the magnetic field reaches a certain threshold value, the sensor is considered "tripped".

Tripping the magnetic sensor for less than a certain amount of time will pause / play toggle the
current song (playing in the background Android music player).

Tripping the sensor for longer than a certain amount of time will result in the song being skipped.

This functionality allows one to easily control music while skiing with
nothing more than a phone placed in a chest pocket and a small fridge magnet in one's glove.
Simply bringing the glove within several inches of the phone will trip the magnetic sensor.
I have found that pausing or changing songs is easy to do, regardless of weather conditions or skiing speed.
Moreover, and most importantly, this method of control is effectively free
for anyone who happens to have a magnet sitting around. No expensive wireless glove controls are required.

### Time Announcements ###

The app also has an optional time announcement feature.

While enabled, the phone will periodically read out the current time via text to speech.
This is useful on the slopes, as it can be difficult to access a watch.

### Issue: Screen must stay on ###

Due to a combination of Android firmware bugs and HTC hardware shortcomings, the phone is unable to read
values from the Magnetometer while the phone's screen is off. As a result, the application will
forcibly keep the screen on when the magnetic sensing feature is enabled.

I recommend that after setting it up, you turn off the phone so that the phone remains locked
(it will power back on at the lockscreen) to prevent any accidental input.

By reducing the phone's backlight to a minimum, I have found that my phone's battery is more
than capable of running this app for a full day of skiing. You mileage may vary.

Additionally, it may not be necessary to keep the screen on with certain Android phone models.
You can try reducing the wakelock to a partial wakelock and seeing whether or not the app is
still functional.

See this link for more information about this bug: http://nosemaj.org/android-persistent-sensors

### Future Improvements ###

Most of the settings are hardcoded. This includes
 - Threshold value for the magnetometer
 - Timing for skipping a song
 - Interval of time announcements

In the future, I may choose to make these settings configurable.

### Using this app ###

I highly recommend setting up an android project and modifying this code for your own use.

You might also consider trying using the apk file provided in this repository at /SkiApp.apk.
This APK file is only built for Android 2.3.3 (gingerbread) so I cannot gaurentee usability on
any other Android software versions, but you're welcome to try.

Additionally, the lack of configurable settings mentioned earlier may require you to program in
new values.


