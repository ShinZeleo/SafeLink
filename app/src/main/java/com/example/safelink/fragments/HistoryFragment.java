package com.example.safelink.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safelink.R;
import com.example.safelink.adapters.HistoryAdapter;
import com.example.safelink.database.HistoryRepository;
import android.widget.TextView;
import com.example.safelink.models.ScanResult;
import com.example.safelink.models.HistoryModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private View layoutEmpty;
    private TextView tvTotal, tvSafe, tvDanger;
    private HistoryAdapter adapter;
    private HistoryRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHistory = view.findViewById(R.id.rv_history);
        layoutEmpty = view.findViewById(R.id.tv_empty_history);
        tvTotal = view.findViewById(R.id.tv_hist_total);
        tvSafe = view.findViewById(R.id.tv_hist_safe);
        tvDanger = view.findViewById(R.id.tv_hist_danger);

        repo = new HistoryRepository(requireContext());

        setupRecyclerView();
        setupSwipeToDelete();
        
        // UX Polish: Entrance animation
        view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_up));
        
        View btnCta = view.findViewById(R.id.btn_cta_scan);
        if (btnCta != null) {
            btnCta.setOnClickListener(v -> {
                if (getActivity() instanceof com.example.safelink.MainActivity) {
                    ((com.example.safelink.MainActivity) getActivity()).navigateToHome();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(null);
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setAdapter(adapter);
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int position = vh.getAdapterPosition();
                if (position == -1) return;

                HistoryModel item = adapter.getItem(position);
                executor.execute(() -> {
                    repo.delete(item.getId());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            adapter.removeItem(position);
                            checkEmpty();
                        });
                    }
                });
            }
        }).attachToRecyclerView(rvHistory);
    }

    private void loadHistory() {
        executor.execute(() -> {
            List<HistoryModel> list = repo.getAll();
            int total = repo.countAll();
            int safe = repo.countByStatus(ScanResult.STATUS_SAFE);
            int danger = repo.countByStatus(ScanResult.STATUS_DANGEROUS);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (tvTotal != null) tvTotal.setText(String.valueOf(total));
                    if (tvSafe != null) tvSafe.setText(String.valueOf(safe));
                    if (tvDanger != null) tvDanger.setText(String.valueOf(danger));
                    
                    adapter.updateData(list);
                    checkEmpty();
                });
            }
        });
    }

    private void checkEmpty() {
        boolean empty = adapter.getItemCount() == 0;
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
