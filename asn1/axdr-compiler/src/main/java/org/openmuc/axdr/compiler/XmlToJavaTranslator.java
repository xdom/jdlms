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
 */

package org.openmuc.axdr.compiler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlToJavaTranslator {

    private final Document doc;
    private final String outputDir;
    private final XPath xPath;
    private final Element asnTypesElement;
    private final String packageName;
    private int indentNum = 0;
    BufferedWriter out;
    boolean defaultExplicit;

    XmlToJavaTranslator(InputStream xmlInputStream, String outputDir)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();

        doc = builder.parse(xmlInputStream);
        this.outputDir = outputDir;

        XPathFactory xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();

        Element asn1Model = (Element) doc.getElementsByTagName("asn1Model").item(0);

        packageName = ((Element) asn1Model.getElementsByTagName("moduleNS").item(0)).getTextContent();

        asnTypesElement = (Element) doc.getElementsByTagName("asnTypes").item(0);

        defaultExplicit = !(xPath.evaluate("module/tagDefault", asn1Model).equals("IMPLICIT"));
    }

    public void translate() throws XPathExpressionException, IOException {

        NodeList asnTypes = asnTypesElement.getChildNodes();

        for (int i = 0; i < asnTypes.getLength(); i++) {

            if (asnTypes.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element asn1TypeElement = (Element) asnTypes.item(i);

            String tagClass = "";
            String className = "";
            String typeName = asn1TypeElement.getAttribute("xsi:type");

            if (typeName.equals("asnTaggedType")) {

                // Parse only if explicitly tagged
                if (isExplicitlyTagged(asn1TypeElement)) {

                    // According to IEC 61334-6 §6.7 (last paragraph) explicitly tagged variables are
                    // encoded as their BER counterparts. As such, any variable that is explicitly tagged
                    // will be extending the corresponding Ber* java class instead if its Axdr* class.

                    if (xPath.evaluate("typeReference", asn1TypeElement).equals("")) {
                        throw new IOException("ASN1 element " + xPath.evaluate("name", asn1TypeElement)
                                + ": this kind of definition where an element equals aother element with an implicit tag is not supported by this compiler");
                    }

                    tagClass = getTagClass(asn1TypeElement);
                    String tagNum = getTagNum(asn1TypeElement);

                    className = getSequenceElementName(asn1TypeElement);

                    // Change extending type from Axdr* to Ber*
                    String classParent = getElementType(asn1TypeElement, true);
                    if (classParent.indexOf("Axdr") == 0) {
                        classParent = "Ber" + classParent.substring(4);
                    }

                    asn1TypeElement = (Element) xPath.evaluate("typeReference", asn1TypeElement, XPathConstants.NODE);
                    String asn1TypeElementString = getASNType(asn1TypeElement);

                    if (asn1TypeElementString.equals("asnInteger")
                            || (asn1TypeElementString.startsWith("asn") && asn1TypeElementString.endsWith("String"))) {

                        FileWriter fstream = new FileWriter(outputDir + "/" + className + ".java");
                        out = new BufferedWriter(fstream);
                        writeHeader(new String[] { "import java.util.List;", "import java.util.LinkedList;",
                                "import java.io.UnsupportedEncodingException;", "import org.openmuc.jasn1.ber.*;",
                                "import org.openmuc.jasn1.ber.types.*;" });

                        if (tagClass.equals("")) {
                            tagClass = "CONTEXT_CLASS";
                        }

                        write("public class " + className + " extends " + classParent + " {\n");

                        write("public final static BerTag identifier = new BerTag(BerTag." + tagClass
                                + ", BerTag.PRIMITIVE, " + tagNum + ");\n");

                        write("public " + className + "() {");
                        write("}\n");

                        // if (!getASNType(asn1TypeElement).equals("asnNull")) {
                        // write("public " + className + "(byte[] code) {");
                        // write("id = identifier;");
                        // write("this.code = code;");
                        // write("}\n");
                        // }

                        if (asn1TypeElementString.equals("asnInteger")) {
                            write("public " + className + "(long value) {");
                            write("this.value = value;");
                            write("}\n");
                        }
                        else if (asn1TypeElementString.equals("asnReal")) {
                            write("public " + className + "(double value) {");
                            write("this.value = value;");
                            write("}\n");
                        }
                        else if (asn1TypeElementString.equals("asnBoolean")) {
                            write("public " + className + "(boolean value) {");
                            write("this.value = value;");
                            write("}\n");
                        }
                        else if (asn1TypeElementString.equals("asnObjectIdentifier")) {
                            write("public " + className + "(int[] objectIdentifierComponents) {");
                            write("this.value = objectIdentifierComponents;");
                            write("}\n");
                        }
                        else if (asn1TypeElementString.equals("asnEnum")) {
                            write("public " + className + "(long value) {");
                            write("this.value = val;");
                            write("}\n");
                        }
                        else if (asn1TypeElementString.equals("asnBitString")) {
                            write("public " + className + "(byte[] bitString, int numBits) {");
                            write("if ((numBits <= (((bitString.length - 1) * 8) + 1)) || (numBits > (bitString.length * 8))) {");
                            write("throw new IllegalArgumentException(\"numBits out of bound.\");");
                            write("}\n");

                            write("this.value = bitString;");
                            write("this.numBits = numBits;");
                            write("}\n");
                        }
                        else if ((asn1TypeElementString.startsWith("asn") && asn1TypeElementString.endsWith("String"))
                                || asn1TypeElementString.equals("asnGeneralizedTime")) {
                            write("public " + className + "(byte[] value) {");
                            write("this.value = value;");
                            write("}\n");

                            if (asn1TypeElementString.equals("asnVisibleString")) {

                                write("public " + className
                                        + "(String visibleString) throws UnsupportedEncodingException {");
                                write("this.value = visibleString.getBytes(\"US-ASCII\");");
                                write("}\n");
                            }
                            else if (asn1TypeElementString.equals("asnUTF8String")) {

                                write("public " + className
                                        + "(String visibleString) throws UnsupportedEncodingException {");
                                write("this.value = visibleString.getBytes(\"UTF-8\");");
                                write("}\n");
                            }
                        }

                        write("public int encode(BerByteArrayOutputStream berOStream) throws IOException {");
                        write("return encode(berOStream, false) + identifier.encode(berOStream);");
                        write("}\n");

                        write("public int decode(InputStream iStream) throws IOException {");
                        write("return identifier.decodeAndCheck(iStream) + decode(iStream, false);");
                        write("}\n");

                        write("}");

                        out.close();
                    }
                }
                else {
                    // Implicitly tagged, get underlying type
                    className = getSequenceElementName(asn1TypeElement);
                    asn1TypeElement = (Element) xPath.evaluate("typeReference", asn1TypeElement, XPathConstants.NODE);
                    typeName = asn1TypeElement.getAttribute("xsi:type");

                    if (typeName.endsWith("String")) {
                        FileWriter fstream = new FileWriter(outputDir + "/" + className + ".java");
                        out = new BufferedWriter(fstream);
                        writeHeader(new String[] {});

                        write("public class " + className + " extends AxdrVisibleString {\n");

                        write("public " + className + "() {");
                        write("super();");
                        write("}\n");

                        write("public " + className + "(byte[] octetString) {");
                        write("super(octetString);");
                        write("}\n");
                        write("}\n");
                        out.close();
                    }
                }
            }

            if (typeName.equals("asnInteger")) {
                writeIntegerClass(asn1TypeElement, className);
            }

            if (typeName.equals("asnEnum")) {
                writeEnumClass(asn1TypeElement);
            }

            if (typeName.equals("asnBitString")) {
                writeBitStringClass(asn1TypeElement, className);
            }

            if (typeName.equals("asnOctetString")) {
                writeOctetStringClass(asn1TypeElement, className);
            }

            if (!typeName.equals("asnSequenceSet") && !typeName.equals("asnSequenceOf")
                    && !typeName.equals("asnChoice")) {
                continue;
            }

            if (className.equals("")) {
                className = getSequenceElementName(asn1TypeElement);
            }

            FileWriter fstream = new FileWriter(outputDir + "/" + className + ".java");
            out = new BufferedWriter(fstream);
            writeHeader(new String[] {});

            writeClass(asn1TypeElement, className, false);

            out.close();
        }

    }

    private void writeIntegerClass(Element asn1TypeElement, String className)
            throws XPathExpressionException, IOException {
        if (className.isEmpty()) {
            className = getSequenceElementName(asn1TypeElement);
        }
        String minValue = xPath.evaluate(
                "constraint/elemSetSpec/intersectionList/cnsElemList/lEndValue/signedNumber/num", asn1TypeElement);
        String positive = xPath.evaluate(
                "constraint/elemSetSpec/intersectionList/cnsElemList/lEndValue/signedNumber/positive", asn1TypeElement);
        if ("false".equals(positive)) {
            minValue = "-".concat(minValue);
        }
        String maxValue = xPath.evaluate(
                "constraint/elemSetSpec/intersectionList/cnsElemList/uEndValue/signedNumber/num", asn1TypeElement);
        positive = xPath.evaluate("constraint/elemSetSpec/intersectionList/cnsElemList/uEndValue/signedNumber/positive",
                asn1TypeElement);
        if ("false".equals(positive)) {
            maxValue = "-".concat(maxValue);
        }

        FileWriter fstream = new FileWriter(outputDir + "/" + className + ".java");
        out = new BufferedWriter(fstream);

        writeHeader(new String[] { "import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrInteger;" });

        write("public class " + className + " extends AxdrInteger {\n");

        writeIntegerConstructor(className, minValue, maxValue);

        write("}\n");

        out.close();
    }

    private void writeEnumClass(Element asn1TypeElement) throws XPathExpressionException, IOException {
        String className = getSequenceElementName(asn1TypeElement);
        NodeList enumElements = (NodeList) xPath.evaluate("namedNumberList/namedNumbers", asn1TypeElement,
                XPathConstants.NODESET);

        FileWriter fstream = new FileWriter(outputDir + "/" + className + ".java");
        out = new BufferedWriter(fstream);

        writeHeader(new String[] { "import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;" });

        write("public class " + className + " extends AxdrEnum {\n");

        write("public " + className + "() {");
        write("super();");
        write("}\n");

        write("public " + className + "(byte[] code) {");
        write("super(code);");
        write("}\n");

        write("public " + className + "(long val) {");
        write("super(val);");
        write("}\n");

        for (int j = 0; j < enumElements.getLength(); j++) {
            Element enumElement = (Element) enumElements.item(j);

            String name = getSequenceElementName(enumElement).toUpperCase();
            String value = xPath.evaluate("signedNumber/num", enumElement);

            write("public static final int " + name + " = " + value + ";");
        }

        write("}\n");

        out.close();
    }

    private void writeBitStringClass(Element asn1TypeElement, String className)
            throws XPathExpressionException, IOException {
        if (className.isEmpty()) {
            className = getSequenceElementName(asn1TypeElement);
        }

        String bitLengthString = xPath.evaluate(
                "constraint/elemSetSpec/intersectionList/cnsElemList/constraint/elemSetSpec/intersectionList/cnsElemList/value/signedNumber/num",
                asn1TypeElement);
        if ("".equals(bitLengthString)) {
            bitLengthString = "0";
        }

        int bitLength = Integer.parseInt(bitLengthString);
        FileWriter fstream = new FileWriter(outputDir + "/" + className + ".java");
        out = new BufferedWriter(fstream);

        writeHeader(new String[] { "import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrBitString;" });

        write("public class " + className + " extends AxdrBitString {\n");

        write("public " + className + "() {");
        write("super(" + bitLength + ");");
        write("}\n");

        write("public " + className + "(byte[] bytes) {");
        write("super(bytes);");
        write("}\n");

        write("}\n");

        out.close();
    }

    private void writeOctetStringClass(Element asn1TypeElement, String className)
            throws XPathExpressionException, IOException {
        if (className.isEmpty()) {
            className = getSequenceElementName(asn1TypeElement);
        }

        String octetLengthString = xPath.evaluate(
                "constraint/elemSetSpec/intersectionList/cnsElemList/constraint/elemSetSpec/intersectionList/cnsElemList/value/signedNumber/num",
                asn1TypeElement);
        if ("".equals(octetLengthString)) {
            octetLengthString = "0";
        }

        int octetLength = Integer.parseInt(octetLengthString);

        FileWriter fstream = new FileWriter(outputDir + "/" + className + ".java");
        out = new BufferedWriter(fstream);

        writeHeader(new String[] { "import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;" });

        write("public class " + className + " extends AxdrOctetString {\n");

        write("public static final int length = " + octetLength + ";");

        write("public " + className + "() {");
        write("super( length );");
        write("}\n");

        write("public " + className + "(byte[] octetString) {");
        write("super( length, octetString );");
        write("}\n");

        write("}\n");

        out.close();
    }

    private void writeClass(Element asn1TypeElement, String className, boolean isStatic)
            throws IOException, XPathExpressionException {
        if (asn1TypeElement.getAttribute("xsi:type").equals("asnSequenceSet")) {

            writeSequenceClass(asn1TypeElement, className, isStatic);

        }
        else if (asn1TypeElement.getAttribute("xsi:type").equals("asnSequenceOf")) {

            writeSequenceOfClass(asn1TypeElement, className, isStatic);

        }
        else if (asn1TypeElement.getAttribute("xsi:type").equals("asnChoice")) {
            writeChoiceClass(asn1TypeElement, className, isStatic);

        }
    }

    private void writeClass(Element asn1TypeElement, String tagNum, String tagClass, String className, boolean isStatic)
            throws IOException, XPathExpressionException {
        if (asn1TypeElement.getAttribute("xsi:type").equals("asnSequenceSet")) {

            writeSequenceClass(asn1TypeElement, className, isStatic);

        }
        else if (asn1TypeElement.getAttribute("xsi:type").equals("asnSequenceOf")) {

            writeSequenceOfClass(asn1TypeElement, className, isStatic);

        }
        else if (asn1TypeElement.getAttribute("xsi:type").equals("asnChoice")) {
            writeChoiceClass(asn1TypeElement, className, isStatic);

        }
    }

    private void writeChoiceClass(Element asn1TypeElement, String className, boolean isStatic)
            throws IOException, XPathExpressionException {

        if (className.equals("")) {
            className = xPath.evaluate("name", asn1TypeElement);
        }

        String isStaticStr = "";
        if (isStatic) {
            isStaticStr = " static";
        }

        write("public" + isStaticStr + " class " + className + " implements AxdrType {\n");

        write("public byte[] code = null;\n");

        NodeList sequenceElements = (NodeList) xPath.evaluate("elementTypeList/elements", asn1TypeElement,
                XPathConstants.NODESET);

        /*
         * write("public static class Choices {"); for (int j = 0; j < sequenceElements.getLength(); j++) { Element
         * sequenceElement = (Element) sequenceElements.item(j);
         * 
         * String choiceName = getSequenceElementName(sequenceElement).toUpperCase(); int choiceNum =
         * Integer.parseInt(getTagNum(sequenceElement));
         * 
         * write("public static final int "+choiceName+" = "+choiceNum+";"); } write("}\n");
         */

        write("public static enum Choices {");
        write("_ERR_NONE_SELECTED(-1),");
        for (int j = 0; j < sequenceElements.getLength(); j++) {
            Element sequenceElement = (Element) sequenceElements.item(j);

            String choiceName = getSequenceElementName(sequenceElement).toUpperCase();
            int choiceNum = Integer.parseInt(getTagNum(sequenceElement));

            write(choiceName + "(" + choiceNum + "),");
        }
        write(";");

        write("");
        write("private int value;\n");
        write("private Choices(int value) {");
        write("this.value = value;");
        write("}\n");
        write("public int getValue() { return this.value; }\n");
        write("public static Choices valueOf(long tagValue) {");
        write("Choices[] values = Choices.values();\n");
        write("for (Choices c : values) {");
        write("if (c.value == tagValue) { return c; }");
        write("}");
        write("return _ERR_NONE_SELECTED;");
        write("}");
        write("}\n");

        write("private Choices choice;\n");

        for (int j = 0; j < sequenceElements.getLength(); j++) {
            Element sequenceElement = (Element) sequenceElements.item(j);

            Element subElementRef = (Element) xPath.evaluate("typeReference", sequenceElement, XPathConstants.NODE);

            if (subElementRef != null) {
                String subClassName = getElementType(sequenceElement, true);
                writeClass(subElementRef, "", "", subClassName, true);
            }
        }

        writePublicMembers(out, sequenceElements);

        writeEmptyConstructor(className, true);

        writeChoiceEncodeFunction(sequenceElements);

        writeChoiceDecodeFunction(sequenceElements);

        writeEncodeAndSaveFunction();

        writeChoiceMethods(sequenceElements);

        writeChoiceToStringFunction(sequenceElements);

        write("}\n");

    }

    private void writeChoiceMethods(NodeList sequenceElements) throws IOException, XPathExpressionException {

        write("public Choices getChoiceIndex() {");
        write("return this.choice;");
        write("}\n");

        for (int j = 0; j < sequenceElements.getLength(); j++) {
            Element sequenceElement = (Element) sequenceElements.item(j);

            String choiceName = getSequenceElementName(sequenceElement);

            write("public void set" + choiceName + "(" + getElementType(sequenceElement, true) + " newVal) {");
            write("resetChoices();");
            write("choice = Choices." + choiceName.toUpperCase() + ";");
            write(choiceName + " = newVal;");
            write("}\n");
        }

        write("private void resetChoices() {");
        write("choice = Choices._ERR_NONE_SELECTED;");
        for (int j = 0; j < sequenceElements.getLength(); j++) {
            Element sequenceElement = (Element) sequenceElements.item(j);

            write(getSequenceElementName(sequenceElement) + " = null;");
        }
        write("}\n");
    }

    private void writeChoiceDecodeFunction(NodeList sequenceElements) throws IOException, XPathExpressionException {
        write("public int decode(InputStream iStream) throws IOException {");
        write("int codeLength = 0;");
        write("AxdrEnum choosen = new AxdrEnum();\n");

        write("codeLength += choosen.decode(iStream);");
        write("resetChoices();");
        write("this.choice = Choices.valueOf(choosen.getValue());\n");

        for (int j = 0; j < sequenceElements.getLength(); j++) {
            Element sequenceElement = (Element) sequenceElements.item(j);

            write("if (choice == Choices." + getSequenceElementName(sequenceElement).toUpperCase() + ") {");
            write(getSequenceElementName(sequenceElement) + " = new " + getElementType(sequenceElement, true) + "();");
            write("codeLength += " + getSequenceElementName(sequenceElement) + ".decode(iStream);");
            write("return codeLength;");
            write("}\n");

        }

        write("throw new IOException(\"Error decoding AxdrChoice: Identifier matched to no item.\");");

        write("}\n");

    }

    private void writeChoiceToStringFunction(NodeList sequenceElements) throws IOException, XPathExpressionException {
        write("public String toString() {");

        for (int j = 0; j < sequenceElements.getLength(); j++) {
            Element sequenceElement = (Element) sequenceElements.item(j);

            write("if (choice == Choices." + getSequenceElementName(sequenceElement).toUpperCase() + ") {");
            write("return \"choice: {" + getSequenceElementName(sequenceElement) + ": \" + "
                    + getSequenceElementName(sequenceElement) + " + \"}\";");
            write("}\n");

        }

        write("return \"unknown\";");

        write("}\n");

    }

    private void writeChoiceEncodeFunction(NodeList sequenceElements) throws IOException, XPathExpressionException {
        write("public int encode(BerByteArrayOutputStream axdrOStream) throws IOException {");

        write("if (code != null) {");

        write("for (int i = code.length - 1; i >= 0; i--) {");
        write("axdrOStream.write(code[i]);");
        write("}");
        write("return code.length;\n");
        write("}");

        write("if (choice == Choices._ERR_NONE_SELECTED) {");
        write("throw new IOException(\"Error encoding AxdrChoice: No item in choice was selected.\");");
        write("}\n");

        write("int codeLength = 0;\n");

        for (int j = sequenceElements.getLength() - 1; j >= 0; j--) {

            Element sequenceElement = (Element) sequenceElements.item(j);

            int choiceNum = Integer.parseInt(getTagNum(sequenceElement));

            write("if (choice == Choices." + getSequenceElementName(sequenceElement).toUpperCase() + ") {");

            write("codeLength += " + getSequenceElementName(sequenceElement) + ".encode(axdrOStream);");

            write("AxdrEnum c = new AxdrEnum(" + choiceNum + ");");
            write("codeLength += c.encode(axdrOStream);");

            write("return codeLength;");

            write("}\n");

        }

        write("// This block should be unreachable");
        write("throw new IOException(\"Error encoding AxdrChoice: No item in choice was encoded.\");");

        write("}\n");

    }

    private void writeSequenceClass(Element asn1TypeElement, String className, boolean isStatic)
            throws IOException, XPathExpressionException {

        if (className.equals("")) {
            className = xPath.evaluate("name", asn1TypeElement);
        }

        String isStaticStr = "";
        if (isStatic) {
            isStaticStr = " static";
        }

        write("public" + isStaticStr + " class " + className + " implements AxdrType {\n");

        NodeList sequenceElements = (NodeList) xPath.evaluate("elementTypeList/elements", asn1TypeElement,
                XPathConstants.NODESET);

        for (int j = 0; j < sequenceElements.getLength(); j++) {
            Element sequenceElement = (Element) sequenceElements.item(j);

            Element subElementRef = (Element) xPath.evaluate("typeReference", sequenceElement, XPathConstants.NODE);

            if (subElementRef != null) {
                String subClassName = getElementType(sequenceElement, true);
                writeClass(subElementRef, subClassName, true);
            }
        }

        write("public byte[] code = null;");

        writePublicMembers(out, sequenceElements);

        writeEmptyConstructor(className, true);

        writeEncodeConstructor(className, sequenceElements, true);

        writeSequenceEncodeFunction(sequenceElements);

        writeSequenceDecodeFunction(sequenceElements);

        writeEncodeAndSaveFunction();

        writeSequenceToStringFunction(sequenceElements);

        write("}\n");

    }

    private void writeSequenceOfClass(Element asn1TypeElement, String className, boolean isStatic)
            throws IOException, XPathExpressionException {

        String isStaticStr = "";
        if (isStatic) {
            isStaticStr = " static";
        }

        Element subElementRef = (Element) xPath.evaluate("typeReference", asn1TypeElement, XPathConstants.NODE);
        String typeName = getElementType(asn1TypeElement, false);

        String classLine = "public" + isStaticStr + " class " + className + " extends AxdrSequenceOf<";
        if (subElementRef != null) {
            classLine += className + ".";
        }
        classLine += typeName + "> {\n";

        write(classLine);

        write("protected " + typeName + " createListElement() {");
        write("return new " + typeName + "();");
        write("}");

        write("protected " + className + "(int length) {");
        write("super(length);");
        write("}");

        write("public " + className + "() {} // Call empty base constructor\n");

        if (subElementRef != null) {
            String subClassName = getElementType(asn1TypeElement, false);
            writeClass(subElementRef, "", "", subClassName, true);
        }

        write("}\n");
    }

    private void writeSequenceDecodeFunction(NodeList sequenceElements) throws IOException, XPathExpressionException {
        write("public int decode(InputStream iStream) throws IOException {");
        write("int codeLength = 0;\n");

        for (int j = 0; j < sequenceElements.getLength(); j++) {
            Element sequenceElement = (Element) sequenceElements.item(j);

            String elementType = getElementType(sequenceElement, true);
            String subType = elementType;
            String value = "new " + elementType + "()";
            if (isOptional(sequenceElement)) {
                elementType = "AxdrOptional<" + elementType + ">";
                value = "new " + elementType + "(new " + subType + "(), false)";
            }
            else if (hasDefault(sequenceElement)) {
                elementType = "AxdrDefault<" + elementType + ">";
                value = xPath.evaluate("value/definedValue/name", sequenceElement);
                value = "new " + elementType + "(new " + subType + "(), new " + subType + "(" + value + "))";
            }

            write(getSequenceElementName(sequenceElement) + " = " + value + ";");

            write("codeLength += " + getSequenceElementName(sequenceElement) + ".decode(iStream);\n");
        }

        write("return codeLength;");
        write("}\n");
    }

    private void writeSequenceEncodeFunction(NodeList sequenceElements) throws IOException, XPathExpressionException {
        write("public int encode(BerByteArrayOutputStream axdrOStream) throws IOException {\n");

        write("int codeLength;\n");

        write("if (code != null) {");
        write("codeLength = code.length;");
        write("for (int i = code.length - 1; i >= 0; i--) {");
        write("axdrOStream.write(code[i]);");
        write("}");
        write("}");
        write("else {");

        write("codeLength = 0;");

        for (int j = sequenceElements.getLength() - 1; j >= 0; j--) {

            Element sequenceElement = (Element) sequenceElements.item(j);

            write("codeLength += " + getSequenceElementName(sequenceElement) + ".encode(axdrOStream);");

            write("");

        }

        write("}\n");

        write("return codeLength;\n");

        write("}\n");
    }

    private void writeHeader(String[] additionalImports) throws IOException {

        write("/**");
        write(" * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)\n */\n");
        write("package " + packageName + ";\n");

        write("import java.io.IOException;");
        write("import java.io.InputStream;");
        if (additionalImports != null) {
            for (String importStatement : additionalImports) {
                write(importStatement);
            }
        }
        write("import org.openmuc.jasn1.ber.BerByteArrayOutputStream;");
        write("import org.openmuc.jdlms.internal.asn1.axdr.*;");
        write("import org.openmuc.jdlms.internal.asn1.axdr.types.*;\n");

    }

    private void writePublicMembers(BufferedWriter out, NodeList sequenceElements)
            throws XPathExpressionException, IOException {
        for (int j = 0; j < sequenceElements.getLength(); j++) {
            Element sequenceElement = (Element) sequenceElements.item(j);

            String elementType = getElementType(sequenceElement, true);
            String subType = "";
            String value = "null";
            if (isOptional(sequenceElement)) {
                subType = elementType;
                elementType = "AxdrOptional<" + elementType + ">";
                value = "new " + elementType + "(new " + subType + "(), false)";
            }
            else if (hasDefault(sequenceElement)) {
                subType = elementType;
                elementType = "AxdrDefault<" + elementType + ">";
                value = xPath.evaluate("value/definedValue/name", sequenceElement);
                value = "new " + elementType + "(new " + subType + "(), new " + subType + "(" + value + "))";
            }

            write("public " + elementType + " " + getSequenceElementName(sequenceElement) + " = " + value + ";\n");

        }
    }

    private void writeEncodeAndSaveFunction() throws IOException {
        write("public void encodeAndSave(int encodingSizeGuess) throws IOException {");
        write("BerByteArrayOutputStream axdrOStream = new BerByteArrayOutputStream(encodingSizeGuess);");
        write("encode(axdrOStream);");
        write("code = axdrOStream.getArray();");
        write("}");
    }

    private void writeSequenceToStringFunction(NodeList sequenceElements) throws IOException, XPathExpressionException {
        write("public String toString() {");

        StringBuilder sb = new StringBuilder();

        sb.append("return \"sequence: {\"");

        for (int j = 0; j < sequenceElements.getLength(); j++) {

            sb.append("+ \"");

            if (j != 0) {
                sb.append(", ");
            }

            Element sequenceElement = (Element) sequenceElements.item(j);

            sb.append(getSequenceElementName(sequenceElement) + ": \" + " + getSequenceElementName(sequenceElement)
                    + " ");
        }

        sb.append(" + \"}\";");

        write(sb.toString());

        write("}\n");

    }

    private void writeIntegerConstructor(String className, String min, String max) throws IOException {

        String range = "";
        String minString = "";
        if (!min.isEmpty() && !max.isEmpty()) {
            // Check if min and max are valid 32 bit signed integers
            long val = 0;
            try {
                val = Long.parseLong(min);
            } catch (NumberFormatException e) {
                // min has more than 64 bits. Truncate to Long.MIN_VALUE
                val = Long.MIN_VALUE;
                min = Long.toString(val);
            } finally {
                if (val < Integer.MIN_VALUE) {
                    min = min + "L";
                }
            }

            try {
                val = Long.parseLong(max);
            } catch (NumberFormatException e) {
                // max has more than 64 bits. Truncate to Long.MAX_VALUE
                val = Long.MAX_VALUE;
                max = Long.toString(val);
            } finally {
                if (val > Integer.MAX_VALUE) {
                    max = max + "L";
                }
            }

            range = "" + min + ", " + max + ", ";
            minString = "" + min;
        }

        write("public " + className + "() {");
        write("super(" + range + minString + ");");
        write("}\n");

        write("public " + className + "(byte[] code) {");
        write("super(" + range + minString + ");");
        write("this.code = code;");
        write("}\n");

        write("public " + className + "(long val) {");
        write("super(" + range + "val);");
        write("}\n");
    }

    private void writeEmptyConstructor(String className, boolean isChoice) throws IOException {
        write("public " + className + "() {");
        if (!isChoice) {
            write("id = identifier;");
        }
        write("}\n");

        write("public " + className + "(byte[] code) {");
        if (!isChoice) {
            write("id = identifier;");
        }
        write("this.code = code;");
        write("}\n");
    }

    private void writeEncodeConstructor(String className, NodeList sequenceElements, boolean isChoice)
            throws IOException, XPathExpressionException {
        StringBuilder line = new StringBuilder("public " + className + "(");

        // String line = "public " + className + "(";

        for (int j = 0; j < sequenceElements.getLength(); j++) {
            Element sequenceElement = (Element) sequenceElements.item(j);

            if (j != 0) {
                line.append(", ");
            }
            line.append(getElementType(sequenceElement, true))
                    .append(' ')
                    .append(getSequenceElementName(sequenceElement));

        }
        line.append(") {");
        write(line.toString());
        if (!isChoice) {
            write("id = identifier;");
        }

        for (int j = 0; j < sequenceElements.getLength(); j++) {
            Element sequenceElement = (Element) sequenceElements.item(j);

            if (isOptional(sequenceElement) || hasDefault(sequenceElement)) {
                write("this." + getSequenceElementName(sequenceElement) + ".setValue("
                        + getSequenceElementName(sequenceElement) + ");");
            }
            else {
                write("this." + getSequenceElementName(sequenceElement) + " = "
                        + getSequenceElementName(sequenceElement) + ";");
            }
        }

        write("}\n");
    }

    private String getASNType(Element element) throws XPathExpressionException, IOException {
        if (element == null) {
            throw new IOException("");
        }
        if (element.getAttribute("xsi:type").equals("asnElementType")
                || element.getAttribute("xsi:type").equals("asnDefinedType")
                || element.getAttribute("xsi:type").equals("asnTaggedType")) {
            if (!xPath.evaluate("typeReference/@type", element).equals("")) {
                return getASNType((Element) xPath.evaluate("typeReference", element, XPathConstants.NODE));
            }
            else {
                Element name = (Element) xPath.evaluate("*/name[.='" + xPath.evaluate("typeName", element) + "']",
                        asnTypesElement, XPathConstants.NODE);
                return getASNType((Element) name.getParentNode());
            }
        }
        else {
            if (element.getAttribute("xsi:type").equals("asnCharacterString")) {
                return "asn" + xPath.evaluate("stringtype", element);
            }
            return element.getAttribute("xsi:type");
        }

    }

    private String getElementType(Element seqElement, boolean appendName) throws XPathExpressionException, IOException {

        String asnType = xPath.evaluate("typeReference/@type", seqElement);
        if (asnType.equals("")) {
            return simplifyTypeName(xPath.evaluate("typeName", seqElement)).replace('-', '_');
        }
        else {

            if (asnType.equals("asnSequenceSet")) {

                String subClassName = "";
                if (xPath.evaluate("typeReference/isSequence", seqElement).equals("true")) {

                    subClassName += "SubSeq";
                }
                else {
                    subClassName += "SubSet";
                }

                if (appendName) {
                    subClassName += "_" + xPath.evaluate("name", seqElement);
                }

                return subClassName.replace('-', '_');

            }

            else if (asnType.equals("asnSequenceOf")) {
                String subClassName = "";
                if (xPath.evaluate("typeReference/isSequenceOf", seqElement).equals("true")) {

                    subClassName += "SubSeqOf";
                }
                else {
                    subClassName += "SubSetOf";
                }

                if (appendName) {
                    subClassName += "_" + xPath.evaluate("name", seqElement);
                }

                return subClassName.replace('-', '_');
            }
            else if (asnType.equals("asnChoice")) {

                String subClassName = "SubChoice";

                if (appendName) {
                    subClassName += "_" + xPath.evaluate("name", seqElement);
                }

                return subClassName.replace('-', '_');

            }

            return getAxdrType((Element) xPath.evaluate("typeReference", seqElement, XPathConstants.NODE));

        }
    }

    private String simplifyTypeName(String typeName) throws XPathExpressionException, IOException {
        Element name = (Element) xPath.evaluate("*/name[.='" + typeName + "']", asnTypesElement, XPathConstants.NODE);
        if (name == null) {
            throw new IOException("No asntype of name: " + typeName + " was found.");
        }

        Element newASNElement = (Element) name.getParentNode();
        String newASNType = newASNElement.getAttribute("xsi:type");
        if (newASNType.equals("asnSequenceSet") || newASNType.equals("asnSequenceOf") || newASNType.equals("asnChoice")
                || newASNType.equals("asnTaggedType")) {
            return typeName;
        }
        else {
            if (newASNType.equals("asnDefinedType")) {
                // return simplifyTypeName(xPath.evaluate("typeName", name.getParentNode()));
                return xPath.evaluate("typeName", newASNElement);
            }
            if (newASNType.equals("asnInteger") || newASNType.equals("asnBitString")
                    || newASNType.equals("asnOctetString")) {
                return xPath.evaluate("name", newASNElement);
            }

            return getAxdrType(newASNElement);

        }
    }

    private String getAxdrType(Element asnTypeElement) throws XPathExpressionException {
        String newASNType = asnTypeElement.getAttribute("xsi:type");
        if (newASNType.equals("asnCharacterString")) {
            return "Axdr" + xPath.evaluate("stringtype", asnTypeElement);
        }
        return "Axdr" + newASNType.substring(3);
    }

    private String getSequenceElementName(Element sequenceElement) throws XPathExpressionException {
        return xPath.evaluate("name", sequenceElement).replace('-', '_');
    }

    private boolean isOptional(Element sequenceElement) throws XPathExpressionException {
        if (xPath.evaluate("isOptional", sequenceElement).equals("true")) {
            return true;
        }
        return false;
    }

    private boolean hasDefault(Element sequenceElement) throws XPathExpressionException {
        if (xPath.evaluate("isDefault", sequenceElement).equals("true")) {
            return true;
        }
        return false;
    }

    private String getTagNum(Element sequenceElement) throws XPathExpressionException {
        String subTagNum = xPath.evaluate("tag/classNumber/num", sequenceElement);

        if (subTagNum.equals("")) {
            subTagNum = "0";
        }
        return subTagNum;
    }

    private String getTagClass(Element sequenceElement) throws XPathExpressionException {
        String subTagClass = xPath.evaluate("tag/clazz", sequenceElement);
        if (subTagClass.equals("")) {
            subTagClass = "CONTEXT";
        }

        subTagClass += "_CLASS";

        return subTagClass;
    }

    private void write(String line) throws IOException {
        if (line.startsWith("}")) {
            indentNum--;
        }
        for (int i = 0; i < indentNum; i++) {
            out.write("\t");
        }
        out.write(line + "\n");
        if (line.endsWith(" {") || line.endsWith(" {\n") || line.endsWith(" {\n\n")) {
            indentNum++;
        }
    }

    private boolean isExplicitlyTagged(Element sequenceElement) throws XPathExpressionException {
        return (!xPath.evaluate("tagDefault", sequenceElement).equals("IMPLICIT")) && defaultExplicit;
    }
}
