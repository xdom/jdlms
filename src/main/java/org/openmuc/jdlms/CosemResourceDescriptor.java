/**
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
 */
package org.openmuc.jdlms;

import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;

/**
 * The COSEM resource descriptor. The base class for an attribute address or a method address.
 */
public abstract class CosemResourceDescriptor {

    private final int classId;
    private final ObisCode instanceId;
    private final int id;

    CosemResourceDescriptor(int classId, ObisCode instanceId, int id) {
        this.classId = classId;
        this.instanceId = instanceId;
        this.id = id;
    }

    /**
     * Get the class ID.
     * 
     * @return the int class ID.
     */
    public int getClassId() {
        return classId;
    }

    /**
     * The ID/index of the resource. Method or attribute index.
     * 
     * @return the index.
     */
    public int getId() {
        return id;
    }

    /**
     * Get the instance ID of the interface class.
     * 
     * @return the instance ID.
     */
    public ObisCode getInstanceId() {
        return instanceId;
    }

    abstract AxdrType toDescriptor();
}
