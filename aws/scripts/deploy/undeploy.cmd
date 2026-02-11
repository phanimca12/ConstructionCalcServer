cd "C:\opt\dst\schema-services\bin"


IF EXIST schemaServices.exe (
  schemaServices.exe stop
  schemaServices.exe uninstall
)