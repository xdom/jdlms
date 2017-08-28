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

import static org.openmuc.jdlms.AccessResultCode.SUCCESS;

import java.text.MessageFormat;

import org.openmuc.jdlms.datatypes.DataObject;

/**
 * Class representing the result of a get operation received from the server.
 */
public class GetResult {

    private final DataObject resultData;
    private final AccessResultCode resultCode;

    GetResult(DataObject resultData) {
        this(resultData, SUCCESS);
    }

    GetResult(AccessResultCode errorCode) {
        this(null, errorCode);
    }

    private GetResult(DataObject resultData, AccessResultCode resultCode) {
        this.resultData = resultData;
        this.resultCode = resultCode;
    }

    /**
     * Returns the data of return data of this get operation.
     * <p>
     * NOTE: if the value of {@link #getResultCode()} is not {@link AccessResultCode#SUCCESS}, the result data is
     * <code>null</code>.
     * </p>
     * 
     * @return returns the data of return data
     */
    public DataObject getResultData() {
        return this.resultData;
    }

    /**
     * The result code of the GET operation.
     * 
     * @return The result code of the get operation
     */
    public AccessResultCode getResultCode() {
        return this.resultCode;
    }

    public boolean requestSuccessful() {
        return getResultCode() == SUCCESS;
    }

    @Override
    public String toString() {
        return MessageFormat.format("'{'code: {0}({1}), data: '{'{2}'}''}'", this.resultCode, this.resultCode.getCode(),
                this.resultData);
    }
}
