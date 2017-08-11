package fr.coppernic.sample.serial.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import java.util.Collection;

/**
 * <p>Created on 10/08/17
 *
 * @author bastien
 */
public class BoundaryArrayAdapter<T> extends ArrayAdapter<T> {

    private int nbMaxItem = -1;

    public BoundaryArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void setMaxItem(int maxItem) {
        nbMaxItem = maxItem;
    }

    @Override
    public void add(T object) {
        if (this.getCount() >= nbMaxItem && nbMaxItem != -1) {
            this.remove(this.getItem(0));
        }
        super.add(object);
    }

    @Override
    public void addAll(@NonNull Collection<? extends T> collection) {
        super.addAll(collection);
        while (getCount() > nbMaxItem && nbMaxItem != -1) {
            this.remove(this.getItem(0));
        }
    }

    @SafeVarargs
    @Override
    public final void addAll(@SuppressWarnings("unchecked") T... items) {
        super.addAll(items);
        while (getCount() > nbMaxItem && nbMaxItem != -1) {
            this.remove(this.getItem(0));
        }
    }

}
