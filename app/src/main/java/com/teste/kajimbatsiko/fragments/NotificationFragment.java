package com.teste.kajimbatsiko.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.teste.kajimbatsiko.R;
import com.teste.kajimbatsiko.data.database;
import com.teste.kajimbatsiko.data.rooms.NotificationItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private database db;
    private TextView emptyText;
    private ImageView btnBack, btnClear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.notificationRecycler);
        emptyText = view.findViewById(R.id.emptyText);
        btnBack = view.findViewById(R.id.btnBack);
        btnClear = view.findViewById(R.id.btnClear);

        db = database.getDatabase(requireContext());

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnClear.setOnClickListener(v -> {
            new Thread(() -> {
                db.notificationDao().markAllAsRead();
                requireActivity().runOnUiThread(this::loadNotifications);
            }).start();
        });

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        new Thread(() -> {
            List<NotificationItem> notifications = db.notificationDao().getAllNotifications();
            requireActivity().runOnUiThread(() -> {
                if (notifications.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter = new NotificationAdapter(notifications);
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    recyclerView.setAdapter(adapter);
                }
            });
        }).start();
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private List<NotificationItem> items;

        public NotificationAdapter(List<NotificationItem> items) {
            this.items = items;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title, message, time;
            View indicator;

            ViewHolder(View view) {
                super(view);
                icon = view.findViewById(R.id.notifIcon);
                title = view.findViewById(R.id.notifTitle);
                message = view.findViewById(R.id.notifMessage);
                time = view.findViewById(R.id.notifTime);
                indicator = view.findViewById(R.id.unreadIndicator);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.notification_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationItem item = items.get(position);

            holder.icon.setImageResource(item.iconRes);
            holder.title.setText(item.title);
            holder.message.setText(item.message);
            holder.time.setText(formatTime(item.timestamp));
            holder.indicator.setVisibility(item.isRead ? View.INVISIBLE : View.VISIBLE);

            if (item.type.equals("alert")) {
                holder.itemView.setBackgroundColor(0x22FF0000); // Fond rouge léger
            }

            holder.itemView.setOnClickListener(v -> {
                if (!item.isRead) {
                    new Thread(() -> {
                        db.notificationDao().markAsRead(item.id);
                        item.isRead = true;
                        requireActivity().runOnUiThread(() -> notifyItemChanged(position));
                    }).start();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private String formatTime(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH);
            return sdf.format(new Date(timestamp));
        }
    }
}