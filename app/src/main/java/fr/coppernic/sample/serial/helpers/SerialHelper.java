package fr.coppernic.sample.serial.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import fr.coppernic.sample.serial.Constants;
import fr.coppernic.sdk.serial.SerialCom;
import fr.coppernic.sdk.serial.SerialFactory;
import fr.coppernic.sdk.utils.core.CpcString;
import fr.coppernic.sdk.utils.helpers.CpcUsb;
import fr.coppernic.sdk.utils.io.InstanceListener;

/**
 * <p>Created on 23/08/17
 *
 * @author bastien
 */
public class SerialHelper {
    private static final String TAG = "SerialHelper";

    private final Context context;

    private SerialCom direct = null;
    private SerialCom ftdi = null;

    private UsbManager usbManager = null;
    private TreeSet<String> devSet = new TreeSet<>();
    private List<String> directList = new ArrayList<>();
    private List<String> ftdiList = new ArrayList<>();

    private GetDeviceListCallBack getDeviceListCallBack;
    private final InstanceListener<SerialCom> ftdiListener = new InstanceListener<SerialCom>() {
        @Override
        public void onCreated(SerialCom serialCom) {
            ftdi = serialCom;
            ftdiList = Arrays.asList(ftdi.listDevices());
            for (String s : ftdiList) {
                Log.d(TAG, s);
            }
            getDeviceList(getDeviceListCallBack);
        }

        @Override
        public void onDisposed(SerialCom serialCom) {
            if (ftdi == serialCom) {
                ftdi = null;
                ftdiList.clear();
            }
        }
    };
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                SerialFactory.getFtdiInstance(context, ftdiListener);
            }
            try {
                SerialHelper.this.context.unregisterReceiver(usbReceiver);
            } catch (Exception ignore) {

            }
        }
    };
    private final InstanceListener<SerialCom> directListener = new InstanceListener<SerialCom>() {
        @Override
        public void onCreated(SerialCom serialCom) {
            direct = serialCom;
            directList = Arrays.asList(direct.listDevices());
            getDeviceList(getDeviceListCallBack);
        }

        @Override
        public void onDisposed(SerialCom serialCom) {
            if (direct == serialCom) {
                direct = null;
                directList.clear();
            }
        }
    };

    public SerialHelper(Context context) {
        this.context = context;
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    public void getDeviceList(@NonNull GetDeviceListCallBack callBack) {
        getDeviceListCallBack = callBack;

        if (direct == null) {
            SerialFactory.getDirectInstance(context, directListener);
        } else if (ftdi == null) {
            ArrayList<UsbDevice> devList = CpcUsb.getVidDevicesList(context, Constants.VID_FTDI);
            for (UsbDevice dev : devList) {
                int pid = dev.getProductId();

                if (Constants.PIDS_FTDI.contains(dev.getProductId())) {
                    if (!usbManager.hasPermission(dev)) {
                        CpcUsb.registerUsbReceiver(context, usbReceiver);
                        CpcUsb.getUsbAuthorization(context, pid, dev.getVendorId(), null, 10000);
                    } else {
                        SerialFactory.getFtdiInstance(context, ftdiListener);
                    }
                }
            }
        } else {
            listDevices();
        }
    }

    public SerialCom getSerialFromPort(String port) {
        SerialCom ret = null;
        if (direct != null && CpcString.isStringInArray(direct.listDevices(), port)) {
            ret = direct;
        } else if (ftdi != null && CpcString.isStringInArray(ftdi.listDevices(), port)) {
            ret = ftdi;
        } else {
            Log.e(TAG, "No suitable device found");
        }
        return ret;
    }

    private void listDevices() {
        Log.v(TAG, "List devices");
        devSet = new TreeSet<>();
        devSet.addAll(directList);
        devSet.addAll(ftdiList);

        getDeviceListCallBack.onDeviceListReady(new ArrayList<>(devSet.descendingSet()));
    }

    public interface GetDeviceListCallBack {
        void onDeviceListReady(List<String> devList);
    }
}
