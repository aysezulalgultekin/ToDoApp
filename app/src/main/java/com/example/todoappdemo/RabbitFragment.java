package com.example.todoappdemo;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class RabbitFragment extends Fragment {
    private final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("tasks");
    private final DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
    private RecyclerView recyclerView;
    private List<Item> allItems;
    private List<Item> userItems;
    private TextView levelTextView;
    private int userLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rabbit, container, false);

        levelTextView = view.findViewById(R.id.tv_lvl);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        String userId = Objects.requireNonNull(SessionManager.getCurrentUser()).getUserId();
        userDatabaseReference.child(userId).get()
                .addOnCompleteListener(databaseTask -> {
                    if (databaseTask.isSuccessful() && databaseTask.getResult().exists()) {
                        Integer levelInteger = databaseTask.getResult().child("level").getValue(Integer.class);
                        userLevel = Objects.requireNonNullElse(levelInteger, 0);
                        updateRabbitImage(view, userLevel);
                        objRecyclerView(view, userLevel);
                        String lvl = "lvl " + userLevel;
                        levelTextView.setText(lvl);
                        progressBar.setProgress(userLevel);
                        Log.d("Firebase", "User level: " + userLevel + "userId: " + userId);
                    } else {
                        Log.w("Firebase", "User does not exist.");
                    }
                });

        return view;
    }
    private void objRecyclerView(View view, int userLevel) {
        recyclerView = view.findViewById(R.id.rv_obj);
        Button btnAll = view.findViewById(R.id.btn_all);
        Button btnMine = view.findViewById(R.id.btn_mine);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        allItems = new ArrayList<>();
        allItems.add(new Item("Flowers", 20, R.drawable.flowers_obj));
        allItems.add(new Item("Basket", 50, R.drawable.basket_obj));
        allItems.add(new Item("Ball", 50, R.drawable.ball_obj));
        allItems.add(new Item("Chick", 70, R.drawable.chick_obj));
        allItems.add(new Item("Bird", 90, R.drawable.bird_obj));

        userItems = new ArrayList<>();
        for (Item item : allItems) {
            if (userLevel >= item.getLevelRequirement()) {
                userItems.add(item);
            }
        }

        updateRecyclerView(allItems);

        btnAll.setOnClickListener(v -> updateRecyclerView(allItems));
        btnMine.setOnClickListener(v -> updateRecyclerView(userItems));
    }
    private void updateRecyclerView(List<Item> items) {
        RecyclerView.Adapter<ItemViewHolder> adapter = new RecyclerView.Adapter<>() {
            @NonNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.obj_item, parent, false);
                return new ItemViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
                Item item = items.get(position);
                holder.imageView.setImageResource(item.getImageResId());
                holder.textView.setText(item.getName());
            }

            @Override
            public int getItemCount() {
                return items.size();
            }
        };

        recyclerView.setAdapter(adapter);
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            textView = itemView.findViewById(R.id.item_name);
        }
    }
    private void updateRabbitImage(View view, int level) {
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

    private void playLevelUpAnimation(LottieAnimationView animationView, int userLevel, int newLevel) {
        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();

        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                animationView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                animationView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });
    }
}

