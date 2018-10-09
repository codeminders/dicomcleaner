#!/bin/sh

PIXELMEDDIR=.

java -Xmx2g -Xms512m -XX:-UseGCOverheadLimit -cp "${PIXELMEDDIR}/pixelmed.jar:${PIXELMEDDIR}/lib/additional/commons-compress-1.12.jar:${PIXELMEDDIR}/lib/additional/commons-codec-1.3.jar" com.pixelmed.validate.DicomSRValidator $*
