package com.foxishangxian.ebank.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.foxishangxian.ebank.R;
import com.foxishangxian.ebank.data.NewsItem;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_NEWS = 0;
    private static final int VIEW_TYPE_LOADING = 1;
    private static final int VIEW_TYPE_NO_MORE = 2;

    private List<NewsItem> newsList;
    private OnNewsClickListener listener;
    private boolean isLoadingMore = false;
    private boolean showNoMore = false;

    public interface OnNewsClickListener {
        void onNewsClick(NewsItem newsItem);
    }

    public NewsAdapter(List<NewsItem> newsList, OnNewsClickListener listener) {
        this.newsList = newsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_NEWS) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_news, parent, false);
            return new NewsViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading_more, parent, false);
            return new LoadingViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_no_more, parent, false);
            return new NoMoreViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NewsViewHolder) {
            NewsItem newsItem = newsList.get(position);
            ((NewsViewHolder) holder).bind(newsItem);
        }
        // LoadingViewHolder和NoMoreViewHolder不需要绑定数据
    }

    @Override
    public int getItemCount() {
        int count = newsList.size();
        if (isLoadingMore) count += 1;
        if (showNoMore) count += 1;
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == newsList.size() && isLoadingMore) {
            return VIEW_TYPE_LOADING;
        } else if (position == newsList.size() && showNoMore) {
            return VIEW_TYPE_NO_MORE;
        }
        return VIEW_TYPE_NEWS;
    }

    public void setLoadingMore(boolean loading) {
        if (this.isLoadingMore != loading) {
            this.isLoadingMore = loading;
            if (loading) {
                showNoMore = false;
                notifyItemInserted(newsList.size());
            } else {
                notifyItemRemoved(newsList.size());
            }
        }
    }

    public void setNoMore(boolean noMore) {
        if (this.showNoMore != noMore) {
            this.showNoMore = noMore;
            if (noMore) {
                isLoadingMore = false;
                notifyItemInserted(newsList.size());
            } else {
                notifyItemRemoved(newsList.size());
            }
        }
    }

    public void updateNews(List<NewsItem> newNewsList) {
        this.newsList = newNewsList;
        notifyDataSetChanged();
    }

    public void addNews(List<NewsItem> moreNews) {
        int startPosition = newsList.size();
        newsList.addAll(moreNews);
        notifyItemRangeInserted(startPosition, moreNews.size());
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvSource;
        private TextView tvTime;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_news_title);
            tvDescription = itemView.findViewById(R.id.tv_news_description);
            tvSource = itemView.findViewById(R.id.tv_news_source);
            tvTime = itemView.findViewById(R.id.tv_news_time);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < newsList.size() && listener != null) {
                    listener.onNewsClick(newsList.get(position));
                }
            });
        }

        public void bind(NewsItem newsItem) {
            tvTitle.setText(newsItem.getTitle());
            tvDescription.setText(newsItem.getDescription());
            tvSource.setText(newsItem.getSource());
            tvTime.setText(newsItem.getPublishedAt());
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class NoMoreViewHolder extends RecyclerView.ViewHolder {
        public NoMoreViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
} 