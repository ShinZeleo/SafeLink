package com.example.safelink.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safelink.R;
import com.example.safelink.adapters.BookmarkAdapter;
import com.example.safelink.database.BookmarkRepository;
import com.example.safelink.models.BookmarkModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookmarkFragment extends Fragment {

    private RecyclerView rvBookmarks;
    private View layoutEmpty;
    private BookmarkAdapter adapter;
    private BookmarkRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvBookmarks = view.findViewById(R.id.rv_bookmarks);
        layoutEmpty = view.findViewById(R.id.layout_empty_bookmarks);

        repo = new BookmarkRepository(requireContext());

        setupRecyclerView();
        
        view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_up));

        View btnCta = view.findViewById(R.id.btn_cta_scan_bookmark);
        if (btnCta != null) {
            btnCta.setOnClickListener(v -> {
                com.google.android.material.bottomnavigation.BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_nav);
                if (nav != null) nav.setSelectedItemId(R.id.homeFragment);
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookmarks();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void setupRecyclerView() {
        adapter = new BookmarkAdapter(null, new BookmarkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BookmarkModel item) {
                // Open safely
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl()));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Cannot open link", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onMoreClick(BookmarkModel item, View anchor) {
                PopupMenu popup = new PopupMenu(requireContext(), anchor);
                popup.getMenu().add(0, 1, 0, "Copy URL");
                popup.getMenu().add(0, 2, 0, "Delete Bookmark");
                popup.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case 1:
                            ClipboardManager cb = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cb != null) {
                                cb.setPrimaryClip(ClipData.newPlainText("URL", item.getUrl()));
                                Toast.makeText(getContext(), "URL Copied", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        case 2:
                            deleteBookmark(item);
                            return true;
                    }
                    return false;
                });
                popup.show();
            }
        });
        rvBookmarks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBookmarks.setAdapter(adapter);
    }

    private void deleteBookmark(BookmarkModel item) {
        executor.execute(() -> {
            repo.delete(item.getId());
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::loadBookmarks);
            }
        });
    }

    private void loadBookmarks() {
        executor.execute(() -> {
            List<BookmarkModel> list = repo.getAll();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.updateData(list);
                    checkEmpty();
                });
            }
        });
    }

    private void checkEmpty() {
        boolean empty = adapter.getItemCount() == 0;
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvBookmarks.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
