#!/bin/bash
echo 'In deploy'

chmod +x /tmp/schema-services/*.sh
chmod +x /tmp/schema-services/*.service

# Set and export an environment variables
export JDBC_URL="jdbc:oracle:thin:@localhost:1521/AWD1"
export USERNAME="SCHMDB"
export PASSWORD="password"

if [ ! -d "/opt/dst/schema-services" ]
then
  mkdir -p /opt/dst/schema-services
fi
echo 'copying schema services'
cp /tmp/schema-services/**schemaservice**.zip /opt/dst/schema-services/
JARFILENAME=$(ls -1 /opt/dst/schema-services/**schemaservice**.zip | xargs -n 1 basename)
echo $JARFILENAME
unzip -o /opt/dst/schema-services/$JARFILENAME -d /opt/dst/schema-services
rm -rf /opt/dst/schema-services/$JARFILENAME
# create ux builder service
echo 'creating service'
cp /opt/dst/schema-services/schemaservice/schemaService.service /etc/systemd/system/schemaService.service
chmod -R 777 /opt/dst/schema-services/schemaservice

echo 'enable service'

systemctl enable schemaService.service
echo 'start service'
systemctl start schemaService

sleep 15s


