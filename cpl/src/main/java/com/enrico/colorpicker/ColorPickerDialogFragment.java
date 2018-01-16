package com.enrico.colorpicker;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

public class ColorPickerDialogFragment extends DialogFragment {

    private ColorPickerDialog.ColorPickerDialogListener mListener;
    private int mColor;

    public static void showColorPicker(AppCompatActivity activity,
                                       ColorPickerDialog.ColorPickerDialogListener listener,
                                       int color, String tag) {
        ColorPickerDialogFragment fragment = new ColorPickerDialogFragment();
        if (listener != null) {
            fragment.mListener = listener;
        } else {
            fragment.mListener = new ColorPickerDialog.ColorPickerDialogListener() {
                @Override
                public void colorPicked(String key, int color) {
                }

                @Override
                public void dismiss() {
                }
            };
        }
        fragment.mColor = color;
        fragment.show(activity.getSupportFragmentManager(), tag);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ColorPickerDialog dialog = new ColorPickerDialog(getContext(),
                mListener);
        dialog.updateColor(mColor);
        return dialog;
    }
}
