#!/bin/sh

echo "clearing old files"
rm ../../src/main/java-gen/org/openmuc/jdlms/internal/asn1/cosem/*

echo "generating files"
../axdr-compiler/run-scripts/axdr-compiler.sh -o "../../src/main/java-gen/org/openmuc/jdlms/internal/asn1/cosem/" -p "org.openmuc.jdlms.internal.asn1.cosem" cosem.asn
