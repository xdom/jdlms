/*
 * Copyright Fraunhofer ISE, 2012
 * Author(s): Karsten Müller-Bier
 * 
 * This file is part of jASN1.
 * For more information visit http://www.openmuc.org
 * 
 * jDlms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * jDlms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with jDlms.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.openmuc.jdlms.internal.asn1.axdr.types;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrLength;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;

public abstract class AxdrSequenceOf<E extends AxdrType> implements AxdrType {

    public byte[] code = null;
    private Integer length = null;
    private List<E> seqOf;

    public AxdrSequenceOf() {
        seqOf = new LinkedList<>();
    }

    public AxdrSequenceOf(byte[] code) {
        this.code = code;
        seqOf = new LinkedList<>();
    }

    public AxdrSequenceOf(List<E> seqOf) {
        this.seqOf = seqOf;
    }

    protected AxdrSequenceOf(int length) {
        this.length = length;
        this.seqOf = new ArrayList<>(length);
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
            if (length != null && length != seqOf.size()) {
                throw new IOException("Error decoding AxdrSequenceOf: Size of elements does not match.");
            }

            codeLength = 0;
            for (int i = (seqOf.size() - 1); i >= 0; i--) {
                codeLength += seqOf.get(i).encode(axdrOStream);
            }

            if (length == null) {
                codeLength += AxdrLength.encodeLength(axdrOStream, seqOf.size());
            }

        }

        return codeLength;
    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;
        int numElements = this.length == null ? 0 : this.length;

        if (numElements == 0) {
            AxdrLength length = new AxdrLength();
            codeLength += length.decode(iStream);

            numElements = length.getValue();
        }

        seqOf = new LinkedList<>();

        for (int i = 0; i < numElements; i++) {
            E subElem = createListElement();
            codeLength += subElem.decode(iStream);
            seqOf.add(subElem);
        }

        return codeLength;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        BerByteArrayOutputStream axdrOStream = new BerByteArrayOutputStream(encodingSizeGuess);
        encode(axdrOStream);
        code = axdrOStream.getArray();
    }

    public void add(E element) {
        if (length != null && seqOf.size() == length) {
            throw new IndexOutOfBoundsException();
        }
        seqOf.add(element);
    }

    public E get(int index) {
        return seqOf.get(index);
    }

    public int size() {
        return seqOf.size();
    }

    public Iterator<E> iterator() {
        return seqOf.iterator();
    }

    public List<E> list() {
        return Collections.unmodifiableList(seqOf);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SEQUENCE OF{");

        if (seqOf == null) {
            sb.append("null");
        }
        else {
            Iterator<E> it = seqOf.iterator();
            if (it.hasNext()) {
                sb.append(it.next());
                while (it.hasNext()) {
                    sb.append(", ").append(it.next());
                }
            }
        }

        sb.append('}');

        return sb.toString();
    }

    protected abstract E createListElement();
}
