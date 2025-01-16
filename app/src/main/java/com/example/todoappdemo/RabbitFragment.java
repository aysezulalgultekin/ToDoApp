package com.example.todoappdemo;

import android.annotation.SuppressLint;
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

        userRef.child("level").addValueEventListener(new ValueEventListener() {
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
                            Integer levelInteger = snapshot.child("level").getValue(Integer.class);
                            completedTasks++;
                            if (levelInteger != null) {
                                int level = levelInteger;
                                level++;
                                userRef.child("level").setValue(level);
                            }
                        }
                    }
                }
                if (totalTasks > 0) {
                    double completionRate = (double) completedTasks / totalTasks * 100;
                    Integer levelInteger = snapshot.child("level").getValue(Integer.class);
                    if (levelInteger != null) {
                        int level = levelInteger;
                        getRabbitImageByLevel(completionRate, view, level);
                    }
                }
                Log.d("Firebase", "Total tasks: " + totalTasks + ", Completed tasks: " + completedTasks);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to fetch user level: " + error.getMessage());
            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void getRabbitImageByLevel(double completionRate, View view, int level) {
        ImageView ivRabbit = view.findViewById(R.id.iv_bunny);
        //lvl 0: 0, lvl 1: 20 - flowers, lvl 2: 50 - objects, lvl 3 - chick: 70-, lvl 4 - bird: 90
        //level


    }

}