/*
 *  2023.
 * Alexey Rasskazov
 */

package com.physicaloid.lib.framework;

import java.io.InputStream;

import com.physicaloid.lib.Boards;
import com.physicaloid.lib.Physicaloid.UploadCallBack;
import com.physicaloid.lib.fpga.PhysicaloidFpgaConfigurator;
import com.physicaloid.lib.programmer.avr.AvrUploader;
import com.physicaloid.lib.programmer.avr.UploadErrors;
import com.physicaloid.lib.usb.driver.uart.UartConfig;

public class Uploader {
    public Uploader() {}
    public boolean upload(InputStream fileStream, Boards board, SerialCommunicator comm, UploadCallBack callback) {
        boolean ret = false;

        if(callback != null) {callback.onPreUpload(); }

        if(board == null) {
            if(callback != null) {
                callback.onError(UploadErrors.AVR_CHIPTYPE);
            }
            return false;
        }

        if(     board.uploadProtocol == Boards.UploadProtocols.STK500 ||
                board.uploadProtocol == Boards.UploadProtocols.STK500V2) {

            AvrUploader avrUploader = new AvrUploader(comm);

            comm.setUartConfig(new UartConfig(board.uploadBaudrate, UartConfig.DATA_BITS8, UartConfig.STOP_BITS1, UartConfig.PARITY_NONE, false, false));

            ret = avrUploader.run(fileStream, board, callback);

        } else if(board.uploadProtocol == Boards.UploadProtocols.ALTERA_FPGA_RBF) {
            ret = new PhysicaloidFpgaConfigurator(comm).configuration(fileStream);
        }

        if(callback != null) {callback.onPostUpload(ret); }

        return ret;
    }

}
