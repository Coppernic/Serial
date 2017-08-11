package fr.coppernic.sample.serial;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import java.util.TreeSet;

import fr.coppernic.sample.serial.adapters.BoundaryArrayAdapter;
import fr.coppernic.sample.serial.adapters.HistoryAdapter;
import fr.coppernic.sdk.serial.SerialCom;
import fr.coppernic.sdk.serial.SerialFactory;
import fr.coppernic.sdk.serial.utils.DataListener;
import fr.coppernic.sdk.serial.utils.SerialThreadListener;
import fr.coppernic.sdk.utils.debug.L;
import fr.coppernic.sdk.utils.io.InstanceListener;
import fr.coppernic.sdk.utils.ui.AdapterAppender;
import fr.coppernic.sdk.utils.ui.UiHandler;


/**
 * A simple {@link Fragment} subclass.
 */
public class TerminalFragment extends BaseFragment {

    public static final String TAG = "TerminalFragment";
    private static final boolean DEBUG = true;

    private Spinner spDevices = null;
    private Spinner spBaudRates = null;
    private Button btnOpenClose = null;
    private SerialCom serial = null;
    private ListView listView = null;
    private BoundaryArrayAdapter<String> mLogAdapter = null;
    private SharedPreferences mPrefs = null;
    private String[] mDeviceList = null;
    private SerialThreadListener threadListener = null;
    private UiHandler uiHandler = new UiHandler();
    private AutoCompleteTextView edtCmd = null;
    private HistoryAdapter mHistoryAdapter = null;


    private final InstanceListener<SerialCom> instanceListener = new InstanceListener<SerialCom>() {
        @Override
        public void onCreated(SerialCom serialCom) {
            serial = serialCom;
            initUi();
            enable(true);
        }

        @Override
        public void onDisposed(SerialCom serialCom) {
            if(serial == serialCom){
                serial = null;
                enable(false);
            }
        }
    };
    private final DataListener dataListener = new DataListener() {
        @Override
        public void onDataReceived(byte[] bytes) {
            try {
                final String data = new String(bytes, "UTF-8");
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        addTextInList(data);
                    }
                });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };
    private TextView.OnEditorActionListener actionListener = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if (v.getText().length() == 0) {
                return false;
            }
            //displayCmd(v.getText());
            send(v.getText());
            addLineInHistory(v.getText());
            handleLog(v.getText().toString());
            v.setText("");
            return true;
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

        mLogAdapter = new BoundaryArrayAdapter<>(getContext(), R.layout.list_dropdown_item);
        listView.setAdapter(mLogAdapter);

        edtCmd = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);
        edtCmd.setOnEditorActionListener(actionListener);
        edtCmd.setAdapter(getHistoryAdapter());

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Waiting for serial com to be ready before enabling open button
        enable(false);
        setUp();

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,  MenuInflater inflater) {
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

        mLogAdapter.setMaxItem(Integer.parseInt(mPrefs.getString("pref_nb_item_log", "1000")));
    }

    @Override
    public void onDestroy() {
        close();
        super.onDestroy();
    }

    private void clear(){
        L.m(TAG, DEBUG);
        mLogAdapter.clear();
        mLogAdapter.notifyDataSetChanged();
    }

    private void openClose(){
        if(serial == null){
            enable(false);
        } else if(serial.isOpened()) {
            close();
        } else {
            open();
        }

    }

    private void enable(boolean b){
        btnOpenClose.setEnabled(b);
    }

    private void setUp(){
        SerialFactory.getDirectInstance(getActivity(),instanceListener);
    }

    private void initUi(){
        loadDevices();
        listDevices();
    }

    private void addTextInList(String s){
        mLogAdapter.add(s);
        mLogAdapter.notifyDataSetChanged();
        listView.setSelection(mLogAdapter.getCount() - 1);
    }

    private void handleLog(String log) {
        uiHandler.post(new AdapterAppender(mLogAdapter, log));
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(mLogAdapter.getCount() - 1);
            }
        });
    }

    private void loadDevices() {
        mDeviceList = serial.listDevices();
    }

    private void listDevices() {

        Log.v(TAG, "List devices");
        TreeSet<String> mySet = new TreeSet<>();

        if (mDeviceList != null) {

            for (String device: mDeviceList) {
                mySet.add(device.trim());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity().getApplicationContext(),
                R.layout.spinner_item,
                new ArrayList<>(mySet.descendingSet()));
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

            spDevices.setAdapter(adapter);
        }
    }

    private void open(){
        String device = spDevices.getSelectedItem().toString();
        int bdt = Integer.parseInt(spBaudRates.getSelectedItem().toString());
        if(serial.open(device, bdt) != 0){
            Toast.makeText(getContext(), "Open fail !", Toast.LENGTH_SHORT).show();
        } else {
            btnOpenClose.setText(R.string.close);
            threadListener = new SerialThreadListener(serial, dataListener);
            threadListener.start();
        }
    }

    private void close(){
        if(serial != null){
            serial.close();
        }
        if(threadListener != null){
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

        StringBuilder sb = new StringBuilder(cmd);
        byte[] tx;

        tx = sb.toString().getBytes();

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

}
