package org.bn.compiler.parser.model;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//~--- classes ----------------------------------------------------------------

public class AsnCharacterStringValue {
    String cStr;
    public ArrayList charDefsList;
    public boolean isCharDefList;
    boolean isQuadruple;
    public boolean isTuple;
    public List<AsnSignedNumber> tupleQuad;

    // ~--- constructors -------------------------------------------------------

    // Default Constructor
    public AsnCharacterStringValue() {
        charDefsList = new ArrayList<>();
        tupleQuad = new ArrayList<>();
    }

    // ~--- methods ------------------------------------------------------------

    @Override
    public String toString() {
        String ts = "";

        if (isTuple || isQuadruple) {
            Iterator i = tupleQuad.iterator();

            while (i.hasNext()) {
                ts += i.next() + "\n";
            }
        }
        else if (isCharDefList) {
            Iterator i = charDefsList.iterator();

            while (i.hasNext()) {
                ts += i.next();
            }
        }

        return ts;
    }
}
