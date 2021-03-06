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
/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrSequenceOf;

public class Set_Request_With_List implements AxdrType {

    public static class SubSeqOf_attribute_descriptor_list
            extends AxdrSequenceOf<Cosem_Attribute_Descriptor_With_Selection> {

        @Override
        protected Cosem_Attribute_Descriptor_With_Selection createListElement() {
            return new Cosem_Attribute_Descriptor_With_Selection();
        }

        protected SubSeqOf_attribute_descriptor_list(int length) {
            super(length);
        }

        public SubSeqOf_attribute_descriptor_list() {
        } // Call empty base constructor

    }

    public static class SubSeqOf_value_list extends AxdrSequenceOf<Data> {

        @Override
        protected Data createListElement() {
            return new Data();
        }

        protected SubSeqOf_value_list(int length) {
            super(length);
        }

        public SubSeqOf_value_list() {
        } // Call empty base constructor

    }

    public byte[] code = null;
    public Invoke_Id_And_Priority invoke_id_and_priority = null;

    public SubSeqOf_attribute_descriptor_list attribute_descriptor_list = null;

    public SubSeqOf_value_list value_list = null;

    public Set_Request_With_List() {
    }

    public Set_Request_With_List(byte[] code) {
        this.code = code;
    }

    public Set_Request_With_List(Invoke_Id_And_Priority invoke_id_and_priority,
            SubSeqOf_attribute_descriptor_list attribute_descriptor_list, SubSeqOf_value_list value_list) {
        this.invoke_id_and_priority = invoke_id_and_priority;
        this.attribute_descriptor_list = attribute_descriptor_list;
        this.value_list = value_list;
    }

    @Override
    public int encode(BerByteArrayOutputStream axdrOStream) throws IOException {

        int codeLength;

        if (code != null) {
            codeLength = code.length;
            for (int i = code.length - 1; i >= 0; i--) {
                axdrOStream.write(code[i]);
            }
        }
        else {
            codeLength = 0;
            codeLength += value_list.encode(axdrOStream);

            codeLength += attribute_descriptor_list.encode(axdrOStream);

            codeLength += invoke_id_and_priority.encode(axdrOStream);

        }

        return codeLength;

    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;

        invoke_id_and_priority = new Invoke_Id_And_Priority();
        codeLength += invoke_id_and_priority.decode(iStream);

        attribute_descriptor_list = new SubSeqOf_attribute_descriptor_list();
        codeLength += attribute_descriptor_list.decode(iStream);

        value_list = new SubSeqOf_value_list();
        codeLength += value_list.decode(iStream);

        return codeLength;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        BerByteArrayOutputStream axdrOStream = new BerByteArrayOutputStream(encodingSizeGuess);
        encode(axdrOStream);
        code = axdrOStream.getArray();
    }

    @Override
    public String toString() {
        return "sequence: {" + "invoke_id_and_priority: " + invoke_id_and_priority + ", attribute_descriptor_list: "
                + attribute_descriptor_list + ", value_list: " + value_list + "}";
    }

}
