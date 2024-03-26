adb shell pm uninstall com.termux.api
adb install app/build/outputs/apk/debug/termux-api_debug.apk
adb shell settings put secure sms_default_application com.termux.api # maybe?
