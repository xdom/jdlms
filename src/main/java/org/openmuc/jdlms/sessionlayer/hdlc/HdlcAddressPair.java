/*
 * Copyright 2012-17 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.jdlms.sessionlayer.hdlc;

/**
 * Pair of client and server address that uniquely identifies an HDLC Client connection.
 */
public class HdlcAddressPair {
    private final HdlcAddress source;
    private final HdlcAddress destination;

    public HdlcAddressPair(HdlcAddress source, HdlcAddress destination) {
        this.source = source;
        this.destination = destination;
    }

    public HdlcAddress source() {
        return source;
    }

    public HdlcAddress destination() {
        return destination;
    }

    // used for the map, since since the incoming HDLC addess pair is switched.
    public HdlcAddressPair switchedPair() {
        return new HdlcAddressPair(destination, source);
    }

    @Override
    public int hashCode() {
        int hashSource = source != null ? source.getPhysicalId() + source.getLogicalId() : 0;
        int hashDestination = destination != null ? destination.getPhysicalId() + destination.getLogicalId() : 0;

        return (hashSource + hashDestination) * hashDestination + hashSource;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HdlcAddressPair)) {
            return false;
        }

        HdlcAddressPair other = (HdlcAddressPair) o;

        return source.equals(other.source) && destination.equals(other.destination)
                || (source.equals(other.source) && other.destination.isCalling());
    }

    @Override
    public String toString() {
        return source + ":" + destination;
    }
}
