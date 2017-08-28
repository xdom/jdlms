/*
 * Copyright 2012-14 Fraunhofer ISE
 *
 * This file is part of AXDR-Compiler.
 * For more information visit http://www.openmuc.org
 *
 * AXDR-Compiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AXDR-Compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AXDR-Compiler.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:

 Copyright 2006-2011 Abdulla Abdurakhmanov (abdulla@latestbit.com)
 Original sources are available at www.latestbit.com

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.openmuc.axdr.compiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.bn.compiler.parser.ASNLexer;
import org.bn.compiler.parser.ASNParser;
import org.bn.compiler.parser.model.ASN1Model;
import org.bn.compiler.parser.model.ASNModule;

public class Compiler {

    private static String outputBaseDir = "";
    private static String basePackageName = "asn1";
    private static boolean printXml = false;
    private static String inputFile = null;

    private static void printUsage() {
        System.out.println(
                "SYNOPSIS\n\torg.openmuc.axdr.compiler.Compiler -o <output_dir> -p <package_name> [-x] <asn1_file>");
        System.out.println(
                "DESCRIPTION\n\tThe compiler reads the ASN.1 definition from the given files and generates coresponding Java classes that can be used to conveniently encode/decode BER encoded data.");
        System.out.println("OPTIONS");
        System.out.println(
                "\t-o <output_dir>\n\t    The base directory for the generated Java classes. The class files will be saved in subfolders of the base directory coresponding to the name of the defined modules.\n");
        System.out.println(
                "\t-p <package_name>\n\t    The base package name. Added to this will be a name generated from the module name.\n");
        System.out.println(
                "\t[-x]\n\t    Print an XML structure that represents the given ASN.1 definition to standard out.\n");
        System.out.println("\t<asn1_file>\n\t    ASN.1 file.\n");
    }

    public static void main(String args[]) throws Exception {

        if (!parseArgs(args)) {
            printUsage();
            System.exit(1);
        }

        System.out.println("Generated code will be saved in: " + outputBaseDir);

        ByteArrayOutputStream outputXml = getXMLStream(outputBaseDir, basePackageName, inputFile);
        InputStream stream = new ByteArrayInputStream(outputXml.toByteArray());

        if (printXml) {
            System.out.println(new String(outputXml.toByteArray()));
        }

        XmlToJavaTranslator xmlToJavaTranslator = new XmlToJavaTranslator(stream, outputBaseDir);
        xmlToJavaTranslator.translate();

    }

    static private ByteArrayOutputStream getXMLStream(String outputDir, String nameSpace, String inputFileName)
            throws PropertyException, Exception, JAXBException {

        ByteArrayOutputStream outputXml = new ByteArrayOutputStream(65535);

        JAXBContext jc = JAXBContext.newInstance("org.bn.compiler.parser.model");
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        ASN1Model model = createModel(inputFileName);

        model.outputDirectory = outputDir;
        if (nameSpace != null) {
            model.moduleNS = nameSpace;
        }
        else {
            model.moduleNS = model.module.moduleIdentifier.name.toLowerCase();
        }

        marshaller.marshal(model, outputXml);

        return outputXml;

    }

    static private ASN1Model createModel(String inputFileName) throws Exception {
        InputStream stream = new FileInputStream(inputFileName);
        ASNLexer lexer = new ASNLexer(stream);
        ASNParser parser = new ASNParser(lexer);
        ASNModule module = new ASNModule();

        parser.module_definition(module);

        ASN1Model model = new ASN1Model();

        model.module = module;

        return model;
    }

    private static boolean parseArgs(String[] args) {
        if (args.length < 5) {
            return false;
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o")) {
                i++;
                if (i == args.length) {
                    return false;
                }
                outputBaseDir = args[i];
            }
            else if (args[i].equals("-p")) {
                i++;
                if (i == args.length) {
                    return false;
                }
                basePackageName = args[i];
            }
            else if (args[i].equals("-x")) {
                printXml = true;
            }
            else {
                inputFile = args[i];
            }
        }

        if (inputFile == null) {
            return false;
        }
        return true;
    }

}
