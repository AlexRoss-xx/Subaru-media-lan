/*
 *  2023.
 * Alexey Rasskazov
 */
package cz.jaybee.intelhex;

import java.util.Arrays;

/**
 *
 * @author Jan Breuer
 * @license BSD 2-Clause 
 */
public class IntelHexParserRun implements IntelHexDataListener {

    private long addressStart;
    private long addressStop;
    private long length;
    private long totalLength;
    private byte[] buffer;
    private boolean eofDone = false;

    public IntelHexParserRun(long addressStart, long addressStop) {
        this.addressStart = addressStart;
        this.addressStop = addressStop;
        this.length = (addressStop - addressStart + 1);
        this.totalLength = 0;
        this.buffer = new byte[(int) length];
        Arrays.fill(buffer, (byte) 0xFF);
        eofDone = false;
    }

    public void getBufData(byte[] buf){
        int copyLen = (int)length;
        if(copyLen > buf.length) {
            copyLen = buf.length;
        }
        System.arraycopy(buffer, 0, buf, 0, copyLen);
    }

    public long getTotalBufLength() {
        return totalLength;
    }

    public boolean isEOF() {
        return eofDone;
    }

    @Override
    public void data(long address, byte[] data) {
        if ((address >= addressStart) && (address <= addressStop)) {
            int length = data.length;
            if ((address + length) > addressStop) {
                length = (int) (addressStop - address + 1);
            }
            System.arraycopy(data, 0, buffer, (int) (address - addressStart), length);
            totalLength += length;
        }
    }

    @Override
    public void eof() {
        eofDone = true;
    }
}
