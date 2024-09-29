package com.easyfitness.views;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;

import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Keyboard;

import java.util.Calendar;

public class SingleValueInputView extends LinearLayout {
    private View rootView;
    private TextView titleTextView;
    private AppCompatEditText valueEditText;
    private AppCompatSpinner unitSpinner;
    private LinearLayout commentLayout;
    private TextView commentTextView;
    private CharSequence[] mUnits;

    private boolean mShowUnit;
    private String mTitle;
    private boolean mShowComment;
    private String mComment;
    private String mValue;
    private int mType;
    private boolean mIsTimePickerShown = false;
    private boolean mIsDatePickerShown = false;
    private int mImeOptions;

    public SingleValueInputView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public SingleValueInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SingleValueInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressLint("WrongViewCast")
    protected void init(Context context, AttributeSet attrs) {

        rootView = inflate(context, R.layout.singlevalueinput_view, this);
        titleTextView = rootView.findViewById(R.id.singlevalueinput_title);
        valueEditText = rootView.findViewById(R.id.singlevalueinput_value);
        unitSpinner = rootView.findViewById(R.id.singlevalueinput_unitSpinner);
        commentTextView = rootView.findViewById(R.id.singlevalueinput_comment);
        commentLayout = rootView.findViewById(R.id.singlevalueinput_commentLayout);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SingleValueInputView,
                0, 0);

        try {
            mShowUnit = a.getBoolean(R.styleable.SingleValueInputView_showUnit, false);
            showUnit(mShowUnit);
            mShowComment = a.getBoolean(R.styleable.SingleValueInputView_showComment, false);
            setShowComment(mShowComment);
            mTitle = a.getString(R.styleable.SingleValueInputView_title);
            setTitle(mTitle);
            mComment = a.getString(R.styleable.SingleValueInputView_comment);
            setComment(mComment);
            mValue = a.getString(R.styleable.SingleValueInputView_value);
            setValue(mValue);
            mType = a.getInteger(R.styleable.SingleValueInputView_type, 0);
            setType(mType);
            CharSequence[] entries = a.getTextArray(R.styleable.SingleValueInputView_units);
            if (entries != null) {
                setUnits(entries);
            }
            mImeOptions = a.getInt(R.styleable.SingleValueInputView_android_imeOptions, 0);
            valueEditText.setImeOptions(mImeOptions);
        } finally {
            a.recycle();
        }
    }

    public boolean isShowUnit() {
        return mShowUnit;
    }

    public void showUnit(boolean showUnit) {
        mShowUnit = showUnit;
        unitSpinner.setVisibility(mShowUnit ? View.VISIBLE : View.GONE);
        invalidate();
        requestLayout();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
        titleTextView.setText(mTitle);
        invalidate();
        requestLayout();
    }

    public boolean isShowComment() {
        return mShowComment;
    }

    public void setShowComment(boolean showComment) {
        mShowComment = showComment;
        commentLayout.setVisibility(mShowComment ? View.VISIBLE : View.GONE);
        invalidate();
        requestLayout();
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
        commentTextView.setText(mComment);
        commentLayout.setVisibility(comment == null || comment.isEmpty() ? View.GONE : View.VISIBLE);
        invalidate();
        requestLayout();
    }

    public String getValue() {
        return valueEditText.getText().toString();
    }

    public void setValue(String value) {
        mValue = value;
        valueEditText.setText(mValue);
        invalidate();
        requestLayout();
    }

    public CharSequence[] getUnits() {
        return mUnits;
    }

    public void setUnits(CharSequence[] value) {
        mUnits = value;
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(unitSpinner.getContext(), android.R.layout.simple_spinner_item, mUnits);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);
        invalidate();
        requestLayout();
    }

    public String getSelectedUnit() {
        return unitSpinner.getSelectedItem().toString();
    }

    public void setSelectedUnit(String selectedUnit) {
        ArrayAdapter<String> arrayAdapter = (ArrayAdapter) unitSpinner.getAdapter();
        if (arrayAdapter != null) {
            unitSpinner.setSelection(arrayAdapter.getPosition(selectedUnit));
        }
    }

    public boolean isEmpty() {
        return valueEditText.getText().toString().isEmpty();
    }

    public int getType() {
        return mType;
    }

    public void setType(int value) {
        mType = value;

        if (value == 3) { // time
            valueEditText.setFocusable(false);
            valueEditText.setOnClickListener(v -> {
                if (mIsTimePickerShown) return;

                // Parsing the time text (format: HH:mm:ss)
                String tx = valueEditText.getText().toString();
                int hour = 0, minute = 0, seconds = 0;
                try {
                    String[] timeParts = tx.split(":");
                    hour = Integer.parseInt(timeParts[0]);
                    minute = Integer.parseInt(timeParts[1]);
                    seconds = Integer.parseInt(timeParts.length > 2 ? timeParts[2] : "0");
                } catch (Exception e) {
                    // Ignore parsing issues and use default (0) for time components
                }

                TimePickerDialog mTimePicker = new TimePickerDialog(this.getContext(), (timePicker, selectedHour, selectedMinute) -> {
                    valueEditText.setText(String.format("%02d:%02d:00", selectedHour, selectedMinute));
                }, hour, minute, true);

                mTimePicker.setOnDismissListener(dialog -> mIsTimePickerShown = false);
                mTimePicker.setTitle("Select Time");
                mIsTimePickerShown = true;
                mTimePicker.show();
            });
        } else if (value == 2) { // date
            valueEditText.setFocusable(false);
            valueEditText.setOnClickListener(v -> {
                if (mIsDatePickerShown) return;

                Calendar cal = Calendar.getInstance();
                DatePickerDialog mDatePicker = new DatePickerDialog(this.getContext(),
                        (view, year, month, day) -> {
                            valueEditText.setText(DateConverter.dateToLocalDateStr(year, month, day, getContext()));
                            Keyboard.hide(getContext(), valueEditText);
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH));

                mDatePicker.setOnDismissListener(dialog -> mIsDatePickerShown = false);
                mDatePicker.setTitle("Select Date");
                mIsDatePickerShown = true;
                mDatePicker.show();
            });
        } else {
            valueEditText.setFocusableInTouchMode(true);
            valueEditText.setOnClickListener(null);
        }
    }
}
