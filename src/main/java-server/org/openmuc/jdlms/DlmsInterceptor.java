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

import org.openmuc.jdlms.datatypes.DataObject;

/**
 * A interceptor interface used to intercept xDLMS services of a COSEM object.
 * 
 * @see CosemInterfaceObject#CosemInterfaceObject(ObisCode, DlmsInterceptor)
 * @see CosemInterfaceObject#CosemInterfaceObject(String, DlmsInterceptor)
 */
public interface DlmsInterceptor {
    /**
     * Intercept all xDLMS GET, SET and ACTION services, except the get for attribute ID 1 (logical name/ instance ID).
     * 
     * @param ctx
     *            the invocation context.
     * @return the result of the invocation. Return the result of {@linkplain DlmsInvocationContext#proceed()}
     * @throws DlmsAccessException
     *             if {@linkplain DlmsInvocationContext#proceed()} throws an exception (meaning the actual method), this
     *             can be forwarded.
     */
    DataObject intercept(DlmsInvocationContext ctx) throws DlmsAccessException;
}
