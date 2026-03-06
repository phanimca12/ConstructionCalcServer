Findstr -m /S /C:"^/schema-services/(.*)$" "C:\Program Files\IBM\HTTPServer\conf\httpd.conf" > SchemaService.txt
set /p FINDOUTPUT= < SchemaService.txt
IF "%FINDOUTPUT%"=="" (
    net stop IBMHTTPServerV9.0
    echo. >> "C:\Program Files\IBM\HTTPServer\conf\httpd.conf"
    echo ProxyPassMatch "^/schema-services/(.*)$"  "http://localhost:8082/$1" >> "C:\Program Files\IBM\HTTPServer\conf\httpd.conf"
    echo ProxyPassReverse "^/schema-services/(.*)$"  "http://localhost:8082/$1" >> "C:\Program Files\IBM\HTTPServer\conf\httpd.conf"
    net start IBMHTTPServerV9.0
)
del UXBuilder.txt

powershell -command "Expand-Archive -Force C:\tmp\schema-services\schemaservice-windows-**.zip C:\tmp\schema-services"
SET ES_HOME=C:\opt\dst\schema-services
SET TMPES_HOME=C:\tmp\schema-services\schemaservice

IF NOT EXIST "%ES_HOME%" MKDIR "%ES_HOME%"
IF NOT EXIST "%ES_HOME%\bin" MKDIR "%ES_HOME%\bin"

copy "%TMPES_HOME%\schema-service-**.jar" "%ES_HOME%"
copy "%TMPES_HOME%\schemaService.exe" "%ES_HOME%"\bin
copy "%TMPES_HOME%\application.yml" "%ES_HOME%"\bin
copy "%TMPES_HOME%\schemaService.xml" "%ES_HOME%"\bin

cd "%ES_HOME%"\bin

IF EXIST schemaService.exe (
  schemaService.exe stop
  schemaService.exe install
  schemaService.exe start
)