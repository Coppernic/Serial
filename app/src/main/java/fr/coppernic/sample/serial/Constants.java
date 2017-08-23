package fr.coppernic.sample.serial;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Created on 11/08/17
 *
 * @author bastien
 */
public class Constants {

    public static final int VID_FTDI = 1027;
    public static final int PID_FTDI_FT232R = 24577;
    public static final int PID_FTDI_FT231X = 24597;
    public static final int PID_FTDI_GSM_R = 25906;

    public static final List<Integer> PIDS_FTDI = Arrays.asList(PID_FTDI_FT232R, PID_FTDI_FT231X, PID_FTDI_GSM_R);

    private Constants() {
    }
}
