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
public interface IntelHexDataListener {

    public void data(long address, byte[] data);

    public void eof();
}
