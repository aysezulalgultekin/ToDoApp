package com.example.todoappdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditTextLogin,emailEditTextRegister, passwordEditTextLogin, passwordEditTextRegister, nameEditText;
    private TextView warningTextViewLogin, warningTextViewRegister;
    private  User user;
    private FirebaseAuth mAuth;
    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        SessionManager.init(this);

        if (SessionManager.isUserLoggedIn()) {
            User user = SessionManager.getCurrentUser();
            assert user != null;
            Log.d("LoginActivity", "User is already logged in: " + user.getEmail());
            Intent intentHome = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intentHome);
            finish();
            return;
        }

        emailEditTextLogin = findViewById(R.id.edt_email_login);
        nameEditText = findViewById(R.id.edt_user_name);
        passwordEditTextLogin = findViewById(R.id.edt_password_login);
        warningTextViewLogin = findViewById(R.id.tv_warn_login);
        emailEditTextRegister = findViewById(R.id.edt_email_register);
        passwordEditTextRegister = findViewById(R.id.edt_password_register);
        warningTextViewRegister = findViewById(R.id.tv_warn_register);
        MaterialCardView loginCardView = findViewById(R.id.mvc_login);
        MaterialCardView registerCardView = findViewById(R.id.mcv_register);
        Button signInButton = findViewById(R.id.btn_sign_in);
        Button signUpButton = findViewById(R.id.btn_sign_up);
        Button registerButton = findViewById(R.id.btn_register);
        Button loginButton = findViewById(R.id.btn_login);
        TextView forgotPasswordButton = findViewById(R.id.tv_forgot_password);
        ConstraintLayout mainLayout = findViewById(R.id.cl_container);

        signInButton.setOnClickListener(v -> {
            showCardWithAnimation(loginCardView);
            registerCardView.setVisibility(MaterialCardView.GONE);
        });

        signUpButton.setOnClickListener(v -> {
            showCardWithAnimation(registerCardView);
            loginCardView.setVisibility(MaterialCardView.GONE);
        });

        mainLayout.setOnClickListener(v -> {
            hideCardWithAnimation(loginCardView);
            hideCardWithAnimation(registerCardView);
        });

        registerButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditTextRegister.getText().toString().trim();
            String password = passwordEditTextRegister.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                warningTextViewRegister.setVisibility(TextView.VISIBLE);
                warningTextViewRegister.setText(getString(R.string.fill_all_fields));
                return;
            }else {
                warningTextViewRegister.setVisibility(TextView.GONE);
            }

            if (!email.contains("@") || !email.contains(".com")) {
                warningTextViewRegister.setVisibility(TextView.VISIBLE);
                warningTextViewRegister.setText(getString(R.string.email_requirements));
                return;
            }else {
                warningTextViewRegister.setVisibility(TextView.GONE);
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            warningTextViewRegister.setVisibility(TextView.GONE);
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            assert firebaseUser != null;
                            String userId = firebaseUser.getUid();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                            int level = 0;

                            User user = new User(userId, email, name, level);
                            databaseReference.child(userId).setValue(user)
                                    .addOnCompleteListener(databaseTask -> {
                                        if (databaseTask.isSuccessful()) {
                                            SessionManager.setCurrentUser(user);
                                            Log.d("RegisterActivity", "Registering successful! Welcome: " + user.getEmail());
                                            onboardingIntent();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Registration failed: " + Objects.requireNonNull(databaseTask.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                warningTextViewRegister.setVisibility(TextView.VISIBLE);
                                warningTextViewRegister.setText(getString(R.string.user_collusion));
                            } else {
                                warningTextViewRegister.setVisibility(TextView.GONE);
                                Toast.makeText(LoginActivity.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        loginButton.setOnClickListener(v -> {
            String email = emailEditTextLogin.getText().toString().trim();
            String password = passwordEditTextLogin.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                warningTextViewLogin.setVisibility(TextView.VISIBLE);
                warningTextViewLogin.setText(getString(R.string.email_or_password_is_empty));
                return;
            }else {
                warningTextViewLogin.setVisibility(TextView.GONE);
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            assert firebaseUser != null;
                            String userId = firebaseUser.getUid();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                            databaseReference.child(userId).get()
                                    .addOnCompleteListener(databaseTask -> {
                                        if (databaseTask.isSuccessful() && databaseTask.getResult().exists()) {
                                            warningTextViewLogin.setVisibility(TextView.GONE);
                                            String name = databaseTask.getResult().child("name").getValue(String.class);
                                            String emailFromDb = databaseTask.getResult().child("email").getValue(String.class);
                                            Integer levelInteger = databaseTask.getResult().child("level").getValue(Integer.class);

                                            int level;
                                            level = Objects.requireNonNullElse(levelInteger, 0);

                                            User user = new User(userId, emailFromDb, name, level);
                                            SessionManager.setCurrentUser(user);

                                            Log.d("LoginActivity", "Login successful! Welcome: " + user.getEmail());
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            warningTextViewLogin.setVisibility(TextView.VISIBLE);
                                            warningTextViewLogin.setText(getString(R.string.user_not_found));
                                        }
                                    });
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        forgotPasswordButton.setOnClickListener(v -> {
            String email = emailEditTextLogin.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                warningTextViewLogin.setVisibility(TextView.VISIBLE);
                warningTextViewLogin.setText(getString(R.string.enter_email_address));
                return;
            }else {
                warningTextViewLogin.setVisibility(TextView.GONE);
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Password reset email sent to: " + email, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void showCardWithAnimation(MaterialCardView cardView) {
        cardView.setVisibility(MaterialCardView.VISIBLE);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        cardView.startAnimation(slideUp);
    }

    private void hideCardWithAnimation(MaterialCardView cardView) {
        if (cardView.getVisibility() == MaterialCardView.VISIBLE) {
            Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
            cardView.startAnimation(slideDown);
            cardView.setVisibility(MaterialCardView.GONE);
        }
    }
    public void onboardingIntent(){
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }
}
/*container.setVisibility(View.VISIBLE);
        String enteredName = ((EditText) findViewById(R.id.edt_enter_name)).getText().toString();
        Button enterButton = findViewById(R.id.btn_enter_name);
        enterButton.setOnClickListener(v1 -> {
            if (!enteredName.isEmpty()) {
                SharedPreferences.Editor editor1 = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                editor1.putString("username", enteredName);
                editor1.apply();
                finish();
            }
        });*/