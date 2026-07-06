package com.example.luckywheel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.ViewHolder> {

//    private List<String> data;
    private List<Option> data;
    private OnItemClickListener listener;

    // 定義點擊事件介面
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public OptionAdapter(List<Option> data, OnItemClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItem;

        ViewHolder(View view) {
            super(view);
            tvItem = view.findViewById(R.id.tvItem);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Option option = data.get(position);

//        holder.tvItem.setText(data.get(position));
//        holder.tvItem.setText(
//                option.getName() + "  (" + option.getWeight() + "%)"
//        );
        holder.tvItem.setText(
                holder.itemView.getContext().getString(
                        R.string.option_format,
                        option.getName(),
                        option.getWeight()
                )
        );
        // 設定點擊事件（例如點擊項目進行刪除）
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAbsoluteAdapterPosition();

            if (pos != RecyclerView.NO_POSITION){
                listener.onItemClick(pos);
            }

//            if (listener != null) {
//                listener.onItemClick(position);
//            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}