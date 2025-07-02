package com.foxishangxian.ebank.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.foxishangxian.ebank.R;
import com.foxishangxian.ebank.data.TransferRecord;
import com.foxishangxian.ebank.data.User;
import com.foxishangxian.ebank.data.UserDao;
import com.foxishangxian.ebank.data.UserDatabase;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.os.AsyncTask;

public class AdminTransferAdapter extends RecyclerView.Adapter<AdminTransferAdapter.ViewHolder> {
    private Context context;
    private List<TransferRecord> transferList;
    private OnItemClickListener listener;
    private UserDao userDao;
    private Map<String, String> userCache = new HashMap<>();

    public interface OnItemClickListener {
        void onEditClick(TransferRecord record);
        void onDeleteClick(TransferRecord record);
    }

    public AdminTransferAdapter(Context context, List<TransferRecord> transferList) {
        this.context = context;
        this.transferList = transferList != null ? transferList : new ArrayList<>();
        this.userDao = UserDatabase.getInstance(context).userDao();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<TransferRecord> newTransferList) {
        this.transferList = newTransferList != null ? newTransferList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_transfer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransferRecord record = transferList.get(position);
        
        holder.tvTransferId.setText("转账ID: " + (record.id != null ? record.id.substring(0, Math.min(8, record.id.length())) + "..." : ""));
        
        // 获取转出用户名
        String fromUserId = record.fromUid != null ? record.fromUid : record.fromUserId;
        loadUserNameAsync(fromUserId, holder.tvFromUser, "转出用户: ");
        
        // 获取转入用户名
        String toUserId = record.toUid != null ? record.toUid : record.toUserId;
        loadUserNameAsync(toUserId, holder.tvToUser, "转入用户: ");
        
        holder.tvAmount.setText("金额: ¥" + String.format("%.2f", record.amount));
        holder.tvStatus.setText("状态: " + (record.status != null ? record.status : ""));
        
        // 处理转账时间显示
        String timeDisplay = formatTransferTime(record);
        holder.tvTransferTime.setText("转账时间: " + timeDisplay);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(record);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transferList.size();
    }

    private void loadUserNameAsync(String userId, TextView textView, String prefix) {
        if (userId == null || userId.isEmpty()) {
            textView.setText(prefix + "未知用户");
            return;
        }
        
        // 先从缓存中查找
        if (userCache.containsKey(userId)) {
            textView.setText(prefix + userCache.get(userId));
            return;
        }
        
        // 先显示用户ID，然后异步查询用户名
        textView.setText(prefix + "用户" + userId.substring(0, Math.min(4, userId.length())));
        
        // 异步查询用户名
        AsyncTask.execute(() -> {
            try {
                User user = userDao.getUserByUid(userId);
                if (user != null) {
                    String username = user.username != null ? user.username : "用户" + userId.substring(0, Math.min(4, userId.length()));
                    userCache.put(userId, username);
                    
                    // 在主线程更新UI
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            textView.setText(prefix + username);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private String formatTransferTime(TransferRecord record) {
        // 优先使用transferTime字段（字符串格式）
        if (record.transferTime != null && !record.transferTime.isEmpty()) {
            return record.transferTime;
        }
        
        // 如果transferTime为空，使用time字段（时间戳格式）
        if (record.time > 0) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                return sdf.format(new Date(record.time));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return "未知时间";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransferId, tvFromUser, tvToUser, tvAmount, tvStatus, tvTransferTime;
        MaterialButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransferId = itemView.findViewById(R.id.tv_transfer_id);
            tvFromUser = itemView.findViewById(R.id.tv_from_user);
            tvToUser = itemView.findViewById(R.id.tv_to_user);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTransferTime = itemView.findViewById(R.id.tv_transfer_time);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
} 