package com.foxishangxian.ebank.ui.card;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.foxishangxian.ebank.R;
import com.foxishangxian.ebank.data.BankCard;
import com.foxishangxian.ebank.data.UserDatabase;
import com.foxishangxian.ebank.data.UserDao;
import com.foxishangxian.ebank.data.User;
import com.foxishangxian.ebank.ui.ToastUtil;
import java.util.List;
import java.util.ArrayList;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CardManageActivity extends AppCompatActivity implements CardManageAdapter.OnCardClickListener {
    private RecyclerView recyclerView;
    private CardManageAdapter adapter;
    private List<BankCard> cardList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_manage);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.recycler_cards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CardManageAdapter(cardList, this);
        recyclerView.setAdapter(adapter);
        
        // 设置添加银行卡按钮点击事件
        MaterialButton btnAddCard = findViewById(R.id.btn_add_card);
        btnAddCard.setOnClickListener(v -> addNewCard());
        
        loadCards();
    }

    private void loadCards() {
        AsyncTask.execute(() -> {
            UserDao userDao = UserDatabase.getInstance(this).userDao();
            User user = userDao.getLoggedInUser();
            if (user != null) {
                List<BankCard> cards = UserDatabase.getInstance(this).bankCardDao().getCardsByUserId(user.uid);
                runOnUiThread(() -> {
                    cardList.clear();
                    cardList.addAll(cards);
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    @Override
    public void onCardClick(BankCard card) {
        Intent intent = new Intent(this, CardDetailActivity.class);
        intent.putExtra("cardId", card.id);
        startActivity(intent);
    }

    private void addNewCard() {
        // 显示提示消息
        ToastUtil.show(this, "由于本软件只是模拟软件，没有真正导入银行卡的功能，故自动添加了一张空银行卡");
        
        AsyncTask.execute(() -> {
            UserDao userDao = UserDatabase.getInstance(this).userDao();
            User user = userDao.getLoggedInUser();
            if (user != null) {
                // 生成新的银行卡信息
                String cardType = "储蓄卡";
                String cardNumber = generateCardNumber();
                double balance = 0.0; // 空银行卡，余额为0
                String startDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                String endDate = "2029-12-31";
                double limitPerDay = 5000.0;
                
                BankCard newCard = new BankCard(user.uid, cardType, cardNumber, balance, startDate, endDate, limitPerDay, user.phone, "");
                UserDatabase.getInstance(this).bankCardDao().insert(newCard);
                
                runOnUiThread(() -> {
                    // 刷新列表
                    loadCards();
                });
            }
        });
    }

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        sb.append("6222");
        for (int i = 0; i < 12; i++) {
            sb.append((int)(Math.random() * 10));
        }
        return sb.toString();
    }
} 