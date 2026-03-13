@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE__=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0'; $env:__MVNW_SCRIPT_DIR__=$scriptDir; $env:__MVNW_CMD__=''; PowerShell -NoProfile -ExecutionPolicy Bypass -File \"%~dp0.mvn\wrapper\MavenWrapperDownloader.ps1\" -URL \"https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar\" -Out \".mvn\\wrapper\\maven-wrapper.jar\" 2>&1; exit $LASTEXITCODE}"`) DO @(
    IF "%%A"=="__MVNW_CMD__" (SET __MVNW_CMD__=%%B) ELSE IF "%%A"=="__MVNW_ERROR__" (SET __MVNW_ERROR__=%%B)
)
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE__%

@IF NOT "%__MVNW_CMD__%"=="" (%__MVNW_CMD__% %*)
@IF "%__MVNW_ERROR__%"=="0" @GOTO :mvnw_done

@setlocal
@set WRAPPER_JAR="%~dp0.mvn\wrapper\maven-wrapper.jar"
@set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@if exist %WRAPPER_JAR% @goto :execute

@echo Couldn't find %WRAPPER_JAR%, downloading it ...
@echo.

@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%~dp0.mvn\wrapper\maven-wrapper.properties") DO (
    IF "%%A"=="wrapperUrl" SET wrapperUrl=%%B
)

@IF "%wrapperUrl%"=="" (
    @echo Cannot find wrapperUrl property in '%~dp0.mvn\wrapper\maven-wrapper.properties'
    @goto :error
)

@powershell -Command "&{"^
        "$webclient = new-object System.Net.WebClient;"^
        "if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
        "$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
        "}"^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%wrapperUrl%', '%WRAPPER_JAR%')"^
        "}"
@if %ERRORLEVEL% NEQ 0 @goto :error

:execute
@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%~dp0.mvn\wrapper\maven-wrapper.properties") DO (
    IF "%%A"=="distributionUrl" SET distributionUrl=%%B
)
@IF "%distributionUrl%"=="" (
    @echo Cannot find distributionUrl property in '%~dp0.mvn\wrapper\maven-wrapper.properties'
    @goto :error
)

%JAVA_HOME%\bin\java.exe ^
  %MAVEN_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%~dp0" ^
  %WRAPPER_LAUNCHER% %*
@if ERRORLEVEL 1 @goto :error
@goto :mvnw_done

:error
@set ERROR_CODE=1

:mvnw_done
@endlocal & @set ERROR_CODE=%ERROR_CODE%

@IF NOT "%__MVNW_CMD__%"=="" @SET __MVNW_CMD__=
@IF NOT "%__MVNW_ERROR__%"=="" @SET __MVNW_ERROR__=
@IF NOT "%__MVNW_PSMODULEP_SAVE__%"=="" @SET __MVNW_PSMODULEP_SAVE__=
@IF NOT "%__MVNW_ARG0_NAME__%"=="" @SET __MVNW_ARG0_NAME__=

@exit /B %ERROR_CODE%
