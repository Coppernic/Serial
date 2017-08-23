package fr.coppernic.sample.serial;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.coppernic.sample.serial.adapters.BoundaryArrayAdapter;
import fr.coppernic.sample.serial.helpers.SerialHelper;
import fr.coppernic.sdk.serial.SerialCom;
import fr.coppernic.sdk.serial.utils.DataListener;
import fr.coppernic.sdk.serial.utils.SerialThreadListener;
import fr.coppernic.sdk.utils.core.CpcBytes;
import fr.coppernic.sdk.utils.debug.L;
import fr.coppernic.sdk.utils.ui.UiHandler;


/**
 * A simple {@link Fragment} subclass.
 */
public class TransparentFragment extends BaseFragment {

    public static final String TAG = "TransparentFragment";
    private static final String KEY_LOGS = "key_miam_transparent";
    private static final String KEY_BDT_LOCAL = "key_bdt_local";
    private static final String KEY_DEV_LOCAL = "key_dev_local";
    private static final String KEY_BDT_REMOTE = "key_bdt_remote";
    private static final String KEY_DEV_REMOTE = "key_dev_remote";
    private static final boolean DEBUG = true;
    private final UiHandler uiHandler = new UiHandler();
    @BindView(R.id.spinPortLocal)
    Spinner spDevicesLocal = null;
    @BindView(R.id.spinBdtLocal)
    Spinner spBaudRatesLocal = null;
    @BindView(R.id.spinPortRemote)
    Spinner spDevicesRemote = null;
    @BindView(R.id.spinBdtRemote)
    Spinner spBaudRatesRemote = null;
    @BindView(R.id.buttonOpenClose)
    Button btnOpenClose = null;
    @BindView(R.id.listView1)
    ListView listView = null;
    private SharedPreferences mPrefs = null;
    private BoundaryArrayAdapter<String> logAdapter = null;
    private SerialHelper helperLocal;
    private SerialHelper helperRemote;
    private SerialCom serialLocal = null;
    private final DataListener dataListenerRemote = new DataListener() {
        @Override
        public void onDataReceived(byte[] bytes) {
            //Log.d(TAG, "<< " + CpcBytes.byteArrayToAsciiString(bytes));
            synchronized (TransparentFragment.this) {
                if (serialLocal.isOpened()) {
                    serialLocal.send(bytes, bytes.length);
                    handleLog("<< ", bytes);
                } else {
                    Log.d(TAG, "Serial is not opened");
                }
            }
            //handleLog(bytes, R.color.colorRemote);
            //handleLog(bytes, "#FF0000");
        }
    };
    private SerialCom serialRemote = null;
    private final DataListener dataListenerLocal = new DataListener() {
        @Override
        public void onDataReceived(byte[] bytes) {
            //Log.d(TAG, ">> " + CpcBytes.byteArrayToAsciiString(bytes));
            synchronized (TransparentFragment.this) {
                if (serialRemote.isOpened()) {
                    serialRemote.send(bytes, bytes.length);
                    handleLog(">> ", bytes);
                } else {
                    Log.d(TAG, "Serial is not opened");
                }
            }
            //handleLog(bytes, R.color.colorLocal);
            //handleLog(bytes, "#00FF00");
        }
    };
    private SerialThreadListener threadListenerLocal = null;
    private SerialThreadListener threadListenerRemote = null;

    public TransparentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transparent, container, false);
        ButterKnife.bind(this, view);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        logAdapter = new BoundaryArrayAdapter<>(getContext(), R.layout.list_dropdown_item);
        listView.setAdapter(logAdapter);
        //logAdapter.addAll(mPrefs.getStringSet(KEY_LOGS, Collections.<String>emptySet()));

        return view;
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

        logAdapter.setMaxItem(Integer.parseInt(mPrefs.getString("pref_nb_item_log", "1000")));
        setUp();
    }

    @Override
    public void onStop() {
        saveState();
        close();
        super.onStop();
    }

    @OnClick(R.id.buttonOpenClose)
    void openClose() {
        if (serialLocal != null && serialLocal.isOpened()) {
            close();
        } else {
            open();
        }
    }

    private void saveState() {
        L.m(TAG, DEBUG);
        mPrefs.edit()
            .putInt(KEY_BDT_LOCAL, spBaudRatesLocal.getSelectedItemPosition())
            .putInt(KEY_DEV_LOCAL, spDevicesLocal.getSelectedItemPosition())
            .putInt(KEY_BDT_REMOTE, spBaudRatesRemote.getSelectedItemPosition())
            .putInt(KEY_DEV_REMOTE, spDevicesRemote.getSelectedItemPosition())
            //.putStringSet(KEY_LOGS, logAdapter.getItemSet())
            .apply();
    }

    private void clear() {
        L.m(TAG, DEBUG);
        logAdapter.clear();
        logAdapter.notifyDataSetChanged();
        saveState();
    }

    private void updateSpinners() {
        int devPosLocal = mPrefs.getInt(KEY_DEV_LOCAL, 0);
        int bdtPosLocal = mPrefs.getInt(KEY_BDT_LOCAL, 0);
        int devPosRemote = mPrefs.getInt(KEY_DEV_REMOTE, 0);
        int bdtPosRemote = mPrefs.getInt(KEY_BDT_REMOTE, 0);

        spDevicesLocal.setSelection(devPosLocal);
        spBaudRatesLocal.setSelection(bdtPosLocal);
        spDevicesRemote.setSelection(devPosRemote);
        spBaudRatesRemote.setSelection(bdtPosRemote);
    }

    private void setUp() {
        helperLocal = new SerialHelper(getContext());
        helperRemote = new SerialHelper(getContext());

        helperLocal.getDeviceList(new SerialHelper.GetDeviceListCallBack() {
            @Override
            public void onDeviceListReady(List<String> devList) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                                                  R.layout.spinner_item,
                                                                  devList);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spDevicesLocal.setAdapter(adapter);
                updateSpinners();
            }
        });
        helperRemote.getDeviceList(new SerialHelper.GetDeviceListCallBack() {
            @Override
            public void onDeviceListReady(List<String> devList) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                                                  R.layout.spinner_item,
                                                                  devList);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spDevicesRemote.setAdapter(adapter);
                updateSpinners();
            }
        });
    }

    private synchronized void open() {
        int bdtLocal = Integer.parseInt(spBaudRatesLocal.getSelectedItem().toString());
        String deviceLocal = spDevicesLocal.getSelectedItem().toString();
        serialLocal = helperLocal.getSerialFromPort(deviceLocal);

        int bdtRemote = Integer.parseInt(spBaudRatesRemote.getSelectedItem().toString());
        String deviceRemote = spDevicesRemote.getSelectedItem().toString();
        serialRemote = helperRemote.getSerialFromPort(deviceRemote);

        if (serialLocal.open(deviceLocal, bdtLocal) != 0
            || serialRemote.open(deviceRemote, bdtRemote) != 0) {
            Toast.makeText(getContext(), "Open fail !", Toast.LENGTH_SHORT).show();
            if (serialLocal.isOpened()) {
                serialLocal.close();
            }
            if (serialRemote.isOpened()) {
                serialRemote.close();
            }
        } else {
            btnOpenClose.setText(R.string.close);
            serialLocal.flush();
            serialRemote.flush();
            threadListenerLocal = new SerialThreadListener(serialLocal, dataListenerLocal);
            threadListenerLocal.start();
            threadListenerRemote = new SerialThreadListener(serialRemote, dataListenerRemote);
            threadListenerRemote.start();
        }
    }

    private synchronized void close() {
        if (serialLocal != null) {
            serialLocal.close();
        }
        if (serialRemote != null) {
            serialRemote.close();
        }
        if (threadListenerLocal != null) {
            threadListenerLocal.stop();
        }
        if (threadListenerRemote != null) {
            threadListenerRemote.stop();
        }
        btnOpenClose.setText(R.string.open);
    }

    private void handleLog(final String prefix, byte[] data) {
        final String log = prefix + CpcBytes.byteArrayToAsciiString(data);
        //ContextCompat.getColor(getContext(), colorId)
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) {
                    Log.d(TAG, log);
                }
                logAdapter.add(log);
                logAdapter.notifyDataSetChanged();
                listView.setSelection(logAdapter.getCount() - 1);
            }
        });
    }

}
