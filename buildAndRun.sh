#!/bin/sh
mvn clean package && docker build -t com.mycompany/HolaServlet .
docker rm -f HolaServlet || true && docker run -d -p 9080:9080 -p 9443:9443 --name HolaServlet com.mycompany/HolaServlet