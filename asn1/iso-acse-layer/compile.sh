#!/bin/bash

rm ../../src/main/java-gen/org/openmuc/jdlms/internal/asn1/iso/acse/* 

jasn1-compiler -o "../../src/main/java-gen/" -p "org.openmuc.jdlms.internal.asn1.iso" -f iso-acse-layer.asn
