@echo off

IF [%1]==[] goto usage
IF [%2]==[] goto usage

IF NOT EXIST %1 mkdir %1

sword-utilities-1.6.2\osis2mod %1 %1.xml -z -v %2
goto end

:usage
echo -------------------------------------------------------------------------------
echo build: Build a bible translation
echo -------------------------------------------------------------------------------
echo.
echo Usage: build ^<translation^> ^<versification^>
echo.
echo Versification can be KJV or NRSV
echo.

:end
