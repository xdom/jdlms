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
package org.openmuc.jdlms.settings.client;

/**
 * The referencing method which is used to address the COSEM objects or resources.
 * 
 * <p>
 * See <a href="https://www.openmuc.org/dlms-cosem/user-guide/#_addressing_attributes_and_methods">OpenMUC - DLMS-COSEM
 * user guide</a> for further information.
 * </p>
 */
public enum ReferencingMethod {
    /**
     * Use the logical name (<b>LN</b>) referencing method. 6 byte address.
     */
    LOGICAL,

    /**
     * Use the short name referencing (<b>SN</b>) method. With SN resources are addressed individually. 13 bits address,
     * first three bits are <u>not</u> used.
     */
    SHORT
}
