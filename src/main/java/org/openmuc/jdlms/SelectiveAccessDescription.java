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

import org.openmuc.jdlms.datatypes.DataObject;

/**
 * Additional parameter to access attributes to narrow the results of a get operation on the smart meter.
 * 
 * Please refer IEC 62056-6-2 (formally IEC 62056-62) to see what specific combination of selector and parameter are
 * allowed for each object.
 */
public class SelectiveAccessDescription {
    private final int accessSelector;
    private final DataObject accessParameter;

    /**
     * Constructs a new SelectiveAccessDescription.
     * 
     * @param accessSelector
     *            The selector index, specifying what shall be filtered from the response
     * @param accessParameter
     *            The actual filter of the selection.
     * @throws IllegalArgumentException
     *             if the accessSelector is out of range. [0, 255]
     */
    public SelectiveAccessDescription(int accessSelector, DataObject accessParameter) throws IllegalArgumentException {
        if (accessSelector < 0 || accessSelector > 0xFF) {
            throw new IllegalArgumentException("Access selector must be in range [0, 255]");
        }

        this.accessSelector = accessSelector;
        this.accessParameter = accessParameter;
    }

    /**
     * Get the access selector.
     * <p>
     * The selector index, specifying what shall be filtered from the response.
     * </p>
     * 
     * @return the access selector.
     */
    public int getAccessSelector() {
        return accessSelector;
    }

    /**
     * Returns the access parameter.
     * 
     * @return the access parameter
     */
    public DataObject getAccessParameter() {
        return accessParameter;
    }

}
