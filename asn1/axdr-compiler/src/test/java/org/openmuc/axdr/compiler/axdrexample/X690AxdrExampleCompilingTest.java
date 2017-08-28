package org.openmuc.axdr.compiler.axdrexample;

import java.io.File;

import org.junit.Test;
import org.openmuc.axdr.compiler.Compiler;

public class X690AxdrExampleCompilingTest {

    @Test
    public void compiling() throws Exception {

        System.out.println(new File(".").getAbsolutePath());

        String[] args = new String[] { "-o", "src/test/java/org/openmuc/axdr/compiler/axdrexample/generated", "-p",
                "org.openmuc.axdr.compiler.axdrexample.generated", "src/test/resources/x690AxdrExample.asn" };
        Compiler.main(args);

    }

}
