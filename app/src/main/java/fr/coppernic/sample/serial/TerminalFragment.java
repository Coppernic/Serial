package fr.coppernic.sample.serial;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import fr.coppernic.sample.serial.adapters.BoundaryArrayAdapter;
import fr.coppernic.sample.serial.adapters.HistoryAdapter;
import fr.coppernic.sdk.serial.SerialCom;
import fr.coppernic.sdk.serial.SerialFactory;
import fr.coppernic.sdk.serial.utils.DataListener;
import fr.coppernic.sdk.serial.utils.SerialThreadListener;
import fr.coppernic.sdk.utils.core.CpcBytes;
import fr.coppernic.sdk.utils.core.CpcString;
import fr.coppernic.sdk.utils.debug.L;
import fr.coppernic.sdk.utils.helpers.CpcUsb;
import fr.coppernic.sdk.utils.io.InstanceListener;
import fr.coppernic.sdk.utils.ui.AdapterAppender;
import fr.coppernic.sdk.utils.ui.UiHandler;


/**
 * A simple {@link Fragment} subclass.
 */
public class TerminalFragment extends BaseFragment {

    public static final String TAG = "TerminalFragment";
    private static final boolean DEBUG = true;
    private static final String KEY_BDT = "key_bdt";
    private static final String KEY_DEV = "key_dev";
    private static final String KEY_LOGS = "key_miam_miam";
    private final static String KEY_PREF_ASCII = "pref_ascii";
    private final static String KEY_PREF_W_LINE_ENDING = "rn"; // Windows
    private final static String KEY_PREF_U_LINE_ENDING = "n"; //Unix
    private final static String KEY_PREF_R_LINE_ENDING = "r"; //cr
    private final static String KEY_PREF_LINE_ENDING = "pref_line_ending";

    private Spinner spDevices = null;
    private Spinner spBaudRates = null;
    private Button btnOpenClose = null;
    private SerialCom direct = null;
    private SerialCom ftdi = null;
    private SerialCom serial = null;
    private ListView listView = null;
    private BoundaryArrayAdapter<String> logAdapter = null;
    private SharedPreferences mPrefs = null;
    private SerialThreadListener threadListener = null;
    private UiHandler uiHandler = new UiHandler();
    private final DataListener dataListener = new DataListener() {
        @Override
        public void onDataReceived(byte[] bytes) {
            try {

                if (isAscii()) {
                    handleDataStringReceived(new String(bytes, "UTF-8"));
                } else {
                    handleDataByteReceived(bytes);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };
    private AutoCompleteTextView edtCmd = null;
    private HistoryAdapter mHistoryAdapter = null;
    private UsbManager usbManager = null;
    private TreeSet<String> devSet = new TreeSet<>();
    private List<String> directList = new ArrayList<>();
    private List<String> ftdiList = new ArrayList<>();
    private final InstanceListener<SerialCom> directListener = new InstanceListener<SerialCom>() {
        @Override
        public void onCreated(SerialCom serialCom) {
            direct = serialCom;
            directList = Arrays.asList(direct.listDevices());
            listDevices();
        }

        @Override
        public void onDisposed(SerialCom serialCom) {
            if (direct == serialCom) {
                direct = null;
                directList.clear();
                listDevices();
            }
        }
    };
    private final InstanceListener<SerialCom> ftdiListener = new InstanceListener<SerialCom>() {
        @Override
        public void onCreated(SerialCom serialCom) {
            ftdi = serialCom;
            ftdiList = Arrays.asList(ftdi.listDevices());
            for (String s : ftdiList) {
                Log.d(TAG, s);
            }
            listDevices();
        }

        @Override
        public void onDisposed(SerialCom serialCom) {
            if (ftdi == serialCom) {
                ftdi = null;
                ftdiList.clear();
                listDevices();
            }
        }
    };
    private TextView.OnEditorActionListener actionListener = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if (v.getText().length() == 0) {
                return false;
            }
            send(v.getText());
            addLineInHistory(v.getText());
            handleDataSent(v.getText().toString());
            v.setText("");
            return true;
        }
    };
    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                SerialFactory.getFtdiInstance(getContext(), ftdiListener);
            }
        }
    };
    private InputFilter mHexaFilter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            if (source.length() == 0) {
                return null;
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = start; i < end; i++) {
                    if (Character.isDigit(source.charAt(i))
                        || Character.isWhitespace(source.charAt(i))
                        || isHexaLetter(source.charAt(i))) {
                        sb.append(source.charAt(i));
                    } else {
                        Log.w(TAG,
                              "Wrong character supplied : "
                              + source.charAt(i) + " at index "
                              + Integer.toString(i));
                    }
                }
                return sb.toString();
            }
        }

        private boolean isHexaLetter(char c) {
            return c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
        }
    };

    public TerminalFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_terminal, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        spDevices = (Spinner) view.findViewById(R.id.spDev);
        spBaudRates = (Spinner) view.findViewById(R.id.spBdt);
        btnOpenClose = (Button) view.findViewById(R.id.btnOpenClose);
        btnOpenClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openClose();
            }
        });
        listView = (ListView) view.findViewById(R.id.listview1);

        logAdapter = new BoundaryArrayAdapter<>(getContext(), R.layout.list_dropdown_item);
        listView.setAdapter(logAdapter);
        logAdapter.addAll(mPrefs.getStringSet(KEY_LOGS, Collections.<String>emptySet()));


        edtCmd = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);
        edtCmd.setOnEditorActionListener(actionListener);
        edtCmd.setAdapter(getHistoryAdapter());

        usbManager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);

        enable(false);
        setUp();

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        switch (id) {
            case R.id.menu_clear:
                clear();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        CpcUsb.registerUsbReceiver(getContext(), usbReceiver);
        logAdapter.setMaxItem(Integer.parseInt(mPrefs.getString("pref_nb_item_log", "1000")));

        if (isAscii()) {
            edtCmd.setFilters(new InputFilter[]{});
        } else {
            edtCmd.setFilters(new InputFilter[]{mHexaFilter});
        }
    }

    @Override
    public void onStop() {
        saveState();
        getContext().unregisterReceiver(usbReceiver);
        close();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        L.m(TAG, DEBUG);
        saveState();
        super.onSaveInstanceState(outState);
    }

    private void saveState() {
        L.m(TAG, DEBUG);
        mPrefs.edit()
            .putInt(KEY_BDT, spBaudRates.getSelectedItemPosition())
            .putInt(KEY_DEV, spDevices.getSelectedItemPosition())
            .putStringSet(KEY_LOGS, logAdapter.getItemSet())
            .apply();
    }

    private void clear() {
        L.m(TAG, DEBUG);
        logAdapter.clear();
        logAdapter.notifyDataSetChanged();
        saveState();
    }

    private void openClose() {
        if (serial != null && serial.isOpened()) {
            close();
        } else {
            open();
        }

    }

    private void enable(boolean b) {
        edtCmd.setEnabled(b);
        if (!b) {
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        Utils.hideKeyboard(getActivity());
                    }
                }
            }, 1000);
        }
    }

    private void setUp() {
        SerialFactory.getDirectInstance(getActivity(), directListener);

        CpcUsb.displayUsbDeviceList(getContext());
        ArrayList<UsbDevice> devList = CpcUsb.getVidDevicesList(getContext(), Constants.VID_FTDI);

        for (UsbDevice dev : devList) {
            int pid = dev.getProductId();

            if (Constants.PIDS_FTDI.contains(dev.getProductId())) {
                if (!usbManager.hasPermission(dev)) {
                    CpcUsb.getUsbAuthorization(getContext(), pid, dev.getVendorId(), null, 10000);
                } else {
                    SerialFactory.getFtdiInstance(getContext(), ftdiListener);
                }
            }
        }
    }

    private void handleDataByteReceived(byte[] data) {
        StringBuilder sb = new StringBuilder(">> ");
        for (int i = 0; i < data.length; i++) {
            sb.append(String.format("%02x ", data[i]));
        }
        handleLog(sb.toString());
    }

    private void handleDataStringReceived(String s) {
        StringBuilder sb = new StringBuilder(">> ");
        if (s.endsWith("\r\n")) {
            sb.append(s.substring(0, s.length() - 2));
            sb.append("[CR][LF]");
        } else if (s.endsWith("\n")) {
            sb.append(s.substring(0, s.length() - 1));
            sb.append("[LF]");
        } else if (s.endsWith("\r")) {
            sb.append(s.substring(0, s.length() - 1));
            sb.append("[CR]");
        } else {
            sb.append(s);
        }
        handleLog(sb.toString());
    }

    private void handleDataSent(String s) {
        handleLog("<< " + s);
    }

    private void handleLog(String log) {
        if (DEBUG) {
            Log.d(TAG, log);
        }
        uiHandler.post(new AdapterAppender(logAdapter, log));
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(logAdapter.getCount() - 1);
            }
        });
    }

    private void listDevices() {
        Log.v(TAG, "List devices");
        devSet = new TreeSet<>();
        devSet.addAll(directList);
        devSet.addAll(ftdiList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getActivity().getApplicationContext(),
            R.layout.spinner_item,
            new ArrayList<>(devSet.descendingSet()));
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spDevices.setAdapter(adapter);
        updateSpinners();
    }

    private void updateSpinners() {
        int devPos = mPrefs.getInt(KEY_DEV, 0);
        int bdtPos = mPrefs.getInt(KEY_BDT, 0);

        spDevices.setSelection(devPos);
        spBaudRates.setSelection(bdtPos);
    }

    private void open() {
        String device = spDevices.getSelectedItem().toString();

        if (direct != null && CpcString.isStringInArray(direct.listDevices(), device)) {
            serial = direct;
        } else if (ftdi != null && CpcString.isStringInArray(ftdi.listDevices(), device)) {
            serial = ftdi;
        } else {
            Log.e(TAG, "No Serial device suitable to open");
            return;
        }

        int bdt = Integer.parseInt(spBaudRates.getSelectedItem().toString());
        //TODO consider doing an open in an other thread
        if (serial.open(device, bdt) != 0) {
            Toast.makeText(getContext(), "Open fail !", Toast.LENGTH_SHORT).show();
        } else {
            btnOpenClose.setText(R.string.close);
            serial.flush();
            threadListener = new SerialThreadListener(serial, dataListener);
            threadListener.start();
            enable(true);
        }
    }

    private void close() {

        enable(false);
        if (serial != null) {
            serial.close();
        }
        if (threadListener != null) {
            threadListener.stop();
        }
        btnOpenClose.setText(R.string.open);
    }

    private void send(CharSequence cmd) {

        if (serial == null || !serial.isOpened()) {
            return;
        }

        if (cmd.length() == 0) {
            return;
        }

        if (isAscii()) {
            sendAscii(cmd);
        } else {
            sendBytes(cmd);
        }
    }

    private void sendAscii(CharSequence cmd) {
        StringBuilder sb = new StringBuilder(cmd);
        switch (mPrefs.getString(KEY_PREF_LINE_ENDING, "none")) {
            case KEY_PREF_W_LINE_ENDING:
                Log.v(TAG, "KEY_PREF_W_LINE_ENDING");
                sb.append("\r\n");
                break;
            case KEY_PREF_U_LINE_ENDING:
                Log.v(TAG, "KEY_PREF_U_LINE_ENDING");
                sb.append("\n");
                break;
            case KEY_PREF_R_LINE_ENDING:
                Log.v(TAG, "KEY_PREF_R_LINE_ENDING");
                sb.append("\r");
                break;
        }

        byte[] tx = sb.toString().getBytes();
        serial.send(tx, tx.length);
    }

    private void sendBytes(CharSequence cmd) {

        String[] strByte = cmd.toString().split(" ");
        List<byte[]> byteList = new ArrayList<>();

        for (String s : strByte) {
            try {
                byteList.add(CpcBytes.parseHexStringToArray(s));
            } catch (NumberFormatException e) {
                Log.e(TAG, e.toString());
            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
        }
        byte[] tx = CpcBytes.contactByteArrays(byteList);
        serial.send(tx, tx.length);
    }

    private ArrayAdapter<String> getHistoryAdapter() {

        if (mHistoryAdapter == null) {
            mHistoryAdapter = new HistoryAdapter(getContext(),
                                                 android.R.layout.simple_dropdown_item_1line);
        }
        return mHistoryAdapter;
    }

    private void addLineInHistory(CharSequence s) {

        String sb = s.toString();
        mHistoryAdapter.insert(sb);
    }

    private boolean isAscii() {
        return mPrefs.getBoolean(KEY_PREF_ASCII, false);
    }

}
