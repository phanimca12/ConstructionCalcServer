echo "TargetOS='$AWD_TARGET'"

cp ./aws/linux_appspec.yml appspec.yml

if [ "$AWD_TARGET" = "LinJboss" ] || [ "$AWD_TARGET" = "LinuxLiberty" ]
then
  cp ./aws/linux_appspec.yml appspec.yml
elif [ "$AWD_TARGET" = "WindowsLiberty" ] || [ "$AWD_TARGET" = "WindowsJboss" ]
then
   cp ./aws/windows_appspec.yml appspec.yml
else
   echo "Invalid TargetOS"
   exit 1
fi