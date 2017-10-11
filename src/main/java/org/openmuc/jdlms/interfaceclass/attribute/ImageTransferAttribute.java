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
package org.openmuc.jdlms.interfaceclass.attribute;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

public enum ImageTransferAttribute implements AttributeClass {
    LOGICAL_NAME(1),
    IMAGE_BLOCK_SIZE(2),
    IMAGE_TRANSFERRED_BLOCKS_STATUS(3),
    IMAGE_FIRST_NOT_TRANSFERRED_BLOCK_NUMBER(4),
    IMAGE_TRANSFER_ENABLED(5),
    IMAGE_TRANSFER_STATUS(6),
    IMAGE_TO_ACTIVATE_INFO(7);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.IMAGE_TRANSFER;

    private final int id;

    private ImageTransferAttribute(int id) {
        this.id = id;
    }

    @Override
    public InterfaceClass interfaceClass() {
        return INTERFACE_CLASS;
    }

    @Override
    public int attributeId() {
        return id;
    }

    @Override
    public String attributeName() {
        return name();
    }

}
