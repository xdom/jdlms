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

import java.text.MessageFormat;

import org.openmuc.jdlms.datatypes.DataObject;

/**
 * Container class holding the results of a remote method invocation via action operation
 */
public final class MethodResult {

    private final DataObject resultData;
    private final MethodResultCode resultCode;

    MethodResult(MethodResultCode resultCode, DataObject resultData) {
        this.resultData = resultData;
        this.resultCode = resultCode;
    }

    MethodResult(MethodResultCode resultCode) {
        this.resultData = null;
        this.resultCode = resultCode;
    }

    /**
     * Returns the data of return data of this method call. Note that this value is null if isSuccess() is false.
     * 
     * @return Returns the data of return data
     */
    public DataObject getResultData() {
        return resultData;
    }

    /**
     * @return The result code of the method call
     */
    public MethodResultCode getResultCode() {
        return resultCode;
    }

    @Override
    public String toString() {
        return MessageFormat.format("'{'code: {0}({1}), data: '{'{2}'}''}'", this.resultCode, this.resultCode.getCode(),
                this.resultData);
    }
}
