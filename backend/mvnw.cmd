<# : batch portion
@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set WRAPPER_DIR=%~dp0
powershell -NoProfile -ExecutionPolicy Bypass -Command "& { $propFile = Join-Path '%WRAPPER_DIR%' '.mvn\wrapper\maven-wrapper.properties'; $distUrl = (Get-Content $propFile | Where-Object { $_ -match '^distributionUrl=' }) -replace '^distributionUrl=', '' -replace '\\:', ':'; $zipName = Split-Path $distUrl -Leaf; $dirName = $zipName -replace '-bin\.zip$', ''; $baseDir = Join-Path $env:USERPROFILE '.m2\wrapper\dists\apache-maven'; $targetDir = Join-Path $baseDir $dirName; $mvnCmd = Join-Path $targetDir 'bin\mvn.cmd'; if (-not (Test-Path $mvnCmd)) { New-Item -ItemType Directory -Force -Path $baseDir | Out-Null; $zipPath = Join-Path $baseDir $zipName; Write-Host 'Downloading Apache Maven 3.9.9...'; Invoke-WebRequest -Uri $distUrl -OutFile $zipPath; Expand-Archive -Path $zipPath -DestinationPath $baseDir -Force; Remove-Item $zipPath -Force; } & $mvnCmd %* ; exit $LASTEXITCODE }"
exit /b %ERRORLEVEL%

