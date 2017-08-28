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
package org.openmuc.jdlms.internal;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.EventNotification;
import org.openmuc.jdlms.IllegalPametrizationError;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.datatypes.BitString;
import org.openmuc.jdlms.datatypes.CompactArray;
import org.openmuc.jdlms.datatypes.CompactArray.DescriptionArray;
import org.openmuc.jdlms.datatypes.CompactArray.TypeDesc;
import org.openmuc.jdlms.datatypes.CosemDate;
import org.openmuc.jdlms.datatypes.CosemDateFormat;
import org.openmuc.jdlms.datatypes.CosemDateTime;
import org.openmuc.jdlms.datatypes.CosemTime;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrBitString;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrBoolean;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrNull;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrSequenceOf;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrVisibleString;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.Data.Choices;
import org.openmuc.jdlms.internal.asn1.cosem.Data.SubSeqOf_array;
import org.openmuc.jdlms.internal.asn1.cosem.Data.SubSeqOf_structure;
import org.openmuc.jdlms.internal.asn1.cosem.Data.SubSeq_compact_array;
import org.openmuc.jdlms.internal.asn1.cosem.EVENT_NOTIFICATION_Request;
import org.openmuc.jdlms.internal.asn1.cosem.Enum;
import org.openmuc.jdlms.internal.asn1.cosem.Integer16;
import org.openmuc.jdlms.internal.asn1.cosem.Integer32;
import org.openmuc.jdlms.internal.asn1.cosem.Integer64;
import org.openmuc.jdlms.internal.asn1.cosem.Integer8;
import org.openmuc.jdlms.internal.asn1.cosem.TypeDescription;
import org.openmuc.jdlms.internal.asn1.cosem.TypeDescription.SubSeq_array;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned16;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned32;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned64;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned8;

public final class DataConverter {

    public static DataObject convertDataToDataObject(Data pdu) {

        Choices choice = pdu.getChoiceIndex();
        ByteBuffer buf;
        List<DataObject> innerData;

        switch (choice) {
        case ARRAY:
            innerData = new LinkedList<>();
            for (Data item : pdu.array.list()) {
                innerData.add(convertDataToDataObject(item));
            }
            return DataObject.newArrayData(innerData);

        case STRUCTURE:
            innerData = new LinkedList<>();
            for (Data item : pdu.structure.list()) {
                innerData.add(convertDataToDataObject(item));
            }
            return DataObject.newStructureData(innerData);

        case BOOL:
            return DataObject.newBoolData(pdu.bool.getValue());

        case BIT_STRING:
            return DataObject.newBitStringData(new BitString(pdu.bit_string.getValue(), pdu.bit_string.getNumBits()));

        case DOUBLE_LONG:
            return DataObject.newInteger32Data((int) pdu.double_long.getValue());

        case DOUBLE_LONG_UNSIGNED:
            return DataObject.newUInteger32Data(pdu.double_long_unsigned.getValue());

        case OCTET_STRING:
            return DataObject.newOctetStringData(pdu.octet_string.getValue());

        case VISIBLE_STRING:
            return DataObject.newVisibleStringData(pdu.visible_string.getValue());

        case UTF8_STRING:
            return DataObject.newUtf8StringData(pdu.utf8_string.getValue());

        case BCD:
            return DataObject.newBcdData((byte) pdu.bcd.getValue());

        case INTEGER:
            return DataObject.newInteger8Data((byte) pdu.integer.getValue());

        case LONG_INTEGER:
            return DataObject.newInteger16Data((short) pdu.long_integer.getValue());

        case UNSIGNED:
            return DataObject.newUInteger8Data((short) pdu.unsigned.getValue());

        case LONG_UNSIGNED:
            return DataObject.newUInteger16Data((int) pdu.long_unsigned.getValue());

        case LONG64:
            return DataObject.newInteger64Data(pdu.long64.getValue());
        case LONG64_UNSIGNED:
            return DataObject.newUInteger64Data(pdu.long64_unsigned.getValue());

        case ENUMERATE:
            return DataObject.newEnumerateData((int) pdu.enumerate.getValue());

        case FLOAT32:
            buf = ByteBuffer.wrap(pdu.float32.getValue());
            return DataObject.newFloat32Data(buf.getFloat());

        case FLOAT64:
            buf = ByteBuffer.wrap(pdu.float64.getValue());
            return DataObject.newFloat64Data(buf.getDouble());

        case DATE_TIME:
            CosemDateTime dateTime = CosemDateTime.decode(pdu.date_time.getValue());
            return DataObject.newDateTimeData(dateTime);

        case DATE:
            CosemDate date = CosemDate.decode(pdu.date.getValue());
            return DataObject.newDateData(date);

        case TIME:
            CosemTime time = CosemTime.decode(pdu.time.getValue());
            return DataObject.newTimeData(time);

        case COMPACT_ARRAY:
            SubSeq_compact_array compactArray = pdu.compact_array;
            byte[] arrayContents = compactArray.array_contents.getValue();
            TypeDescription contentsDescription = compactArray.contents_description;

            TypeDesc typeDescription = convert(contentsDescription);
            CompactArray compactArrayS = new CompactArray(typeDescription, arrayContents);
            return DataObject.newCompactArrayData(compactArrayS);

        case DONT_CARE:
        case NULL_DATA:
        default:
            return DataObject.newNullData();
        }

    }

    private static TypeDesc convert(TypeDescription contentsDescription) {
        if (contentsDescription.getChoiceIndex() == TypeDescription.Choices.ARRAY) {
            SubSeq_array array = contentsDescription.array;
            int numOfeElements = (int) array.number_of_elements.getValue();
            TypeDesc typeDescription = convert(array.type_description);
            return new TypeDesc(new DescriptionArray(numOfeElements, typeDescription), TypeDesc.Type.ARRAY);
        }
        else if (contentsDescription.getChoiceIndex() == TypeDescription.Choices.STRUCTURE) {
            TypeDescription.SubSeqOf_structure structure = contentsDescription.structure;
            List<TypeDescription> list = structure.list();

            List<TypeDesc> structList = new ArrayList<>(list.size());
            for (TypeDescription typeDescription : list) {
                structList.add(convert(typeDescription));
            }
            return new TypeDesc(structList, TypeDesc.Type.STRUCTURE);
        }
        else {
            return new TypeDesc(TypeDesc.Type.forValue(contentsDescription.getChoiceIndex().getValue()));
        }

    }

    public static Data convertDataObjectToData(DataObject data) {
        Data result = new Data();

        Type type;

        if (data == null || (type = data.getType()) == Type.DONT_CARE) {
            result.setdont_care(new AxdrNull());
        }
        else if (data.isNull()) {
            result.setnull_data(new AxdrNull());
        }
        else if (data.isCosemDateFormat()) {
            CosemDateFormat cal = data.getValue();
            result.setoctet_string(new AxdrOctetString(cal.encode()));

        }
        else if (data.isNumber()) {
            result = convertNumberToPduData(data, type);

        }
        else if (data.isByteArray()) {
            result = converByteArrayToPduData(data, type);

        }
        else if (data.isBitString()) {
            BitString value = data.getValue();
            result.setbit_string(new AxdrBitString(value.getBitString(), value.getNumBits()));

        }
        else if (type == Type.BOOLEAN) {
            Boolean boolValue = data.getValue();
            result.setbool(new AxdrBoolean(boolValue));

        }
        else if (data.isComplex()) {
            result = convertComplexToPduData(data, type);
        }

        return result;
    }

    private static TypeDescription convert(TypeDesc typeDescription) {
        TypeDescription genTypeDescription = new TypeDescription(); // generated type
        switch (typeDescription.getType()) {
        case ARRAY:
            DescriptionArray array = typeDescription.getValue();
            genTypeDescription.setarray(
                    new SubSeq_array(new Unsigned16(array.getNumOfeElements()), convert(array.getTypeDescription())));
            break;
        case STRUCTURE:
            TypeDescription.SubSeqOf_structure newStruct = new TypeDescription.SubSeqOf_structure();
            List<TypeDesc> struct = typeDescription.getValue();
            for (TypeDesc typeDesc : struct) {
                newStruct.add(convert(typeDesc));
            }
            genTypeDescription.setstructure(newStruct);

            break;
        case BCD:
            genTypeDescription.setbcd(new AxdrNull());
            break;
        case BIT_STRING:
            genTypeDescription.setbit_string(new AxdrNull());
            break;
        case BOOL:
            genTypeDescription.setbool(new AxdrNull());
            break;
        case DATE:
            genTypeDescription.setdate(new AxdrNull());
            break;
        case DATE_TIME:
            genTypeDescription.setdate_time(new AxdrNull());
            break;
        case DONT_CARE:
            genTypeDescription.setdont_care(new AxdrNull());
            break;
        case DOUBLE_LONG:
            genTypeDescription.setdouble_long(new AxdrNull());
            break;
        case DOUBLE_LONG_UNSIGNED:
            genTypeDescription.setdouble_long_unsigned(new AxdrNull());
            break;
        case ENUMERATE:
            genTypeDescription.setenumerate(new AxdrNull());
            break;
        case FLOAT32:
            genTypeDescription.setfloat32(new AxdrNull());
            break;
        case FLOAT64:
            genTypeDescription.setfloat64(new AxdrNull());
            break;
        case INTEGER:
            genTypeDescription.setinteger(new AxdrNull());
            break;
        case LONG64:
            genTypeDescription.setlong64(new AxdrNull());
            break;
        case LONG64_UNSIGNED:
            genTypeDescription.setlong64_unsigned(new AxdrNull());
            break;
        case LONG_INTEGER:
            genTypeDescription.setlong_integer(new AxdrNull());
            break;
        case LONG_UNSIGNED:
            genTypeDescription.setlong_unsigned(new AxdrNull());
            break;
        case NULL_DATA:
            genTypeDescription.setnull_data(new AxdrNull());
            break;
        case OCTET_STRING:
            genTypeDescription.setoctet_string(new AxdrNull());
            break;
        case TIME:
            genTypeDescription.settime(new AxdrNull());
            break;
        case UNSIGNED:
            genTypeDescription.setunsigned(new AxdrNull());
            break;
        case UTF8_STRING:
            genTypeDescription.setutf8_string(new AxdrNull());
            break;
        case VISIBLE_STRING:
            genTypeDescription.setvisible_string(new AxdrNull());
            break;

        case ERR_NONE_SELECTED:
        default:
            throw new IllegalPametrizationError("Unknown type, can't convert.");
        }
        return genTypeDescription;
    }

    private static Data convertComplexToPduData(DataObject data, Type type) {
        Data result = new Data();

        if (data.getType() == Type.COMPACT_ARRAY) {
            CompactArray compactArray = data.getValue();

            TypeDescription contentsDescription = convert(compactArray.getTypeDescription());
            AxdrOctetString arrayContents = new AxdrOctetString(compactArray.getArrayContents());
            SubSeq_compact_array compVal = new SubSeq_compact_array(contentsDescription, arrayContents);
            result.setcompact_array(compVal);
        }
        else {

            List<DataObject> dataList = data.getValue();
            if (type == Type.STRUCTURE) {
                result.setstructure(new SubSeqOf_structure());
                setSeq(result.structure, dataList);
            }
            else if (type == Type.ARRAY) {
                result.setarray(new SubSeqOf_array());
                setSeq(result.array, dataList);
            }
        }

        return result;
    }

    private static void setSeq(AxdrSequenceOf<Data> seq, List<DataObject> dataList) {
        for (DataObject element : dataList) {
            seq.add(convertDataObjectToData(element));
        }
    }

    private static Data converByteArrayToPduData(DataObject data, Type type) {
        byte[] value = data.getValue();

        Data result = new Data();

        switch (type) {
        case OCTET_STRING:
            result.setoctet_string(new AxdrOctetString(value));
            break;
        case VISIBLE_STRING:
            result.setvisible_string(new AxdrVisibleString(value));
            break;
        case UTF8_STRING:
            result.setutf8_string(new AxdrOctetString(value));
            break;
        default:
            // can't be reached
            throw new IllegalPametrizationError("No such type: " + type);
        }

        return result;
    }

    private static Data convertNumberToPduData(DataObject data, Type type) {
        ByteBuffer buffer;
        Number value = data.getValue();

        Data result = new Data();

        switch (type) {
        case FLOAT64:

            buffer = ByteBuffer.allocate(8);
            buffer.putDouble(value.doubleValue());
            buffer.flip();

            result.setfloat64(new AxdrOctetString(8, buffer.array()));
            break;

        case FLOAT32:
            buffer = ByteBuffer.allocate(4);
            buffer.putDouble(value.floatValue());
            buffer.flip();

            result.setfloat32(new AxdrOctetString(4, buffer.array()));
            break;

        case ENUMERATE:
            result.setenumerate(new Enum(value.longValue()));
            break;

        case LONG64_UNSIGNED:
            result.setlong64_unsigned(new Unsigned64(value.longValue()));
            break;

        case LONG64:
            result.setlong64(new Integer64(value.longValue()));
            break;

        case LONG_UNSIGNED:
            result.setlong_unsigned(new Unsigned16(value.longValue()));
            break;

        case UNSIGNED:
            result.setunsigned(new Unsigned8(value.longValue()));
            break;

        case LONG_INTEGER:
            result.setlong_integer(new Integer16(value.longValue()));
            break;
        case INTEGER:
            result.setinteger(new Integer8(value.longValue()));
            break;
        case BCD:
            result.setbcd(new Integer8(value.longValue()));
            break;
        case DOUBLE_LONG_UNSIGNED:
            result.setdouble_long_unsigned(new Unsigned32(value.longValue()));
            break;
        case DOUBLE_LONG:
            result.setdouble_long(new Integer32(value.longValue()));

            break;
        default:
            // can't be rached
            throw new IllegalArgumentException("No such number: " + type);
        }
        return result;
    }

    public static EventNotification convertNotiReqToNotification(EVENT_NOTIFICATION_Request pdu) {
        int classId = (int) pdu.cosem_attribute_descriptor.class_id.getValue();
        int attributeId = (int) pdu.cosem_attribute_descriptor.attribute_id.getValue();

        byte[] obisCodeBytes = pdu.cosem_attribute_descriptor.instance_id.getValue();

        Long timestamp = null;
        if (pdu.time.isUsed()) {
            CosemDateTime dateTime = CosemDateTime.decode(pdu.time.getValue().getValue());

            timestamp = dateTime.toCalendar().getTimeInMillis();
        }

        DataObject newValue = null;
        if (pdu.attribute_value != null) {
            newValue = convertDataToDataObject(pdu.attribute_value);
        }

        return new EventNotification(new AttributeAddress(classId, new ObisCode(obisCodeBytes), attributeId), newValue,
                timestamp);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private DataConverter() {
    }
}
