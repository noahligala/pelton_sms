package com.example.peltondata.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.peltondata.models.CheckInOutRecord;
import com.example.peltondata.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckInOutAdapter extends RecyclerView.Adapter<CheckInOutAdapter.CheckInOutViewHolder> {

    private List<CheckInOutRecord> checkInOutRecords;

    public CheckInOutAdapter(List<CheckInOutRecord> checkInOutRecords) {
        this.checkInOutRecords = checkInOutRecords;
    }

    @NonNull
    @Override
    public CheckInOutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_check_in_out, parent, false);
        return new CheckInOutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckInOutViewHolder holder, int position) {
        CheckInOutRecord record = checkInOutRecords.get(position);

        if (record.getAction().equals("Check In")) {
            holder.checkInTextView.setText(record.getAction());
            holder.checkInTimestampTextView.setText(record.getTimestamp());
            holder.checkOutTextView.setText("");
            holder.checkOutTimestampTextView.setText("");
        } else if (record.getAction().equals("Check Out")) {
            holder.checkOutTextView.setText(record.getAction());
            holder.checkOutTimestampTextView.setText(record.getTimestamp());
            holder.checkInTextView.setText("");
            holder.checkInTimestampTextView.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return checkInOutRecords.size();
    }

    public void setRecords(List<CheckInOutRecord> records) {
        this.checkInOutRecords = mergeRecords(records);
        notifyDataSetChanged();
    }

    private List<CheckInOutRecord> mergeRecords(List<CheckInOutRecord> records) {
        List<CheckInOutRecord> mergedRecords = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        CheckInOutRecord checkInRecord = null;

        for (CheckInOutRecord record : records) {
            try {
                Date currentDate = dateFormat.parse(dateFormat.format(timestampFormat.parse(record.getTimestamp())));

                if (record.getAction().equals("Check In")) {
                    checkInRecord = record;
                } else if (record.getAction().equals("Check Out") && checkInRecord != null) {
                    mergedRecords.add(new CheckInOutRecord("Check In/Out",
                            checkInRecord.getTimestamp() + " / " + record.getTimestamp()));
                    checkInRecord = null;
                } else {
                    mergedRecords.add(record);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (checkInRecord != null) {
            mergedRecords.add(checkInRecord);
        }

        return mergedRecords;
    }

    public static class CheckInOutViewHolder extends RecyclerView.ViewHolder {
        TextView checkInTextView;
        TextView checkInTimestampTextView;
        TextView checkOutTextView;
        TextView checkOutTimestampTextView;

        public CheckInOutViewHolder(@NonNull View itemView) {
            super(itemView);
            checkInTextView = itemView.findViewById(R.id.check_in_text_view);
            checkInTimestampTextView = itemView.findViewById(R.id.check_in_timestamp_text_view);
            checkOutTextView = itemView.findViewById(R.id.check_out_text_view);
            checkOutTimestampTextView = itemView.findViewById(R.id.check_out_timestamp_text_view);
        }
    }
}
