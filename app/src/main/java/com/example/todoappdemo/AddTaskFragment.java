package com.example.todoappdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class AddTaskFragment extends Fragment {
    private ConstraintLayout constraintLayoutPeriod, constraintLayoutCalendarView;
    private LinearLayout linearLayoutTimePicker, linearLayoutTimeButtons;
    private NumberPicker numberPickerHour, numberPickerMinute;
    private RadioButton radioButtonAllDay, radioButtonTimeSelect;
    private ToggleButton toggleButtonRepeat, toggleButtonToday, toggleButtonTime;
    private String selectedDate = "Today", frequency = "No", colorName = "";
    private final Calendar calendar = Calendar.getInstance();
    private final int currentHour = calendar.get(Calendar.HOUR_OF_DAY), currentMinute = calendar.get(Calendar.MINUTE);
    private int selectedHour = 0, selectedMinute = 0;
    private OnBackPressedCallback onBackPressedCallback;
    private ChipGroup chipGroup;
    private ChipManager chipManager;
    private ArrayList<String> chipList;
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("tasks");
    private final String taskId = databaseReference.push().getKey();

    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_task, container, false);

        EditText editTextTask = view.findViewById(R.id.edt_task_title);
        toggleButtonRepeat = view.findViewById(R.id.btn_repeat);
        toggleButtonToday = view.findViewById(R.id.btn_today);
        toggleButtonTime = view.findViewById(R.id.btn_time);
        constraintLayoutPeriod = view.findViewById(R.id.cl_period);
        linearLayoutTimePicker = view.findViewById(R.id.ll_time_picker);
        linearLayoutTimeButtons = view.findViewById(R.id.ll_time_buttons);
        constraintLayoutCalendarView = view.findViewById(R.id.cl_calendar);
        CalendarView calendarView = view.findViewById(R.id.cv_calendar);
        numberPickerHour = view.findViewById(R.id.np_hour);
        numberPickerMinute = view.findViewById(R.id.np_minute);
        Button buttonCreateTask = view.findViewById(R.id.btn_save);
        radioButtonAllDay = view.findViewById(R.id.btn_all_day);
        radioButtonTimeSelect = view.findViewById(R.id.btn_select_time);
        ScrollView scrollView = view.findViewById(R.id.vcv_contents);
        EditText edtHashtag = view.findViewById(R.id.edt_hashtag);
        chipGroup = view.findViewById(R.id.chipGroup);
        chipList = new ArrayList<>();
        chipManager = new ChipManager(requireContext());
        ImageButton backButton = view.findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> closeFragment());

        edtHashtag.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                addChip(edtHashtag.getText().toString());
                edtHashtag.setText("");
                return true;
            }
            return false;
        });

        scrollView.setOnTouchListener((v, event) -> {
            hideKeyboard(v);
            editTextTask.clearFocus();
            edtHashtag.clearFocus();
            return false;
        });

        RadioGroup radioGroup = view.findViewById(R.id.rd_bg_colors);
        RadioButton[] radioButtons = {
                view.findViewById(R.id.radio_1),
                view.findViewById(R.id.radio_2),
                view.findViewById(R.id.radio_3),
                view.findViewById(R.id.radio_4),
                view.findViewById(R.id.radio_5),
                view.findViewById(R.id.radio_6),
                view.findViewById(R.id.radio_7)
        };

        HashMap<Integer, String> colorNameMapping = new HashMap<>();
        colorNameMapping.put(ContextCompat.getColor(requireContext(), R.color.light_yellow), "light_yellow");
        colorNameMapping.put(ContextCompat.getColor(requireContext(), R.color.light_orange), "light_orange");
        colorNameMapping.put(ContextCompat.getColor(requireContext(), R.color.light_red), "light_red");
        colorNameMapping.put(ContextCompat.getColor(requireContext(), R.color.light_purple), "light_purple");
        colorNameMapping.put(ContextCompat.getColor(requireContext(), R.color.light_blue), "light_blue");
        colorNameMapping.put(ContextCompat.getColor(requireContext(), R.color.light_aqua), "light_aqua");
        colorNameMapping.put(ContextCompat.getColor(requireContext(), R.color.light_green), "light_green");

        int[] colors = {
                ContextCompat.getColor(requireContext(), R.color.light_yellow),
                ContextCompat.getColor(requireContext(), R.color.light_orange),
                ContextCompat.getColor(requireContext(), R.color.light_red),
                ContextCompat.getColor(requireContext(), R.color.light_purple),
                ContextCompat.getColor(requireContext(), R.color.light_blue),
                ContextCompat.getColor(requireContext(), R.color.light_aqua),
                ContextCompat.getColor(requireContext(), R.color.light_green)
        };

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        ContextCompat.getColor(requireContext(), R.color.dark_blue),
                        ContextCompat.getColor(requireContext(), R.color.dark_blue)
                }
        );

        for (int i = 0; i < radioButtons.length; i++) {
            radioButtons[i].setBackgroundTintList(ColorStateList.valueOf(colors[i]));
            radioButtons[i].setButtonTintList(colorStateList);
        }

        if (radioButtons[0].isChecked()) {
            colorName = colorNameMapping.get(colors[0]);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < radioButtons.length; i++) {
                if (radioButtons[i].getId() == checkedId) {
                    colorName = colorNameMapping.get(colors[i]);
                }
            }
        });

        numberPickerHour.setMinValue(0);
        numberPickerHour.setMaxValue(23);
        numberPickerHour.setValue(currentHour);
        numberPickerMinute.setMinValue(0);
        numberPickerMinute.setMaxValue(59);
        numberPickerMinute.setValue(currentMinute);
        numberPickerHour.setFormatter(value -> String.format("%02d", value));
        numberPickerMinute.setFormatter(value -> String.format("%02d", value));

        toggleButtonRepeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            constraintLayoutPeriod.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            toggleButtonRepeat.setText(isChecked ? toggleButtonRepeat.getTextOn() : toggleButtonRepeat.getTextOff());
        });

        RadioGroup radioGroupPeriod = view.findViewById(R.id.rg_period);

        radioGroupPeriod.setOnCheckedChangeListener((buttonView, checkedId) -> {
            if (checkedId == R.id.btn_daily) {
                toggleButtonRepeat.setText(getString(R.string.daily));
                toggleButtonRepeat.setTextOn(getString(R.string.daily));
                toggleButtonRepeat.setTextOff(getString(R.string.daily));
                frequency = "Daily";
            } else if (checkedId == R.id.btn_weekly) {
                toggleButtonRepeat.setText(getString(R.string.weekly));
                toggleButtonRepeat.setTextOn(getString(R.string.weekly));
                toggleButtonRepeat.setTextOff(getString(R.string.weekly));
                frequency = "Weekly";
            } else if (checkedId == R.id.btn_monthly) {
                toggleButtonRepeat.setText(getString(R.string.monthly));
                toggleButtonRepeat.setTextOn(getString(R.string.monthly));
                toggleButtonRepeat.setTextOff(getString(R.string.monthly));
                frequency = "Monthly";
            } else if (checkedId == R.id.btn_no_repeat) {
                toggleButtonRepeat.setText(getString(R.string.no_repeat));
                toggleButtonRepeat.setTextOn(getString(R.string.no_repeat));
                toggleButtonRepeat.setTextOff(getString(R.string.no_repeat));
                frequency = "No";
            }
        });

        toggleButtonToday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                constraintLayoutCalendarView.setVisibility(View.VISIBLE);
                updateToggleButtonTodayText();
            } else {
                constraintLayoutCalendarView.setVisibility(View.GONE);
                updateToggleButtonTodayText();
            }
            updateToggleButtonTodayText();
        });

        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            String formattedDay = String.format("%02d", dayOfMonth);
            String formattedMonth = String.format("%02d", month + 1);
            selectedDate = formattedDay + "." + formattedMonth + "." + year;
            updateToggleButtonTodayText();
        });

        updateToggleButtonTodayText();

        toggleButtonTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                linearLayoutTimeButtons.setVisibility(View.VISIBLE);
                linearLayoutTimePicker.setVisibility(radioButtonTimeSelect.isChecked() ? View.VISIBLE : View.GONE);
            } else {
                linearLayoutTimeButtons.setVisibility(View.GONE);
                linearLayoutTimePicker.setVisibility(View.GONE);
            }
        });

        if (selectedHour != 0 || selectedMinute != 0) {
            updateToggleButtonTimeText();
        } else {
            toggleButtonTime.setTextOn(getString(R.string.all_day));
            toggleButtonTime.setTextOff(getString(R.string.all_day));
            toggleButtonTime.setText(getString(R.string.all_day));
        }

        RadioGroup radioGroupTime = view.findViewById(R.id.rg_time);

        radioGroupTime.setOnCheckedChangeListener((buttonView, checkedId) -> {
            if (checkedId == R.id.btn_select_time) {
                linearLayoutTimePicker.setVisibility(View.VISIBLE);
                selectedHour = currentHour;
                selectedMinute = currentMinute;
                numberPickerHour.setOnValueChangedListener((picker, oldVal, newVal) -> {
                    selectedHour = newVal;
                    updateToggleButtonTimeText();
                });

                numberPickerMinute.setOnValueChangedListener((picker, oldVal, newVal) -> {
                    selectedMinute = newVal;
                    updateToggleButtonTimeText();
                });
                updateToggleButtonTimeText();
            } else {
                toggleButtonTime.setTextOn(getString(R.string.all_day));
                toggleButtonTime.setTextOff(getString(R.string.all_day));
                toggleButtonTime.setText(getString(R.string.all_day));
                linearLayoutTimePicker.setVisibility(View.GONE);
                selectedHour = 0;
                selectedMinute = 0;
            }
        });

        buttonCreateTask.setOnClickListener(v -> {
            String hashtagInput = edtHashtag.getText().toString().trim();
            if (!hashtagInput.isEmpty()) {
                addChip(hashtagInput);
                edtHashtag.setText("");
            }
            if (SessionManager.isUserLoggedIn()) {
                String currentUserId = Objects.requireNonNull(SessionManager.getCurrentUser()).getUserId();
                String title = editTextTask.getText().toString();
                Tasks newTask = getNewTask(title, currentUserId);

                if (taskId != null && !editTextTask.getText().toString().trim().isEmpty()) {
                    databaseReference.child(taskId).setValue(newTask).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(getContext(), "Task Created Successfully", Toast.LENGTH_SHORT).show();
                            closeFragment();
                        } else {
                            Toast.makeText(getContext(), "Failed to create task", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                closeFragment();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCallback);

        return view;
    }

    @NonNull
    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    private Tasks getNewTask(String title, String currentUserId) {
        String time = selectedHour + ":" + selectedMinute;
        boolean isAllDay = radioButtonAllDay.isChecked();
        boolean isCompleted = false;
        String date = selectedDate;
        Calendar calendar = Calendar.getInstance();
        if (Objects.equals(selectedDate, "Today")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            date = dateFormat.format(calendar.getTime());
            Log.d("AddTaskFragment", "Date: " + date);
        }
        return new Tasks(
                title,
                colorName,
                frequency,
                date,
                time,
                isAllDay,
                currentUserId,
                chipList.isEmpty() ? null : chipList,
                isCompleted
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onBackPressedCallback.remove();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateToggleButtonTodayText() {
        if (selectedDate == null) {
            toggleButtonToday.setText(getString(R.string.today));
            toggleButtonToday.setTextOn(getString(R.string.today));
            toggleButtonToday.setTextOff(getString(R.string.today));
        } else {
            toggleButtonToday.setTextOn(selectedDate);
            toggleButtonToday.setTextOff(selectedDate);
            toggleButtonToday.setText(selectedDate);
        }
    }

    private void updateToggleButtonTimeText() {
        @SuppressLint("DefaultLocale") String selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
        toggleButtonTime.setText(selectedTime);
        toggleButtonTime.setTextOn(selectedTime);
        toggleButtonTime.setTextOff(selectedTime);
    }

    public void closeFragment() {
        requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
    private void addChip(String text) {
        Chip chip = new Chip(getContext());
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.light_green);
        chip.setCloseIconVisible(true);
        chip.setShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(90));
        chip.setChipStartPadding(8);
        chip.setChipStrokeColorResource(R.color.dark_blue);
        chip.setChipStrokeWidth(3);
        chip.setCloseIconTintResource(R.color.dark_blue);
        chipGroup.addView(chip);
        chipList.add(text);
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            chipList.remove(text);
            chipManager.saveChips(taskId, chipList);
        });
        chipManager.saveChips(taskId, chipList);
    }
}
