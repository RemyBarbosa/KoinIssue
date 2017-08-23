package fr.radiofrance.alarm.util;

import android.view.View;

import java.lang.ref.WeakReference;

public abstract class WeakRefOnClickListener<T> implements View.OnClickListener {

    private final WeakReference<T> weakReference;

    protected WeakRefOnClickListener(final T reference) {
        weakReference = new WeakReference<>(reference);
    }

    @Override
    public void onClick(final View view) {
        final T reference = weakReference.get();
        if (reference == null) {
            return;
        }
        onClick(reference, view);
    }

    public abstract void onClick(final T reference, final View view);
}
