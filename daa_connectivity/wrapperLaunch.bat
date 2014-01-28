@echo off
set CLIENT_SOLUTION=%cd%
set IB_HOME=C:\Program Files\PowerCurve\Connectivity\Connectivity v1.2SP3

echo Connectivity Install path is : %IB_HOME%
echo Connectivity Environment path is : %CLIENT_SOLUTION%
cd %CLIENT_SOLUTION%

"%OUG_HOME%\wrapper.exe" "%CLIENT_SOLUTION%/wrapper.conf"

pause


