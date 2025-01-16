package com.example.todoappdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    String bunnyName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        Button nextButton = findViewById(R.id.btn_next);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        LinearLayout llBunnyName = findViewById(R.id.ll_bunny_name);
        EditText editTextBunnyName = findViewById(R.id.tv_bunny_name);
        Button enterButton = findViewById(R.id.btn_bunny);

        List<OnboardingItem> onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem("Welcome", "Discover our app!", R.drawable.image_1));
        onboardingItems.add(new OnboardingItem("Organize", "Track your tasks efficiently.", R.drawable.image_1));
        onboardingItems.add(new OnboardingItem("Enjoy", "Achieve your goals with ease.", R.drawable.image_1));

        OnboardingAdapter adapter = new OnboardingAdapter(onboardingItems);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText("")).attach();

        nextButton.setOnClickListener(v -> {
            llBunnyName.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.GONE);

            enterButton.setOnClickListener(view1 -> {
                bunnyName = editTextBunnyName.getText().toString();
                if (!bunnyName.isEmpty()) {
                    SessionManager.setBunnyName(bunnyName);
                    Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    editTextBunnyName.setError(getResources().getString(R.string.bunny_name_error));
                }

            });
        });
    }

    /*Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();*/
}
