gradle clean
gradle build
gradle compileDebugJava
gradle installDebug && adb shell 'am start -n com.hbm.devices.scan.ui.android/.ScanActivity'
adb logcat "Scan:V *:S"
adb logcat "System.out:V *:S"

