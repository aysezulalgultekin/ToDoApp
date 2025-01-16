package com.example.todoappdemo;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Objects;


public class RabbitFragment extends Fragment {
    private final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("tasks");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rabbit, container, false);
        updateRabbitImage(view);
        return view;
    }

    private void updateRabbitImage(View view) {
        String userId = Objects.requireNonNull(SessionManager.getCurrentUser()).getUserId();

        userRef.addValueEventListener(new ValueEventListener() {
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
                    Integer levelInteger = snapshot.child(userId).child("level").getValue(Integer.class);
                    int level = Objects.requireNonNullElse(levelInteger, 0);
                    double completionRate = (double) completedTasks / totalTasks * 100;
                    updateRabbitImageDrawable(completionRate, level, view);
                }

                Log.d("Firebase", "Total tasks: " + totalTasks + ", Completed tasks: " + completedTasks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to fetch tasks: " + error.getMessage());
            }
        });
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateRabbitImageDrawable(double completionRate, int level, View view) {
        ImageView ivRabbit = view.findViewById(R.id.iv_bunny);
        String mood;

        if (completionRate >= 60) {
            mood = "happy";
        } else if (completionRate >= 40) {
            mood = "normal";
        } else {
            mood = "sad";
        }

        int rank;
        if (level >= 90) {
            rank = 4;
        } else if (level >= 70) {
            rank = 3;
        } else if (level >= 50) {
            rank = 2;
        } else if (level >= 20) {
            rank = 1;
        } else {
            rank = 0;
        }

        Drawable drawable = null;
        switch (mood) {
            case "happy":
                switch (rank) {
                    case 4:
                        drawable = getResources().getDrawable(R.drawable.happy_rank_4);
                        break;
                    case 3:
                        drawable = getResources().getDrawable(R.drawable.happy_rank_3);
                        break;
                    case 2:
                        drawable = getResources().getDrawable(R.drawable.happy_rank_2);
                        break;
                    case 1:
                        drawable = getResources().getDrawable(R.drawable.happy_rank_1);
                        break;
                    default:
                        drawable = getResources().getDrawable(R.drawable.happy_rank_0);
                        break;
                }
                break;
            case "normal":
                switch (rank) {
                    case 4:
                        drawable = getResources().getDrawable(R.drawable.normal_rank_4);
                        break;
                    case 3:
                        drawable = getResources().getDrawable(R.drawable.normal_rank_3);
                        break;
                    case 2:
                        drawable = getResources().getDrawable(R.drawable.normal_rank_2);
                        break;
                    case 1:
                        drawable = getResources().getDrawable(R.drawable.normal_rank_1);
                        break;
                    default:
                        drawable = getResources().getDrawable(R.drawable.normal_rank_0);
                        break;
                }
                break;
            case "sad":
                switch (rank) {
                    case 4:
                        drawable = getResources().getDrawable(R.drawable.sad_rank_4);
                        break;
                    case 3:
                        drawable = getResources().getDrawable(R.drawable.sad_rank_3);
                        break;
                    case 2:
                        drawable = getResources().getDrawable(R.drawable.sad_rank_2);
                        break;
                    case 1:
                        drawable = getResources().getDrawable(R.drawable.sad_rank_1);
                        break;
                    default:
                        drawable = getResources().getDrawable(R.drawable.sad_rank_0);
                        break;
                }
                break;
        }

        if (drawable != null) {
            ivRabbit.setImageDrawable(drawable);
        } else {
            Log.e("RabbitImage", "Drawable not found for mood: " + mood + ", rank: " + rank);
        }
    }

}

