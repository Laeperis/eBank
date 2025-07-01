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
import java.util.List;
import java.util.ArrayList;
import androidx.appcompat.widget.Toolbar;

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
} 