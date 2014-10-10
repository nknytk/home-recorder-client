# home-recorder-client

Android client for [home-recorder](https://github.com/nknytk/home-recorder)

## Feature

This app supply your home-recorder with automatic event check enable/disable switch.
* When you go out of home with registered Android terminal, event check of home-recorder is automatically enabled.
* When you come back home with registered Android terminal, event check of home-recorder is automatically disabled.

You can watch real time status of home-recorder (only when you are at home).
* Images from cameras
* Sounds from mikes
* GPIO pin status

## Requirements

* home-recorder in your home
* 4.0 or newer version of Android with wifi lock support  
(Unfortunatelly, some android terminals do not support WifiManager.WifiLock.aquire() spoiling this app.)
* Wifi

# Usage

1. Setup [home-recorder](https://github.com/nknytk/home-recorder) server.
2. Download apk from [here](https://github.com/nknytk/home-recorder-client/blob/master/dist/home-recorder-client.apk)
3. Install downloaded apk
4. Fill in following three fields so that the values are the same as server's: Server token, Client token, Secure-hash repetition

# ToDo

* Register and receive GCM notification
* Notify when server is down.
* stop background service when you disable automatic switch
* all home-recorder config from app (long long way...)

## Comment

You need working [home-recorder](https://github.com/nknytk/home-recorder) first.

## License

Copyright 2014 Yutaka Nakano

This software is released under the Apache License v2.
