@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-19
set MAVEN_HOME=C:\Program Files\NetBeans-19\netbeans\java\maven
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

cd C:\Users\Administrator\Desktop\AVATA\avatarOpenLo

:: Chạy ứng dụng Java mà không build lại
mvn exec:java -Dexec.mainClass="avatar.server.Avatar"

pause
