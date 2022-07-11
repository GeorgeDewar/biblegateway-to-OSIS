@echo off

IF [%1]==[] goto usage

rem Make the directory structure
mkdir zip\%1\modules\texts\ztext\%1
mkdir zip\%1\mods.d

copy %1.conf zip\%1\mods.d\%1.conf
copy %1\*.* zip\%1\modules\texts\ztext\%1\
powershell Compress-Archive -Force zip/ESV2/* ESV2.zip

IF "%2"=="-S" adb shell am start -S -n net.bible.android.activity/.StartupActivity

goto end

:usage
echo push: Copy the translation's data files to the phone
echo
echo Usage: push ^<translation^>
echo

:end
