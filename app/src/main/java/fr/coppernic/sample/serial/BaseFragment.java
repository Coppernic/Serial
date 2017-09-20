package fr.coppernic.sample.serial;

import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import fr.coppernic.sdk.utils.debug.L;

/**
 * <p>Created on 10/08/17
 *
 * @author bastien
 */
public abstract class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        L.m(TAG, DEBUG);
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
                onMenuClear();
                return true;
            case R.id.menu_opt:
                onMenuOpt();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    abstract void onMenuClear();

    abstract void onMenuOpt();
}
