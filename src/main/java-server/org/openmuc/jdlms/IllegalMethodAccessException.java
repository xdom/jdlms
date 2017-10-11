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

/**
 * This exception signals that a COSEM method has been accessed in a wrong way.
 * 
 * @see CosemMethod
 */
public class IllegalMethodAccessException extends DlmsAccessException {

    private final MethodResultCode methodResultCode;

    /**
     * Construct a new Exception.
     * 
     * @param methodResultCode
     *            the reason of the exception.
     */
    public IllegalMethodAccessException(MethodResultCode methodResultCode) {
        this.methodResultCode = methodResultCode;
    }

    /**
     * Get the access result code of the illegal method access.
     * 
     * @return the access result code.
     */
    public MethodResultCode getMethodResultCode() {
        return methodResultCode;
    }

}
