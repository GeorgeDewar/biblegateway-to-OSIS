@echo off

IF [%1]==[] goto usage

adb push %1.conf /mnt/sdcard/Android/data/net.bible.android.activity/files/mods.d/%1.conf
adb push %1 /mnt/sdcard/Android/data/net.bible.android.activity/files/modules/texts/ztext/%1/

IF "%2"=="-S" adb shell am start -S -n net.bible.android.activity/.StartupActivity

goto end

:usage
echo push: Copy the translation's data files to the phone
echo
echo Usage: push ^<translation^>
echo

:end

