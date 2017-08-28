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
 * Collection of data needed for a single remote SET call
 */
public class SetParameter {
    private final DataObject data;
    private final AttributeAddress attributeAddress;

    /**
     * Creates a set parameter for that particular attribute with a copy of the given data container
     * 
     * @param attributeAddress
     *            the address attribute
     * @param data
     *            Container of this parameter
     */
    public SetParameter(AttributeAddress attributeAddress, DataObject data) {
        this.attributeAddress = attributeAddress;
        this.data = data;
    }

    /**
     * Get the address of the attribute.
     * 
     * @return the address.
     */
    public AttributeAddress getAttributeAddress() {
        return attributeAddress;
    }

    /**
     * The new data to set.
     * 
     * @return the data.
     */
    public DataObject getData() {
        return this.data;
    }
}
