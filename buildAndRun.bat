@echo off
call mvn clean package
call docker build -t com.mycompany/HolaServlet .
call docker rm -f HolaServlet
call docker run -d -p 9080:9080 -p 9443:9443 --name HolaServlet com.mycompany/HolaServlet