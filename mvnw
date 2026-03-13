#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.2.0
# ----------------------------------------------------------------------------

# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
    [ -h "$app_path" ]
do
    ls=$( ls -ld "$app_path" )
    link=${ls#*' -> '}
    case $link in             #(
      /*)   app_path=$link ;; #(
      *)    app_path=$APP_HOME$link ;;
    esac
done

# This is normally unused
# shellcheck disable=SC2034
APP_BASE_NAME=${0##*/}
APP_HOME=$( cd -P "${APP_HOME:-./}" > /dev/null && printf '%s\n' "$PWD" ) || exit

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-"$APP_HOME"}"

WRAPPER_JAR="$APP_HOME/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$APP_HOME/.mvn/wrapper/maven-wrapper.properties"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

if [ -r "$WRAPPER_PROPERTIES" ]; then
    distributionUrl=$(grep -E '^\s*distributionUrl\s*=' "$WRAPPER_PROPERTIES" | cut -d'=' -f2- | sed 's/^ *//' | tr -d '\r')
    wrapperUrl=$(grep -E '^\s*wrapperUrl\s*=' "$WRAPPER_PROPERTIES" | cut -d'=' -f2- | sed 's/^ *//' | tr -d '\r')
fi

if [ -z "$distributionUrl" ]; then
    echo "Error: Could not find the 'distributionUrl' property in $WRAPPER_PROPERTIES" >&2
    exit 1
fi

# Download maven-wrapper.jar if needed
if [ ! -r "$WRAPPER_JAR" ]; then
    if [ -z "$wrapperUrl" ]; then
        echo "Error: Could not find the 'wrapperUrl' property in $WRAPPER_PROPERTIES" >&2
        exit 1
    fi
    echo "Downloading from: $wrapperUrl"
    if command -v wget > /dev/null 2>&1; then
        if [ "$MVNW_VERBOSE" = true ]; then
            wget --user-agent='Apache-Maven/3.9' -O "$WRAPPER_JAR" "$wrapperUrl" -q
        else
            wget --user-agent='Apache-Maven/3.9' -O "$WRAPPER_JAR" "$wrapperUrl" -q
        fi
    elif command -v curl > /dev/null 2>&1; then
        curl -fsSL -A 'Apache-Maven/3.9' -o "$WRAPPER_JAR" "$wrapperUrl"
    else
        echo "Error: wget or curl not available for downloading maven-wrapper.jar" >&2
        exit 1
    fi
fi

# End of extension

# Use java command from JAVA_HOME if set, otherwise fall back to java in PATH
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

exec "$JAVACMD" \
    $MAVEN_OPTS \
    -classpath "$WRAPPER_JAR" \
    "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
    $WRAPPER_LAUNCHER "$@"
