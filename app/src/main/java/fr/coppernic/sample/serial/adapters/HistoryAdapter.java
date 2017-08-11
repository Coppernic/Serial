package fr.coppernic.sample.serial.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.HashSet;

/**
 * <p>Created on 11/08/17
 *
 * @author bastien
 */
public class HistoryAdapter extends ArrayAdapter<String> {

    public static final int MAX_HISTORY = 40;

    private HashSet<String> mHistory = new HashSet<>();

    public HistoryAdapter(Context context, int textViewResourceId) {

        super(context, textViewResourceId);
    }

    public void insert(String s) {

        int count = getCount();

        if (MAX_HISTORY <= count) {
            String item = getItem(count - 1);
            remove(item);
            mHistory.remove(item);
        }

        if (mHistory.add(s)) {
            insert(s, 0);
            notifyDataSetChanged();
        }
    }

    @Override
    public void clear() {

        super.clear();
        mHistory.clear();
    }

}
