package fr.coppernic.sample.serial.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>Created on 10/08/17
 *
 * @author bastien
 */
public class BoundaryArrayAdapter<T> extends ArrayAdapter<T> {

    private final List<T> objs = new ArrayList<>();
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
        objs.add(object);
        super.add(object);
    }

    @Override
    public void addAll(@NonNull Collection<? extends T> collection) {
        super.addAll(collection);
        objs.addAll(collection);
        while (getCount() > nbMaxItem && nbMaxItem != -1) {
            this.remove(this.getItem(0));
        }
    }

    @SafeVarargs
    @Override
    public final void addAll(@SuppressWarnings("unchecked") T... items) {
        super.addAll(items);
        Collections.addAll(objs, items);
        while (getCount() > nbMaxItem && nbMaxItem != -1) {
            this.remove(this.getItem(0));
        }
    }

    @Override
    public void insert(@Nullable T object, int index) {
        super.insert(object, index);
        objs.add(index, object);
    }

    @Override
    public void remove(@Nullable T object) {
        super.remove(object);
        objs.remove(object);
    }

    @Override
    public void sort(@NonNull Comparator<? super T> comparator) {
        Collections.sort(objs, comparator);
        super.sort(comparator);
    }

    @Override
    public void clear() {
        objs.clear();
        super.clear();
    }

    public List<T> getItemList() {
        return new ArrayList<>(objs);
    }

    public Set<T> getItemSet() {
        return new TreeSet<>(getItemList());
    }
}
