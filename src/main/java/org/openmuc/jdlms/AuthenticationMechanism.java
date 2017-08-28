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
package org.openmuc.jdlms;

import java.util.HashMap;
import java.util.Map;

/**
 * Mechanisms to authenticate to the remote meter.
 */
public enum AuthenticationMechanism {

    /**
     * No authentication used.
     */
    NONE(0),

    /**
     * Authentication of the client by sending a shared password as secret
     */
    LOW(1),

    /**
     * Authentication of both client and smart meter using GMAC and a pre shared secret password
     */
    HLS5_GMAC(5);

    private final int id;

    private static final Map<Integer, AuthenticationMechanism> idMap = new HashMap<>();

    static {
        for (AuthenticationMechanism enumInstance : AuthenticationMechanism.values()) {
            if (idMap.put(enumInstance.getId(), enumInstance) != null) {
                throw new IllegalArgumentException("duplicate ID: " + enumInstance.getId());
            }
        }
    }

    private AuthenticationMechanism(int id) {
        this.id = id;
    }

    /**
     * Returns the ID of this AuthenticationMechanism.
     * 
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the AuthenticationMechanism that corresponds to the given ID. Throws an IllegalArgumentException if no
     * AuthenticationMechanism with the given ID exists.
     * 
     * @param id
     *            the ID
     * @return the AuthenticationMechanism that corresponds to the given ID
     */
    public static AuthenticationMechanism getInstance(int id) {
        AuthenticationMechanism enumInstance = idMap.get(id);
        if (enumInstance == null) {
            throw new IllegalArgumentException("invalid ID: " + id);
        }
        return enumInstance;
    }

    public boolean isHlsMechanism() {
        switch (this) {
        case HLS5_GMAC:
            return true;

        default:
        case LOW:
        case NONE:
            return false;
        }
    }

}
