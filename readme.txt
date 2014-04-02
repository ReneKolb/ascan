ant debug install && adb shell 'am start -n com.hbm.scan/.Scan'
adb logcat "Scan:V *:S"

