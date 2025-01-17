package com.example.todoappdemo;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private ChipManager chipManager;
    private ChipGroup chipGroup;
    private LinearLayout tasksContainer, completedTaskContainer, clDailyTasks;
    private ProgressBar progressBar;
    private ImageView checkBunny;
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("tasks");
    private final DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
    private final boolean isChecked = false;
    private TextView tvCompletedTasks, tvLevel, tvRabbitMood;
    private int level;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tasksContainer = view.findViewById(R.id.ll_task_container);
        completedTaskContainer = view.findViewById(R.id.ll_completed_task_container);
        clDailyTasks = view.findViewById(R.id.cl_daily_tasks);
        tvCompletedTasks = view.findViewById(R.id.tv_completed_tasks);
        tvLevel = view.findViewById(R.id.tv_lvl);
        tvRabbitMood = view.findViewById(R.id.tv_bunny_mood);
        checkBunny = view.findViewById(R.id.iv_check_kitty);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setMax(100);

        updateRabbitMood();

        String userId = Objects.requireNonNull(SessionManager.getCurrentUser()).getUserId();
        userDatabaseReference.child(userId).get()
                .addOnCompleteListener(databaseTask -> {
                    if (databaseTask.isSuccessful() && databaseTask.getResult().exists()) {
                        Integer levelInteger = databaseTask.getResult().child("level").getValue(Integer.class);
                        level = Objects.requireNonNullElse(levelInteger, 0);
                        progressBar.setProgress(level);
                        String userLevel = "lvl " + level;
                        tvLevel.setText(userLevel);
                    } else {
                        Log.w("Firebase", "User does not exist.");
                    }
                });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String today = dateFormat.format(new Date());
                boolean hasTasksForToday = false;

                tasksContainer.removeAllViews();
                completedTaskContainer.removeAllViews();

                if (snapshot.getChildrenCount() == 0) {
                    clDailyTasks.setVisibility(View.VISIBLE);
                } else {
                    clDailyTasks.setVisibility(View.GONE);
                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        Tasks task = taskSnapshot.getValue(Tasks.class);
                        String taskId = taskSnapshot.getKey();

                        if (task != null) {
                            if (Objects.equals(task.getDate(), today)) {
                                hasTasksForToday = true;

                                String bgColor = task.getBackgroundColor();
                                String timeDate = task.getDate() + " - " + task.getTime();
                                if (Objects.equals(task.getTime(), "0:0")) {
                                    timeDate = task.getDate();
                                }

                                boolean isChecked = taskSnapshot.child("completed").getValue(Boolean.class) != null &&
                                        Boolean.TRUE.equals(taskSnapshot.child("completed").getValue(Boolean.class));

                                addTask(timeDate, task.getTitle(), task.getChips(), bgColor, taskId, isChecked);
                                Log.d("HomeFragment", "Task: " + taskId + ", Checked: " + isChecked + ", Time: " + timeDate + ", Title: " + task.getTitle());
                            }
                        }
                    }
                    if (!hasTasksForToday) {
                        clDailyTasks.setVisibility(View.VISIBLE);
                    }
                    if (completedTaskContainer.getChildCount() > 0) {
                        tvCompletedTasks.setVisibility(View.VISIBLE);
                    } else {
                        tvCompletedTasks.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read value: " + error.getMessage());
            }
        });

        TextView textViewUserName = view.findViewById(R.id.tv_user_name);
        TextView textViewGreeting = view.findViewById(R.id.tv_welcome_time);
        SessionManager.init(getContext());
        if (SessionManager.isUserLoggedIn()) {
            User user = SessionManager.getCurrentUser();
            assert user != null;
            String userName = user.getName();
            Log.d("HomeFragment", "User name: " + userName + ", User ID: " + userId);
            textViewUserName.setText(userName);

        } else{
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        String template = "HH:mm:ss";
        SimpleDateFormat dateFormat = new SimpleDateFormat(template, java.util.Locale.getDefault());
        String currentTime = dateFormat.format(new Date());
        int hour = Integer.parseInt(currentTime.substring(0, 2));

        String greeting;
        if (hour >= 0 && hour < 12) {
            greeting = "Good Morning";
        } else if(hour >= 12 && hour < 17) {
            greeting = "Good Afternoon";
        }else if(hour >= 17 && hour < 21) {
            greeting = "Good Evening";
        }else{
            greeting = "Good Night";
        }

        textViewGreeting.setText(greeting);
        return view;
    }
    private void addTask(String time, String title, List<String> chips, String bgColor, String taskId, boolean isChecked) {
        @SuppressLint("InflateParams") View taskView = LayoutInflater.from(getContext()).inflate(R.layout.task_item, null, false);

        taskView.findViewById(R.id.ll_task_container).setBackgroundTintList(
                ColorStateList.valueOf(
                        getResources().getColor(
                                getResources().getIdentifier(bgColor, "color", requireContext().getPackageName())
                        )
                )
        );

        ((TextView) taskView.findViewById(R.id.tv_time_date)).setText(time);
        ((TextView) taskView.findViewById(R.id.tv_title)).setText(title);

        CheckBox checkBox = taskView.findViewById(R.id.checkBox);
        checkBox.setChecked(isChecked);
        checkBox.setOnCheckedChangeListener((buttonView, isCheckedNew) -> {
            Log.d("TaskStatus", "Task ID: " + taskId + ", Checked: " + isCheckedNew);
            updateTaskStatusInDatabase(taskId, isCheckedNew);
        });

        ChipGroup chipGroup = taskView.findViewById(R.id.chipGroup);
        if (chips != null) {
            for (String chipText : chips) {
                Chip chip = new Chip(getContext());
                chip.setText(chipText);
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(120));
                chip.setChipStartPadding(8);
                chip.setChipStrokeColorResource(R.color.dark_blue);
                chip.setChipStrokeWidth(3);
                chip.setTextColor(getResources().getColor(R.color.dark_blue));
                chipGroup.addView(chip);
            }
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) taskView.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        params.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        taskView.setLayoutParams(params);

        if (isChecked) {
            completedTaskContainer.addView(taskView);
        } else {
            tasksContainer.addView(taskView);
        }

    }
    private void updateTaskStatusInDatabase(String taskId, boolean isChecked) {
        DatabaseReference taskRef = databaseReference.child(taskId);
        taskRef.child("completed").setValue(isChecked)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Task status updated successfully");
                    if (isChecked) {
                        updateLevelBasedOnCompletedTasks();
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Failed to update task status", e));
    }
    private void updateLevelBasedOnCompletedTasks() {
        String userId = Objects.requireNonNull(SessionManager.getCurrentUser()).getUserId();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int completedTaskCount = 0;

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Boolean isCompleted = taskSnapshot.child("completed").getValue(Boolean.class);
                    if (isCompleted != null && isCompleted) {
                        completedTaskCount++;
                    }
                }
                updateUserLevelBasedOnTasks(userId, completedTaskCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to fetch tasks: " + error.getMessage());
            }
        });
    }

    private void updateUserLevelBasedOnTasks(String userId, int completedTaskCount) {
        userDatabaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer levelInteger = snapshot.child("level").getValue(Integer.class);
                    int level = Objects.requireNonNullElse(levelInteger, 0);

                    int newLevel = level + (completedTaskCount / 20);
                    int remainingTasks = completedTaskCount % 20;

                    userDatabaseReference.child(userId).child("level").setValue(newLevel);
                    Log.d("Firebase", "User level updated to: " + newLevel);

                    Log.d("Firebase", "Tasks remaining for next level: " + (20 - remainingTasks));

                    progressBar.setProgress(newLevel);
                    String lvl = "lvl " + newLevel;
                    tvLevel.setText(lvl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to update user level: " + error.getMessage());
            }
        });
    }
    private void updateRabbitMood() {
        String userId = Objects.requireNonNull(SessionManager.getCurrentUser()).getUserId();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalTasks = 0;
                int completedTasks = 0;

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Tasks task = taskSnapshot.getValue(Tasks.class);
                    if (task != null) {
                        totalTasks++;
                        Boolean isChecked = taskSnapshot.child("completed").getValue(Boolean.class);
                        if (isChecked != null && isChecked) {
                            completedTasks++;
                        }
                    }
                }

                if (totalTasks > 0) {
                    double completionRate = (double) completedTasks / totalTasks * 100;
                    //String bunnyName = SessionManager.getBunnyName();
                    String bunnyName = "Bunny";
                    String mood = getLevelFromCompletionRate(completionRate, bunnyName);
                    getBunnyMoodImage(completionRate);
                    tvRabbitMood.setText(mood);
                } else {
                    tvRabbitMood.setText(getResources().getString(R.string.no_task));
                }
                Log.d("Firebase", "Total tasks: " + totalTasks + ", Completed tasks: " + completedTasks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to fetch tasks: " + error.getMessage());
            }
        });
    }

    private String getLevelFromCompletionRate(double completionRate, String bunnyName) {
        String happy = getString(R.string.happy_bunny);
        String normal = getString(R.string.normal_bunny);
        String sad = getString(R.string.sad_bunny);

        if (completionRate >= 60) return bunnyName + " " + happy;
        else if (completionRate >= 40) return bunnyName + " " + normal;
        else return bunnyName + " " + sad;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void getBunnyMoodImage(double completionRate){
        if (completionRate >= 60) checkBunny.setImageDrawable(getResources().getDrawable(R.drawable.happy_bunny_home));
        else if (completionRate >= 40) checkBunny.setImageDrawable(getResources().getDrawable(R.drawable.normal_bunny_home));
        else checkBunny.setImageDrawable(getResources().getDrawable(R.drawable.sad_bunny_home));
    }

    private void updateLevel(int level){
        String userId = Objects.requireNonNull(SessionManager.getCurrentUser()).getUserId();
        userDatabaseReference.child(userId).setValue(level)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Level status updated successfully"))
                .addOnFailureListener(e -> Log.e("Firebase", "Failed to update level status", e));
    }

    //profile
    //onboarding images
}

