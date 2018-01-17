package com.enrico.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class ColorPickerDialog extends Dialog implements SeekBar.OnSeekBarChangeListener {

    private int mColor = Color.RED;
    private View mValue;

    private SeekBar mRedSeekBar;
    private SeekBar mGreenSeekBar;
    private SeekBar mBlueSeekBar;

    private TextView mHexText;
    private TextView mHashTag;
    private TextView mSpacer1;
    private TextView mSpacer2;
    private TextView mRGB;
    private EditText mHex;
    private EditText mRed;
    private EditText mGreen;
    private EditText mBlue;

    private TextView mUserGridTitle;
    private GridView mUserGridView;

    private ColorPickerDialogListener mListener;
    private boolean mUpdating = false;

    public ColorPickerDialog(@NonNull Context context, ColorPickerDialogListener listener) {
        super(new ContextThemeWrapper(context, R.style.ColorPickerDialog));
        if (getActionBar() != null) getActionBar().hide();
        setCanceledOnTouchOutside(true);

        mListener = listener;

        View view = View.inflate(context, R.layout.color_dialog, null);
        GridView paletteGrid = view.findViewById(R.id.palette);

        mUserGridTitle = view.findViewById(R.id.userPaletteText);
        mUserGridView = view.findViewById(R.id.userPalette);

        mValue = view.findViewById(R.id.values_view);
        mHexText = view.findViewById(R.id.hashtext);
        mHashTag = view.findViewById(R.id.hashtag);
        mRGB = view.findViewById(R.id.rgb);
        mHex = view.findViewById(R.id.hex);
        mRed = view.findViewById(R.id.red_edit);
        mGreen = view.findViewById(R.id.green_edit);
        mBlue = view.findViewById(R.id.blue_edit);
        mSpacer1 = view.findViewById(R.id.spacer_1);
        mSpacer2 = view.findViewById(R.id.spacer_2);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int r, g, b;
                try {
                    r = Integer.parseInt(mRed.getText().toString());
                    g = Integer.parseInt(mGreen.getText().toString());
                    b = Integer.parseInt(mBlue.getText().toString());
                    updateColor(Color.rgb(r, g, b));
                } catch (Exception ignored) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        paletteGrid.setAdapter(new PaletteAdapter(getContext(), context.getResources().getIntArray(R.array.colors)));

        mRedSeekBar = view.findViewById(R.id.red_seekbar);
        mGreenSeekBar = view.findViewById(R.id.green_seekbar);
        mBlueSeekBar = view.findViewById(R.id.blue_seekbar);

        mRedSeekBar.setOnSeekBarChangeListener(this);
        mRedSeekBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        mRedSeekBar.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        mGreenSeekBar.setOnSeekBarChangeListener(this);
        mGreenSeekBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        mGreenSeekBar.getThumb().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        mBlueSeekBar.setOnSeekBarChangeListener(this);
        mBlueSeekBar.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
        mBlueSeekBar.getThumb().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);

        setContentView(view);
        updateColor(mColor, true);

        mHex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int color;
                try {
                    color = Integer.decode("0x" + s.toString());
                    updateColor(color);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mRed.addTextChangedListener(watcher);
        mGreen.addTextChangedListener(watcher);
        mBlue.addTextChangedListener(watcher);

        View cancel = view.findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        View ok = view.findViewById(R.id.ok_button);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.colorPicked("", mColor);
                dismiss();
            }
        });
    }
    private static boolean isDarkColor(int color) {
        double darkness = 1 - (0.299 * Color.red(color)
                + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness > 0.5f;
    }

    @SuppressWarnings("WeakerAccess")
    public void updateColor(int color) {
        updateColor(color, false);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mListener != null) {
            mListener.dismiss();
        }
    }

    public void setColors(int[] colors) {
        if (colors != null && colors.length > 0) {
            mUserGridTitle.setVisibility(View.VISIBLE);
            mUserGridView.setVisibility(View.VISIBLE);
            mUserGridView.setAdapter(new PaletteAdapter(getContext(), colors));
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    private void updateColor(int color, boolean force) {
        if ((mColor == color && !force) || mUpdating) {
            return;
        }
        mUpdating = true;
        mColor = color;
        int textColor = isDarkColor(mColor) ? Color.WHITE : Color.BLACK;
        mValue.setBackgroundColor(mColor);
        mHex.setTextColor(textColor);
        mHexText.setTextColor(textColor);
        mHashTag.setTextColor(textColor);
        mRGB.setTextColor(textColor);
        mRed.setTextColor(textColor);
        mGreen.setTextColor(textColor);
        mBlue.setTextColor(textColor);
        mSpacer1.setTextColor(textColor);
        mSpacer2.setTextColor(textColor);
        if (!mHex.getText().toString().equals(String.format("%06x", mColor))) {
            mHex.setText(String.format("%06x", mColor));
        }
        if (!mRed.getText().toString().equals(String.valueOf(Color.red(mColor)))) {
            mRed.setText(String.valueOf(Color.red(mColor)));
        }
        if (!mGreen.getText().toString().equals(String.valueOf(Color.green(mColor)))) {
            mGreen.setText(String.valueOf(Color.green(mColor)));
        }
        if (!mBlue.getText().toString().equals(String.valueOf(Color.blue(mColor)))) {
            mBlue.setText(String.valueOf(Color.blue(mColor)));
        }
        mRedSeekBar.setProgress(Color.red(mColor));
        mBlueSeekBar.setProgress(Color.blue(mColor));
        mGreenSeekBar.setProgress(Color.green(mColor));
        mUpdating = false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int red = Color.red(mColor);
        int green = Color.green(mColor);
        int blue = Color.blue(mColor);
        if (seekBar.getId() == R.id.red_seekbar) {
            red = progress;
        } else if (seekBar.getId() == R.id.green_seekbar) {
            green = progress;
        } else if (seekBar.getId() == R.id.blue_seekbar) {
            blue = progress;
        }

        updateColor(Color.rgb(red, green, blue));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public interface ColorPickerDialogListener {
        void colorPicked(String key, int color);
        void dismiss();
    }

    private class PaletteAdapter extends BaseAdapter {

        private int[] mColors;

        private PaletteAdapter(Context context, int[] colors) {
            super();

            mColors = colors;
        }

        @Override
        public int getCount() {
            return mColors.length;
        }

        @Override
        public Object getItem(int position) {
            return mColors[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.palette_view, parent, false);
            }
            ImageView iv = convertView.findViewById(R.id.icon);
            iv.setBackground(new CircleDrawable(mColors[position]));
            convertView.setTag(mColors[position]);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateColor(mColors[position]);
                }
            });
            return convertView;
        }
    }

    //get complementary color
    public static int getComplementaryColor(int colorToInvert) {

        int r = Color.red(colorToInvert);
        int g = Color.green(colorToInvert);
        int b = Color.blue(colorToInvert);
        int red = 255 - r;
        int green = 255 - g;
        int blue = 255 - b;

        return android.graphics.Color.argb(255, red, green, blue);

    }
}
