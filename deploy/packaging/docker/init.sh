#-------------------------------------------------------------------------------
# Copyright (c) 2013-2020 Contributors to the Eclipse Foundation
# 
# See the NOTICE file distributed with this work for additional
# information regarding copyright ownership.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Apache License,
# Version 2.0 which accompanies this distribution and is available at
# http://www.apache.org/licenses/LICENSE-2.0.txt
#-------------------------------------------------------------------------------
#!/bin/bash
#
# GeoWave Initialization Script
#

# Clean any classes generated by a previous vendor build to avoid binary incompatibilities    
mvn clean
mkdir -p $WORKSPACE/deploy/target
export GEOWAVE_VERSION_STR="$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive -f $WORKSPACE/pom.xml exec:exec)"
export GEOWAVE_VERSION="$(echo ${GEOWAVE_VERSION_STR} | sed -e 's/"//g' -e 's/-SNAPSHOT//g')"
export GEOWAVE_RPM_VERSION="$(echo ${GEOWAVE_VERSION} | sed -e 's/"//g' -e 's/-/~/g')"
echo $GEOWAVE_VERSION > $WORKSPACE/deploy/target/version.txt
echo $GEOWAVE_RPM_VERSION > $WORKSPACE/deploy/target/rpm_version.txt
if [[ "$GEOWAVE_VERSION_STR" =~ "-SNAPSHOT" ]]
then
	#its a dev/latest build
	echo "dev" > $WORKSPACE/deploy/target/build-type.txt
	echo "latest" > $WORKSPACE/deploy/target/version-url.txt
else
	#its a release
	echo "release" > $WORKSPACE/deploy/target/build-type.txt
	echo $GEOWAVE_VERSION_STR > $WORKSPACE/deploy/target/version-url.txt
fi