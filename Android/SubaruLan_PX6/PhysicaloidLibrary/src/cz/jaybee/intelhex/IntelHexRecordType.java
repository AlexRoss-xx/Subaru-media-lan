/*
 *  2023.
 * Alexey Rasskazov
 */
package cz.jaybee.intelhex;

/**
 *
 * @author Jan Breuer
 * @license BSD 2-Clause
 */
public enum IntelHexRecordType {

    DATA(0x00),
    EOF(0x01),
    EXT_SEG(0x02),
    START_SEG(0x03),
    EXT_LIN(0x04),
    START_LIN(0x05),
    UNKNOWN(0xFF);
    int id;

    IntelHexRecordType(int id) {
        this.id = id;
    }

    public int toInt() {
        return id;
    }

    public static IntelHexRecordType fromInt(int id) {
        for (IntelHexRecordType d : IntelHexRecordType.values()) {
            if (d.id == id) {
                return d;
            }
        }
        return IntelHexRecordType.UNKNOWN;
    }
}
