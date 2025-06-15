REM Deskripsi: Script untuk meng-compile file Java dan menyalin folder assets ke direktori bin
@echo off

REM Meng-compile file Main.java dan output ke folder bin, menggunakan sourcepath src dan classpath ke library MySQL
echo Compiling project...
javac -d bin -sourcepath src -cp "lib/mysql-connector-j-9.3.0.jar" src/Main.java

REM Menyalin folder assets dari src ke bin agar game dapat mengakses file-file yang diperlukan
echo.
echo Copying assets...
xcopy src\assets bin\assets /E /I /Y

echo.
echo Build finished.