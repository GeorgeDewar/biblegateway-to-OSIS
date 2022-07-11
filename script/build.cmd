@echo off

IF [%1]==[] goto usage
IF [%2]==[] goto usage

IF NOT EXIST sword_output\%1 mkdir sword_output\%1

%HOME%\code\sword-utilities-1.6.2\osis2mod sword_output/%1 ../scraper/osis_output/%1.xml -z -v %2
goto end

:usage
echo -------------------------------------------------------------------------------
echo build: Build a bible translation
echo -------------------------------------------------------------------------------
echo.
echo Run this from the directory it is in. Ensure that sword-utilities-1.6.2 is installed
echo and on your path.
echo.
echo Usage: build ^<translation^> ^<versification^>
echo.
echo Versification can be KJV or NRSV
echo.

:end
