package org.openmuc.jdlms.datatypes;

abstract class CommonDateFormat implements CosemDateFormat {

    @Override
    public long asUnixTimeStanp() {
        return toCalendar().getTimeInMillis();
    }

}
