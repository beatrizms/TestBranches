@echo off
setlocal enableextensions
set OUG_ENV=%~dp0

echo UG Install path is : %OUG_HOME%
echo UG Environment path is : %OUG_ENV%
cd %OUG_ENV% 

"%OUG_HOME%\wrapper.exe"  -r "%OUG_ENV%/wrapper.conf"
"%OUG_HOME%\wrapper.exe"  -it "%OUG_ENV%/wrapper.conf"

pause


