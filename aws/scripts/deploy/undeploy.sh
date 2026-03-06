#!/bin/bash
if netstat -an | grep 9090 | grep LISTEN
then
systemctl stop schemaService &
sleep 5
fi


if [ ! -d "/etc/systemd/systemd/schemaService.service" ]
then
  rm -f /etc/systemd/systemd/sschemaService.service
fi

systemctl daemon-reload

if [ ! -d "/opt/dst/schema-services/schemservice/schema-service-1.0.jar" ]
then
  rm -f /opt/dst/schema-services/schemservice/schema-service-1.0.jar
fi