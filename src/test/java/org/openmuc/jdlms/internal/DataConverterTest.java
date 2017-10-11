package org.openmuc.jdlms.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.asn1.cosem.Data;

public class DataConverterTest {

    @Test
    public void testFloatToData() {
        float expectedV = 1000f;
        Data d = DataConverter.convertDataObjectToData(DataObject.newFloat32Data(expectedV));
        float f = DataConverter.convertDataToDataObject(d).getValue();

        assertEquals(expectedV, f, .01f);
    }

    @Test
    public void testFloat64ToData() {
        double expectedV = 1000d;
        Data d = DataConverter.convertDataObjectToData(DataObject.newFloat64Data(expectedV));
        double f = DataConverter.convertDataToDataObject(d).getValue();

        assertEquals(expectedV, f, .01f);
    }

}
