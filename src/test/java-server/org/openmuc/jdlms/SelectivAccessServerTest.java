package org.openmuc.jdlms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmuc.jdlms.datatypes.DataObject;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;

@RunWith(JUnitParamsRunner.class)
public class SelectivAccessServerTest {

    @Test(expected = IllegalPametrizationError.class)
    @Parameters(method = "paramsT1")
    @TestCaseName("Testing error case with {0} {1}")
    public void test_exception_handling(CosemInterfaceObject... cosemInterfaceObject) throws Exception {
        excecute(cosemInterfaceObject);
    }

    public Object paramsT1() {
        return new Object[][] { { new DemoClass() }, { new DemoClass2() }, { new DemoClass3(), new DemoClass3() },
                { new DemoClass4() }, { new DemoClass8() } };
    }

    private static LogicalDevice newDemoLD() {
        return new LogicalDevice(1, "", "ISE", 1);
    }

    @CosemClass(id = 2)
    public class DemoClass extends CosemObj0 {
        @CosemAttribute(id = 1)
        private DataObject d1;
    }

    @CosemClass(id = 2)
    public class DemoClass2 extends CosemObj0 {
        @CosemAttribute(id = 2)
        private DataObject d1;
        @CosemAttribute(id = 2)
        private DataObject d2;
    }

    @CosemClass(id = 2)
    public class DemoClass3 extends CosemObj0 {
        @CosemAttribute(id = 2)
        private DataObject d1;
    }

    @CosemClass(id = 2)
    public class DemoClass4 extends CosemObj0 {
        @CosemAttribute(id = 2, selector = 1)
        private DataObject d1;
    }

    @Test
    @Parameters(method = "paramsT2")
    @TestCaseName("test valid case with {0}")
    public void test_valid_case(CosemInterfaceObject cosemInterfaceObject) throws Exception {
        excecute(cosemInterfaceObject);
    }

    private void excecute(CosemInterfaceObject... cosemInterfaceObject) throws Exception {
        LogicalDevice logicalDevice = newDemoLD();
        logicalDevice.registerCosemObject(cosemInterfaceObject);
        DlmsServer.tcpServerBuilder(0).registerLogicalDevice(logicalDevice).build().close();
    }

    public Object paramsT2() {
        return new Object[][] { { new DemoClass5() }, { new DemoClass6() }, { new DemoClass7() },
                { new DemoClass9() } };
    }

    @CosemClass(id = 2)
    public class DemoClass5 extends CosemObj0 {
        @CosemAttribute(id = 2)
        private DataObject d1;

        public DataObject getD1() {
            return d1;
        }

        public void setD1(DataObject d1) {
            this.d1 = d1;
        }
    }

    @CosemClass(id = 2)
    public class DemoClass6 extends CosemObj0 {
        @CosemAttribute(id = 2, selector = 1)
        private DataObject d1;

        public DataObject getD1(SelectiveAccessDescription selectiveAccessDescription) {
            return d1;
        }

        public void setD1(DataObject d1, SelectiveAccessDescription selectiveAccessDescription) {
            this.d1 = d1;
        }
    }

    @CosemClass(id = 2)
    public class DemoClass7 extends CosemObj0 {
        @CosemAttribute(id = 2)
        private DataObject d1;

        public DataObject getD1(Long id) {
            return d1;
        }

        public void setD1(DataObject d1) {
            this.d1 = d1;
        }
    }

    @CosemClass(id = 2)
    public class DemoClass8 extends CosemObj0 {
        @CosemAttribute(id = 2)
        private DataObject d1;

        public DataObject getD1(SelectiveAccessDescription selectiveAccessDescription, Long id) {
            return d1;
        }

        public void setD1(DataObject d1) {
            this.d1 = d1;
        }
    }

    @CosemClass(id = 2)
    public class DemoClass9 extends CosemObj0 {

        @CosemAttribute(id = 2, selector = { 1, 2 }, accessMode = AttributeAccessMode.READ_ONLY)
        private DataObject d1;

        public DataObject getD1(SelectiveAccessDescription selectiveAccessDescription, Long id) {
            return d1;
        }

        public void setD1(DataObject d1, SelectiveAccessDescription selectiveAccessDescription, Long id) {
            this.d1 = d1;
        }
    }

    public abstract class CosemObj0 extends CosemInterfaceObject {

        public CosemObj0() {
            super("99.0.0.0.99.0");
        }

    }
}
