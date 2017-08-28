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
package org.openmuc.jdlms.interfaceclass.attribute;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmuc.jdlms.interfaceclass.InterfaceClass;
import org.openmuc.jdlms.interfaceclass.attribute.AttributeDirectory.AttributeNotFoundException;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;

@RunWith(JUnitParamsRunner.class)
public class AttributeDirectoryTest {

    @Test
    public void testDirectory1() throws Exception {
        InterfaceClass interfaceClass = InterfaceClass.interfaceClassFor(7);
        AttributeClass attributeClass = AttributeDirectory.attributeClassFor(interfaceClass, 2);

        assertEquals(attributeClass.interfaceClass(), InterfaceClass.PROFILE_GENERIC);
        assertEquals(attributeClass.attributeName(), ProfileGenericAttribute.BUFFER.name());
    }

    @Test(expected = AttributeNotFoundException.class)
    @Parameters({ "200, 2", "15, 99" })
    @TestCaseName("Test illegal combination of classId = {0}, attributeId = {2} and version = {1}")
    public void testDirectory2(int classId, int attributeId) throws Exception {
        InterfaceClass interfaceClass = InterfaceClass.interfaceClassFor(classId);
        AttributeDirectory.attributeClassFor(interfaceClass, attributeId);
    }
}
