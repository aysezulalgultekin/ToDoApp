package com.example.todoappdemo;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CalendarFragment extends Fragment {
    private LinearLayout tasksContainer;
    private TextView tvMonthYear;
    private RecyclerView calendarRecyclerView;
    private int currentMonthIndex;
    private String currentYear, currentMonth;
    private String selectedDay;
    private DatabaseReference databaseReference, userDatabaseReference;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference("tasks");

        tvMonthYear = view.findViewById(R.id.tv_month_year);
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));

        tasksContainer = view.findViewById(R.id.ll_task_container);

        Calendar calendar = Calendar.getInstance();
        currentMonthIndex = calendar.get(Calendar.MONTH);
        currentYear = String.valueOf(calendar.get(Calendar.YEAR));
        currentMonth = new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
        updateMonthYearDisplay();

        view.findViewById(R.id.btn_previous_month).setOnClickListener(v -> changeMonth(false));
        view.findViewById(R.id.btn_next_month).setOnClickListener(v -> changeMonth(true));

        loadCalendarDays();

        return view;
    }

    private void updateMonthYearDisplay() {
        String str = currentMonth + " " + currentYear;
        tvMonthYear.setText(str);
    }
    private void loadCalendarDays() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, currentMonthIndex);
        calendar.set(Calendar.YEAR, Integer.parseInt(currentYear));
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<String> days = new ArrayList<>();
        int firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        for (int i = 0; i < firstDayOfMonth; i++) {
            days.add("");
        }
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(String.valueOf(i));
        }

        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this::onDaySelected);
        calendarRecyclerView.setAdapter(calendarAdapter);

        if (selectedDay != null) {
            int selectedDayPosition = days.indexOf(selectedDay);
            calendarAdapter.setSelectedPosition(selectedDayPosition);
            loadTasksForDay(selectedDay);
        } else {
            tasksContainer.removeAllViews();
        }
    }

    private void onDaySelected(String day) {
        selectedDay = day;
        loadTasksForDay(day);
    }


    private void loadTasksForDay(String day) {
        String formattedDay = String.format(Locale.getDefault(), "%02d", Integer.parseInt(day));
        String dateString = formattedDay + "." + String.format(Locale.getDefault(), "%02d", currentMonthIndex + 1) + "." + currentYear;

        databaseReference.orderByChild("date").equalTo(dateString)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tasksContainer.removeAllViews();
                        for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                            Tasks task = taskSnapshot.getValue(Tasks.class);
                            String taskId = taskSnapshot.getKey();
                            boolean isChecked = taskSnapshot.child("completed").getValue(Boolean.class) != null &&
                                    Boolean.TRUE.equals(taskSnapshot.child("completed").getValue(Boolean.class));
                            if (task != null) {
                                addTaskToView(task, isChecked, taskId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Failed to load tasks for day: " + error.getMessage());
                    }
                });
    }

    private void addTaskToView(Tasks task, boolean isChecked, String taskId) {
        View taskView = LayoutInflater.from(getContext()).inflate(R.layout.task_item, tasksContainer, false);

        taskView.findViewById(R.id.ll_task_container).setBackgroundTintList(
                ColorStateList.valueOf(getResources().getColor(getResources().getIdentifier(task.getBackgroundColor(), "color", requireContext().getPackageName()))));

        String timeDate = task.getTime() + " - " + task.getDate();
        if (Objects.equals(task.getTime(), "0:0")) {
            timeDate = task.getDate();
        }
        ((TextView) taskView.findViewById(R.id.tv_time_date)).setText(timeDate);
        ((TextView) taskView.findViewById(R.id.tv_title)).setText(task.getTitle());

        CheckBox checkBox = taskView.findViewById(R.id.checkBox);
        checkBox.setChecked(isChecked);
        checkBox.setOnCheckedChangeListener((buttonView, isCheckedNew) -> {
            Log.d("TaskStatus", "Task ID: " + taskId + ", Checked: " + isCheckedNew);
            updateTaskStatusInDatabase(taskId, isCheckedNew);
        });

        ChipGroup chipGroup = taskView.findViewById(R.id.chipGroup);
        if (task.getChips() != null) {
            for (String chipText : task.getChips()) {
                Chip chip = new Chip(getContext());
                chip.setText(chipText);
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setChipStrokeColorResource(R.color.dark_blue);
                chip.setChipStrokeWidth(3);
                chip.setTextColor(getResources().getColor(R.color.dark_blue));
                chipGroup.addView(chip);
            }
        }
        tasksContainer.addView(taskView);
    }

    private void updateTaskStatusInDatabase(String taskId, boolean isChecked) {
        DatabaseReference taskRef = databaseReference.child(taskId);
        taskRef.child("completed").setValue(isChecked)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Task status updated successfully"))
                .addOnFailureListener(e -> Log.e("Firebase", "Failed to update task status", e));
    }

    private void changeMonth(boolean nextMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, currentMonthIndex);
        calendar.set(Calendar.YEAR, Integer.parseInt(currentYear));

        if (nextMonth) {
            calendar.add(Calendar.MONTH, 1);
        } else {
            calendar.add(Calendar.MONTH, -1);
        }

        currentMonthIndex = calendar.get(Calendar.MONTH);
        currentYear = String.valueOf(calendar.get(Calendar.YEAR));
        currentMonth = new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
        updateMonthYearDisplay();
        loadCalendarDays();
    }
}
