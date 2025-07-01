package com.foxishangxian.ebank.ui;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.foxishangxian.ebank.R;
import java.util.*;
import com.foxishangxian.ebank.data.*;
import java.text.SimpleDateFormat;
import android.view.View;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.LayoutInflater;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class TransferRecordActivity extends AppCompatActivity {
    private MaterialAutoCompleteTextView autoCard, autoType, autoSort;
    private TextInputEditText etStartDate, etEndDate;
    private EditText etMinAmount, etMaxAmount;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TransferRecordAdapter adapter;
    private long startDateMillis = Long.MIN_VALUE, endDateMillis = Long.MAX_VALUE;
    private Map<String, String> maskedCardMap = new HashMap<>(); // 屏蔽卡号->真实卡号
    private List<String> maskedCardList = new ArrayList<>(); // 用于弹窗卡号下拉
    private MaterialButton btnFilter;
    // 用于保存筛选条件
    private String filterCardMasked = "全部", filterType = "全部", filterSort = "时间倒序", filterMinAmount = "", filterMaxAmount = "", filterStartDate = "", filterEndDate = "";
    private long filterStartMillis = Long.MIN_VALUE, filterEndMillis = Long.MAX_VALUE;
    private TextView tvDateRange;
    private int pageSize = 20;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private List<TransferRecordAdapter.RecordDisplayItem> allDisplayList = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_record);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        btnFilter = findViewById(R.id.btn_filter);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.recycler_records);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransferRecordAdapter();
        recyclerView.setAdapter(adapter);
        btnFilter.setOnClickListener(v -> showFilterDialog());
        // 首次加载和卡号下拉初始化
        initCardDropdownAndLoad();
        tvDateRange = findViewById(R.id.tv_date_range);
        // 分页加载监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (!rv.canScrollVertically(1) && hasMore && !isLoading) {
                    loadNextPage();
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            hasMore = true;
            allDisplayList.clear();
            loadRecords();
        });
    }

    private void showDatePicker(boolean isStart) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(isStart ? "选择起始日期" : "选择结束日期");
        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (isStart) {
                startDateMillis = selection;
                etStartDate.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(selection)));
            } else {
                endDateMillis = selection + 24*60*60*1000 - 1; // 包含当天
                etEndDate.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(selection)));
            }
            loadRecords();
        });
        picker.show(getSupportFragmentManager(), isStart ? "start_date_picker" : "end_date_picker");
    }

    private void initCardDropdownAndLoad() {
        new Thread(() -> {
            UserDatabase db = UserDatabase.getInstance(this);
            UserDao userDao = db.userDao();
            BankCardDao cardDao = db.bankCardDao();
            User user = userDao.getLoggedInUser();
            List<String> maskedList = new ArrayList<>();
            maskedList.add("全部");
            maskedCardMap.clear();
            if (user != null) {
                List<BankCard> cards = cardDao.getCardsByUserId(user.uid);
                for (BankCard card : cards) {
                    String masked = maskCard(card.cardNumber);
                    maskedList.add(masked);
                    maskedCardMap.put(masked, card.cardNumber);
                }
            }
            maskedCardList = maskedList;
            runOnUiThread(this::loadRecords);
        }).start();
    }

    private String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) return cardNumber;
        return cardNumber.substring(0, 4) + " **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_transfer_record_filter, null);
        dialog.setContentView(view);
        // 绑定控件
        MaterialAutoCompleteTextView autoCardD = view.findViewById(R.id.auto_card);
        MaterialAutoCompleteTextView autoTypeD = view.findViewById(R.id.auto_type);
        MaterialAutoCompleteTextView autoSortD = view.findViewById(R.id.auto_sort);
        TextInputEditText etMinAmountD = view.findViewById(R.id.et_min_amount);
        TextInputEditText etMaxAmountD = view.findViewById(R.id.et_max_amount);
        TextInputEditText etStartDateD = view.findViewById(R.id.et_start_date);
        TextInputEditText etEndDateD = view.findViewById(R.id.et_end_date);
        MaterialButton btnReset = view.findViewById(R.id.btn_reset);
        MaterialButton btnConfirm = view.findViewById(R.id.btn_confirm);
        // 初始化下拉内容
        autoTypeD.setSimpleItems(new String[]{"全部", "仅收入", "仅支出"});
        autoSortD.setSimpleItems(new String[]{"时间倒序", "时间正序"});
        // 卡号下拉内容
        autoCardD.setSimpleItems(maskedCardList.toArray(new String[0]));
        // 同步当前筛选条件到弹窗
        autoCardD.setText(filterCardMasked, false);
        autoTypeD.setText(filterType, false);
        autoSortD.setText(filterSort, false);
        etMinAmountD.setText(filterMinAmount);
        etMaxAmountD.setText(filterMaxAmount);
        etStartDateD.setText(filterStartDate);
        etEndDateD.setText(filterEndDate);
        // 日期选择
        etStartDateD.setOnClickListener(v -> showDatePickerDialog(etStartDateD, true));
        etEndDateD.setOnClickListener(v -> showDatePickerDialog(etEndDateD, false));
        // 重置按钮
        btnReset.setOnClickListener(v -> {
            autoCardD.setText("全部", false);
            autoTypeD.setText("全部", false);
            autoSortD.setText("时间倒序", false);
            etMinAmountD.setText("");
            etMaxAmountD.setText("");
            etStartDateD.setText("");
            etEndDateD.setText("");
        });
        // 确定按钮
        btnConfirm.setOnClickListener(v -> {
            filterCardMasked = autoCardD.getText() != null ? autoCardD.getText().toString() : "全部";
            filterType = autoTypeD.getText() != null ? autoTypeD.getText().toString() : "全部";
            filterSort = autoSortD.getText() != null ? autoSortD.getText().toString() : "时间倒序";
            filterMinAmount = etMinAmountD.getText() != null ? etMinAmountD.getText().toString() : "";
            filterMaxAmount = etMaxAmountD.getText() != null ? etMaxAmountD.getText().toString() : "";
            filterStartDate = etStartDateD.getText() != null ? etStartDateD.getText().toString() : "";
            filterEndDate = etEndDateD.getText() != null ? etEndDateD.getText().toString() : "";
            // 解析日期
            filterStartMillis = parseDateMillis(filterStartDate, true);
            filterEndMillis = parseDateMillis(filterEndDate, false);
            dialog.dismiss();
            loadRecords();
        });
        dialog.show();
    }

    private String[] getAdapterItems(MaterialAutoCompleteTextView auto) {
        int count = auto.getAdapter() != null ? auto.getAdapter().getCount() : 0;
        String[] arr = new String[count];
        for (int i = 0; i < count; i++) arr[i] = auto.getAdapter().getItem(i).toString();
        return arr;
    }

    private void showDatePickerDialog(TextInputEditText et, boolean isStart) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(isStart ? "选择起始日期" : "选择结束日期");
        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            et.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(selection)));
        });
        picker.show(getSupportFragmentManager(), isStart ? "start_date_picker_dialog" : "end_date_picker_dialog");
    }

    private long parseDateMillis(String date, boolean isStart) {
        try {
            if (date == null || date.isEmpty()) return isStart ? Long.MIN_VALUE : Long.MAX_VALUE;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.util.Date d = sdf.parse(date);
            if (d == null) return isStart ? Long.MIN_VALUE : Long.MAX_VALUE;
            if (!isStart) return d.getTime() + 24*60*60*1000 - 1;
            return d.getTime();
        } catch (Exception e) { return isStart ? Long.MIN_VALUE : Long.MAX_VALUE; }
    }

    private void loadRecords() {
        isLoading = true;
        swipeRefreshLayout.setRefreshing(true);
        new Thread(() -> {
            UserDatabase db = UserDatabase.getInstance(this);
            UserDao userDao = db.userDao();
            BankCardDao cardDao = db.bankCardDao();
            TransferRecordDao recordDao = db.transferRecordDao();
            User user = userDao.getLoggedInUser();
            if (user == null) {
                runOnUiThread(() -> {
                    adapter.setData(new ArrayList<>());
                    swipeRefreshLayout.setRefreshing(false);
                    ToastUtil.show(this, "请先登录");
                });
                return;
            }
            // 获取所有卡号-用户名映射
            Map<String, String> cardUserNameMap = new HashMap<>();
            List<BankCard> allCards = cardDao.getAllCards();
            for (BankCard card : allCards) {
                User u = userDao.getUserByUid(card.userId);
                cardUserNameMap.put(card.cardNumber, u != null ? u.username : card.cardNumber);
            }
            // 获取当前用户所有银行卡号
            List<BankCard> cards = cardDao.getCardsByUserId(user.uid);
            Set<String> myCardNumbers = new HashSet<>();
            for (BankCard card : cards) {
                myCardNumbers.add(card.cardNumber);
            }
            // 查询所有转账记录
            List<TransferRecord> allRecords = recordDao.getAllRecords();
            // 读取筛选条件
            String cardSel = maskedCardMap.getOrDefault(filterCardMasked, "全部");
            int typeSel = 0;
            if ("仅收入".equals(filterType)) typeSel = 1;
            else if ("仅支出".equals(filterType)) typeSel = 2;
            String minStr = filterMinAmount.trim();
            String maxStr = filterMaxAmount.trim();
            double minAmount = minStr.isEmpty() ? Double.MIN_VALUE : Double.parseDouble(minStr);
            double maxAmount = maxStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxStr);
            int sortSel = "时间正序".equals(filterSort) ? 1 : 0;
            long startMillis = filterStartMillis;
            long endMillis = filterEndMillis;
            // 多条件过滤
            List<TransferRecord> filtered = new ArrayList<>();
            for (TransferRecord r : allRecords) {
                boolean isMine = myCardNumbers.contains(r.fromCard) || myCardNumbers.contains(r.toCard);
                if (!isMine) continue;
                boolean isIncome = myCardNumbers.contains(r.toCard);
                // 卡号
                if (!"全部".equals(cardSel) && !(r.fromCard.equals(cardSel) || r.toCard.equals(cardSel))) continue;
                // 类型
                if (typeSel == 1 && !isIncome) continue;
                if (typeSel == 2 && isIncome) continue;
                // 金额区间
                if (r.amount < minAmount || r.amount > maxAmount) continue;
                // 时间段
                if (r.time < startMillis || r.time > endMillis) continue;
                filtered.add(r);
            }
            final int finalSortSel = sortSel;
            filtered.sort((a, b) -> finalSortSel == 0 ? Long.compare(b.time, a.time) : Long.compare(a.time, b.time));
            // 按天分组
            SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy年MM月dd日", java.util.Locale.getDefault());
            Map<String, List<TransferRecord>> groupMap = new LinkedHashMap<>();
            for (TransferRecord r : filtered) {
                String day = sdfDay.format(new Date(r.time));
                if (!groupMap.containsKey(day)) groupMap.put(day, new ArrayList<>());
                groupMap.get(day).add(r);
            }
            // 组装展示数据
            List<TransferRecordAdapter.RecordDisplayItem> displayList = new ArrayList<>();
            for (String day : groupMap.keySet()) {
                displayList.add(new TransferRecordAdapter.RecordDisplayItem(day));
                for (TransferRecord r : groupMap.get(day)) {
                    boolean isIncome = myCardNumbers.contains(r.toCard);
                    String targetCard = isIncome ? r.fromCard : r.toCard;
                    String targetName = cardUserNameMap.getOrDefault(targetCard, targetCard);
                    String timeStr = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new Date(r.time));
                    displayList.add(new TransferRecordAdapter.RecordDisplayItem(isIncome, r.amount, timeStr, targetName, targetCard));
                }
            }
            // 只保留第一页数据
            List<TransferRecordAdapter.RecordDisplayItem> pageList = new ArrayList<>();
            int count = 0;
            for (TransferRecordAdapter.RecordDisplayItem item : displayList) {
                pageList.add(item);
                if (!item.isSection) count++;
                if (count >= pageSize) break;
            }
            allDisplayList = new ArrayList<>(displayList); // 保存全部，后续分页用
            hasMore = count < displayList.size();
            currentPage = 1;
            runOnUiThread(() -> {
                adapter.setCardUserNameMap(cardUserNameMap);
                adapter.setData(pageList);
                adapter.setHasMore(hasMore);
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                // 设置日期区间显示
                String range;
                if (filterStartDate.isEmpty() && filterEndDate.isEmpty()) {
                    range = "全部日期";
                } else if (!filterStartDate.isEmpty() && !filterEndDate.isEmpty()) {
                    range = filterStartDate + " ~ " + filterEndDate;
                } else if (!filterStartDate.isEmpty()) {
                    range = filterStartDate + " ~ ";
                } else {
                    range = " ~ " + filterEndDate;
                }
                tvDateRange.setText(range);
            });
        }).start();
    }

    private void loadNextPage() {
        if (isLoading || !hasMore) return;
        isLoading = true;
        new Thread(() -> {
            // 取下一页数据
            List<TransferRecordAdapter.RecordDisplayItem> nextPage = new ArrayList<>();
            int count = 0, realCount = 0;
            for (int i = 0, n = 0; i < allDisplayList.size() && n < (currentPage + 1) * pageSize; i++) {
                TransferRecordAdapter.RecordDisplayItem item = allDisplayList.get(i);
                if (!item.isSection) n++;
                if (n > currentPage * pageSize) {
                    nextPage.add(item);
                    if (!item.isSection) count++;
                }
            }
            hasMore = (currentPage + 1) * pageSize < getRealItemCount(allDisplayList);
            currentPage++;
            runOnUiThread(() -> {
                adapter.appendData(nextPage);
                adapter.setHasMore(hasMore);
                isLoading = false;
            });
        }).start();
    }

    private int getRealItemCount(List<TransferRecordAdapter.RecordDisplayItem> list) {
        int c = 0;
        for (TransferRecordAdapter.RecordDisplayItem item : list) if (!item.isSection) c++;
        return c;
    }
} 