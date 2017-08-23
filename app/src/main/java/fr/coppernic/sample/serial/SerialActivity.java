package fr.coppernic.sample.serial;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

public class SerialActivity extends AppCompatActivity {

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
        = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_terminal:
                    displayFragment(getTerminalFragment(), TerminalFragment.TAG);
                    return true;
                case R.id.navigation_transparent:
                    displayFragment(getTransparentFragment(), TransparentFragment.TAG);
                    return true;
                case R.id.navigation_config:
                    displayFragment(getConfigFragment(), ConfigFragment.TAG);
                    return true;
            }
            return false;
        }

    };
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_serial);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        displayFragment(getTerminalFragment(), TerminalFragment.TAG);
    }

    private Fragment getTerminalFragment() {
        TerminalFragment f = (TerminalFragment) getSupportFragmentManager()
            .findFragmentByTag(TerminalFragment.TAG);
        if (f == null) {
            f = new TerminalFragment();
        }
        return f;
    }

    private Fragment getTransparentFragment() {
        TransparentFragment f = (TransparentFragment) getSupportFragmentManager()
            .findFragmentByTag(TransparentFragment.TAG);
        if (f == null) {
            f = new TransparentFragment();
        }
        return f;
    }

    private Fragment getConfigFragment() {
        ConfigFragment f = (ConfigFragment) getSupportFragmentManager()
            .findFragmentByTag(ConfigFragment.TAG);
        if (f == null) {
            f = new ConfigFragment();
        }
        return f;
    }

    private void displayFragment(Fragment f, String tag) {
        Utils.hideKeyboard(this);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.content, f, tag)
            .commit();
    }

}
