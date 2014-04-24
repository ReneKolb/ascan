gradle clean
gradle build
gradle compileDebugJava
gradle installDebug && adb shell 'am start -n com.hbm.scan/.Scan'
adb logcat "Scan:V *:S"
adb logcat "System.out:V *:S"

