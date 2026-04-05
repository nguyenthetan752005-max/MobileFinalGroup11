package hcmute.edu.vn.nguyenthetan.ui.common;

import android.app.Dialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.databinding.DialogStreakBinding;

public class StreakDialogFragment extends DialogFragment {

    private static final String ARG_BROKEN = "arg_broken";

    public static StreakDialogFragment newInstance(boolean broken) {
        StreakDialogFragment fragment = new StreakDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_BROKEN, broken);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DialogStreakBinding binding = DialogStreakBinding.inflate(LayoutInflater.from(requireContext()));
        boolean broken = getArguments() != null && getArguments().getBoolean(ARG_BROKEN, false);

        if (broken) {
            binding.textTitle.setText("Streak reset");
            binding.textSubtitle.setText("You have missed 3 days. Start a short session now to rebuild momentum.");
            binding.buttonAction.setText("Start Learning Now");
        } else {
            binding.textTitle.setText("12 day streak");
            binding.textSubtitle.setText("You are showing up consistently. Keep the chain alive today.");
            binding.buttonAction.setText("Keep Going");
        }

        renderWeekDots(binding.weekDotsContainer);
        renderMoods(binding.moodContainer);
        binding.textQuote.setText("Small sessions win because they happen every day.");
        binding.buttonAction.setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();
    }

    private void renderWeekDots(LinearLayout container) {
        boolean[] week = new boolean[]{true, true, true, true, true, false, true};
        container.removeAllViews();
        for (boolean studied : week) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(18), dp(18));
            params.setMargins(0, 0, dp(8), 0);
            dot.setLayoutParams(params);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(ContextCompat.getColor(requireContext(), studied ? R.color.tt_primary : R.color.tt_surface));
            bg.setStroke(dp(2), ContextCompat.getColor(requireContext(), studied ? R.color.tt_primary : R.color.tt_border));
            dot.setBackground(bg);
            container.addView(dot);
        }
    }

    private void renderMoods(LinearLayout container) {
        String[] moods = new String[]{"Happy", "Stylish", "Foreigner", "Angry", "Begging", "Crying Happy"};
        container.removeAllViews();
        for (String mood : moods) {
            TextView item = new TextView(requireContext());
            item.setText(mood);
            item.setTextColor(ContextCompat.getColor(requireContext(), R.color.tt_text_primary));
            item.setPadding(dp(12), dp(10), dp(12), dp(10));
            container.addView(item);
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                requireContext().getResources().getDisplayMetrics()
        );
    }
}
