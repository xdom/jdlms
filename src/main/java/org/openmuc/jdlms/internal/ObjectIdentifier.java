package org.openmuc.jdlms.internal;

import org.openmuc.jasn1.ber.types.BerObjectIdentifier;
import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.internal.asn1.iso.acse.ApplicationContextName;
import org.openmuc.jdlms.internal.asn1.iso.acse.MechanismName;

public class ObjectIdentifier {
    private static final int JOINT_ISO_CCITT = 2;
    private static final int COUNTRY = 16;
    private static final int COUNTRY_NAME = 756;
    private static final int IDENTIFIED_ORGANIZATION = 5;
    private static final int DLMS_UA = 8;

    private enum ObjectIdentifierId {
        CONTEXT(1),
        MECHANISM_NAME(2);

        private int id;

        private ObjectIdentifierId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    private static final int INDEX_OF_ID = 6;

    public static MechanismName mechanismNameFrom(AuthenticationMechanism mechanismId) {
        return new MechanismName(identifierFrom(ObjectIdentifierId.MECHANISM_NAME, mechanismId.getId()));
    }

    public static ApplicationContextName applicationContextNameFrom(ContextId contextId) {
        return new ApplicationContextName(identifierFrom(ObjectIdentifierId.CONTEXT, contextId.getCode()));
    }

    public static ContextId applicationContextIdFrom(BerObjectIdentifier applicationContext) {
        return ContextId.contextIdFor(applicationContext.value[INDEX_OF_ID]);
    }

    private static int[] identifierFrom(ObjectIdentifierId objectIdentifierId, int id) {
        return new int[] { JOINT_ISO_CCITT, COUNTRY, COUNTRY_NAME, IDENTIFIED_ORGANIZATION, DLMS_UA,
                objectIdentifierId.getId(), id };
    }

    public static AuthenticationMechanism mechanismIdFrom(MechanismName mechanismName) {
        return AuthenticationMechanism.getInstance(mechanismName.value[INDEX_OF_ID]);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private ObjectIdentifier() {
    }

}
