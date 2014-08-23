# home-recorder-client

Android client for [home-recorder](https://github.com/nknytk/home-recorder)

## Feature

This app supply your home-recorder with automatic event check enable/disable switch.
* When you go out of home with registered Android terminal, event check of home-recorder is automatically enabled.
* When you come back home with registered Android terminal, event check of home-recorder is automatically disabled.

## Requirements

* home-recorder in your home
* 4.0 or newer version of Android with wifi lock support  
(Unfortunatelly, some android terminals do not support WifiManager.WifiLock.aquire() spoiling this app.)
* Wifi

# Usage

## Download apk from [here](https://github.com/nknytk/home-recorder-client/blob/master/dist/home-recorder-client.apk)
## Install downloaded apk
## Put following values in to be same as home-recorder's
* server side token
* client side token
* secure hash digest repetition

# ToDo

* watch camera from this app
* stop background service when you disable automatic switch
* all home-recorder config from app (long long way...)

## Comment

You need working [home-recorder](https://github.com/nknytk/home-recorder) first.

## License

Copyright (c) [2014] [Yutaka Nakano]

This software is released under the Apache License v2.
