@echo off
TITLE=Connectivity
set "IB_HOME=C:\Program Files\PowerCurve\Connectivity\Connectivity v1.2SP3"

:okHome

set CLIENT_SOLUTION=%cd%

:okCSHome

set "DERBY_LOG=%CLIENT_SOLUTION%\logs\derby.log"
call "%IB_HOME%\bin\setOpts.bat"

java %BOOTSTRAP_DEBUG_OPT% %SECURITY_POLICY_OPT% -classpath .;"%IB_HOME%"\lib\core\enterprise-bootstrap.jar;"%IB_HOME%"\bin;"%CLIENT_SOLUTION%"\conf\system -Dclient.solution.home="%CLIENT_SOLUTION%" -Dib.home="%IB_HOME%" -Dfile.encoding=UTF-8 -Dlogback.configurationFile="%CLIENT_SOLUTION%"/conf/system/logback.xml -Dgroovy.source.encoding=UTF-8 -Dderby.stream.error.file="%DERBY_LOG%" com.experian.eda.enterprise.startup.Bootstrap -fa "%CLIENT_SOLUTION%"/conf/system/camel-context.xml

pause
