package fr.coppernic.sample.serial;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SerialActivity extends AppCompatActivity {

    private ActivityListener listener;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        displayFragment(getTerminalFragment(), TerminalFragment.TAG);
    }

    public void registerActivityListener(ActivityListener listener){
        this.listener = listener;
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

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.content, f, tag)
            .commit();
    }


    public interface ActivityListener{
        boolean onMenuItemSelected(MenuItem item);
    }
}
