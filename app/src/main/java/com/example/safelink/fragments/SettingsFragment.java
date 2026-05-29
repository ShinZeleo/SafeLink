package com.example.safelink.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.safelink.R;
import com.example.safelink.database.HistoryRepository;
import com.example.safelink.utils.ThemeHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchMaterial switchDark = view.findViewById(R.id.switch_dark_mode);
        View rowClear = view.findViewById(R.id.row_clear_history);

        // Theme Switcher
        switchDark.setChecked(ThemeHelper.isDarkMode(requireContext()));
        switchDark.setOnCheckedChangeListener((btn, isChecked) -> {
            ThemeHelper.setDarkMode(requireContext(), isChecked);
        });

        // Clear History
        rowClear.setOnClickListener(v -> showClearDialog());
    }

    private void showClearDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Riwayat")
                .setMessage("Apakah Anda yakin ingin menghapus semua riwayat pemindaian? Tindakan ini tidak dapat dibatalkan.")
                .setPositiveButton("Hapus Semua", (dialog, which) -> {
                    clearHistory();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void clearHistory() {
        executor.execute(() -> {
            HistoryRepository repo = new HistoryRepository(requireContext());
            repo.clearAll();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Riwayat berhasil dihapus", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
