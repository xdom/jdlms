/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.axdr.compiler.someexamples.generated;

import java.io.IOException;
import java.io.InputStream;
import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.*;
import org.openmuc.jdlms.internal.asn1.axdr.types.*;

public class My_Choice2 implements AxdrType {

	public byte[] code = null;

	public static enum Choices {
		_ERR_NONE_SELECTED(-1),
		MYINT(1),
		MYBOOLEAN(24),
		;
		
		private int value;

		private Choices(int value) {
			this.value = value;
		}

		public int getValue() { return this.value; }

		public static Choices valueOf(long tagValue) {
			Choices[] values = Choices.values();

			for (Choices c : values) {
				if (c.value == tagValue) { return c; }
			}
			return _ERR_NONE_SELECTED;
		}
	}

	private Choices choice;

	public AxdrInteger myint = null;

	public AxdrBoolean myboolean = null;

	public My_Choice2() {
	}

	public My_Choice2(byte[] code) {
		this.code = code;
	}

	public int encode(BerByteArrayOutputStream axdrOStream) throws IOException {
		if (code != null) {
			for (int i = code.length - 1; i >= 0; i--) {
				axdrOStream.write(code[i]);
			}
			return code.length;

		}
		if (choice == Choices._ERR_NONE_SELECTED) {
			throw new IOException("Error encoding AxdrChoice: No item in choice was selected.");
		}

		int codeLength = 0;

		if (choice == Choices.MYBOOLEAN) {
			codeLength += myboolean.encode(axdrOStream);
			AxdrEnum c = new AxdrEnum(24);
			codeLength += c.encode(axdrOStream);
			return codeLength;
		}

		if (choice == Choices.MYINT) {
			codeLength += myint.encode(axdrOStream);
			AxdrEnum c = new AxdrEnum(1);
			codeLength += c.encode(axdrOStream);
			return codeLength;
		}

		// This block should be unreachable
		throw new IOException("Error encoding AxdrChoice: No item in choice was encoded.");
	}

	public int decode(InputStream iStream) throws IOException {
		int codeLength = 0;
		AxdrEnum choosen = new AxdrEnum();

		codeLength += choosen.decode(iStream);
		resetChoices();
		this.choice = Choices.valueOf(choosen.getValue());

		if (choice == Choices.MYINT) {
			myint = new AxdrInteger();
			codeLength += myint.decode(iStream);
			return codeLength;
		}

		if (choice == Choices.MYBOOLEAN) {
			myboolean = new AxdrBoolean();
			codeLength += myboolean.decode(iStream);
			return codeLength;
		}

		throw new IOException("Error decoding AxdrChoice: Identifier matched to no item.");
	}

	public void encodeAndSave(int encodingSizeGuess) throws IOException {
		BerByteArrayOutputStream axdrOStream = new BerByteArrayOutputStream(encodingSizeGuess);
		encode(axdrOStream);
		code = axdrOStream.getArray();
	}
	public Choices getChoiceIndex() {
		return this.choice;
	}

	public void setmyint(AxdrInteger newVal) {
		resetChoices();
		choice = Choices.MYINT;
		myint = newVal;
	}

	public void setmyboolean(AxdrBoolean newVal) {
		resetChoices();
		choice = Choices.MYBOOLEAN;
		myboolean = newVal;
	}

	private void resetChoices() {
		choice = Choices._ERR_NONE_SELECTED;
		myint = null;
		myboolean = null;
	}

	public String toString() {
		if (choice == Choices.MYINT) {
			return "choice: {myint: " + myint + "}";
		}

		if (choice == Choices.MYBOOLEAN) {
			return "choice: {myboolean: " + myboolean + "}";
		}

		return "unknown";
	}

}
