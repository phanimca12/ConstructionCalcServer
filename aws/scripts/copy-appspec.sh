echo "TargetOS='$TargetOS'"  

cp ./aws/linux_appspec.yml appspec.yml

if [ "$TargetOS" = "linux" ||  "$TargetOS" = "Linux"  ]
then
   cp ./aws/linux_appspec.yml appspec.yml
elif [ "$TargetOS" = "windows" ]
then
   cp ./aws/windows_appspec.yml appspec.yml
else 
   echo 'Invalid TargetOS' 
fi