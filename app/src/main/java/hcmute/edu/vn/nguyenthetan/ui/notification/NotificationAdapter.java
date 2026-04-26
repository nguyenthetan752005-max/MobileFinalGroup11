package hcmute.edu.vn.nguyenthetan.ui.notification;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.core.ThemeColorResolver;
import hcmute.edu.vn.nguyenthetan.databinding.ItemNotificationBinding;

final class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<NotificationItem> items = new ArrayList<>();
    private final OnNotificationClickListener listener;

    interface OnNotificationClickListener {
        void onNotificationClick(@NonNull NotificationItem item);
    }

    NotificationAdapter(@NonNull OnNotificationClickListener listener) {
        this.listener = listener;
    }

    void submitList(@NonNull List<NotificationItem> notifications) {
        items.clear();
        items.addAll(notifications);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemNotificationBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemNotificationBinding binding;

        ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull NotificationItem item, @NonNull OnNotificationClickListener listener) {
            binding.textNotificationTitle.setText(item.getTitle());
            binding.textNotificationBody.setText(item.getBody());
            binding.textNotificationMeta.setText(item.getMeta());
            binding.textNotificationMeta.setVisibility(item.getMeta().trim().isEmpty()
                    ? android.view.View.GONE
                    : android.view.View.VISIBLE);
            binding.viewNotificationUnread.setVisibility(item.isRead()
                    ? android.view.View.GONE
                    : android.view.View.VISIBLE);
            binding.textNotificationTitle.setAlpha(item.isRead() ? 0.78f : 1f);
            binding.textNotificationBody.setAlpha(item.isRead() ? 0.72f : 1f);
            binding.textNotificationMeta.setAlpha(item.isRead() ? 0.72f : 1f);
            binding.cardNotification.setStrokeWidth(item.isRead() ? 1 : 2);
            binding.cardNotification.setStrokeColor(item.isRead()
                    ? ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorBorder)
                    : ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorPrimary));

            int iconRes;
            int accentColor;
            switch (item.getType()) {
                case MOOD:
                    iconRes = android.R.drawable.ic_menu_info_details;
                    accentColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.tt_warning);
                    break;
                case REMINDER:
                    iconRes = android.R.drawable.ic_lock_idle_alarm;
                    accentColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.tt_success);
                    break;
                case REPLY:
                default:
                    iconRes = android.R.drawable.ic_dialog_email;
                    accentColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.tt_info);
                    break;
            }

            binding.imageNotificationIcon.setImageResource(iconRes);
            binding.imageNotificationIcon.setImageTintList(ColorStateList.valueOf(accentColor));
            binding.iconContainer.setCardBackgroundColor(
                    ThemeColorResolver.resolveColor(binding.getRoot().getContext(), R.attr.ttColorPrimaryTint)
            );
            binding.cardNotification.setOnClickListener(v -> listener.onNotificationClick(item));
        }
    }
}
