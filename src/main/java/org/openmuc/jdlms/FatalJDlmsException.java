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
 * This Exception denotes, that the a fatal exception has occurred. Connection has been closed.
 */
public class FatalJDlmsException extends JDlmsException {

    public FatalJDlmsException(ExceptionId exceptionId, Fault assumedFault, String message) {
        super(exceptionId, assumedFault, message);
    }

    public FatalJDlmsException(ExceptionId exceptionId, Fault assumedFault, String message, Throwable cause) {
        super(exceptionId, assumedFault, message, cause);
    }
}
