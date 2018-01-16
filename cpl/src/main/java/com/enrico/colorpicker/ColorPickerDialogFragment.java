package com.enrico.colorpicker;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

public class ColorPickerDialogFragment extends DialogFragment {

    private ColorPickedListener mListener;
    private int mColor;

    public static void showColorPicker(AppCompatActivity activity,
                                       ColorPickedListener listener, int color, String tag) {
        ColorPickerDialogFragment fragment = new ColorPickerDialogFragment();
        fragment.mListener = listener;
        fragment.mColor = color;
        fragment.show(activity.getSupportFragmentManager(), tag);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ColorPickerDialog dialog = new ColorPickerDialog(getContext(),
                new ColorPickerDialog.ColorSeletectedListener() {
            @Override
            public void colorPicked(int color) {
                if (mListener != null) mListener.onColorPicked(getTag(), color);
            }
        });
        dialog.updateColor(mColor);
        return dialog;
    }

    public interface ColorPickedListener {
        void onColorPicked(String key, int color);
    }
}
