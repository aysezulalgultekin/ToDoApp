package com.example.todoappdemo;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {
    private final List<String> days;
    private final OnDayClickListener onDayClickListener;
    private int selectedPosition = -1;

    public CalendarAdapter(List<String> days, OnDayClickListener onDayClickListener) {
        this.days = days;
        this.onDayClickListener = onDayClickListener;
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;

        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_day_item, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String day = days.get(position);
        holder.tvDay.setText(day);

        if (day.isEmpty()) {
            holder.tvDay.setTextColor(Color.GRAY);
            holder.tvDay.setBackgroundResource(0);
            holder.itemView.setClickable(false);
        } else {
            holder.tvDay.setTextColor(Color.BLACK);

            if (position == selectedPosition) {
                holder.tvDay.setBackgroundResource(R.drawable.bg_circle_selected);
            } else {
                holder.tvDay.setBackgroundResource(R.drawable.bg_circle_normal);
            }

            holder.itemView.setOnClickListener(v -> {
                int previousPosition = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);

                onDayClickListener.onDayClick(day);
            });
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_day);
        }
    }

    public interface OnDayClickListener {
        void onDayClick(String day);
    }
}


