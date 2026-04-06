package hcmute.edu.vn.nguyenthetan.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import hcmute.edu.vn.nguyenthetan.ui.main.MainActivity;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityOnboardingBinding;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonGuest.setOnClickListener(v -> openMain());
        binding.buttonGoogle.setOnClickListener(v -> showPlaceholder());
        binding.buttonFacebook.setOnClickListener(v -> showPlaceholder());
    }

    private void showPlaceholder() {
        Toast.makeText(this, "Auth flow will be connected after the UI baseline.", Toast.LENGTH_SHORT).show();
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
