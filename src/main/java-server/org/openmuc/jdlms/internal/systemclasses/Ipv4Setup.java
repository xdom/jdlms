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
package org.openmuc.jdlms.internal.systemclasses;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.datatypes.DataObject;

@CosemClass(id = 42, version = 0)
public class Ipv4Setup extends CosemInterfaceObject {

    @CosemAttribute(id = 2)
    private DataObject tcpUdpPort;

    @CosemAttribute(id = 3)
    private DataObject ipReference;

    @CosemAttribute(id = 4)
    private DataObject mss;

    @CosemAttribute(id = 5)
    private DataObject nbOfSimConn;

    @CosemAttribute(id = 6)
    private DataObject inactivityTimeOut;

    public Ipv4Setup(String instanceId) {
        super(instanceId);
    }

}
