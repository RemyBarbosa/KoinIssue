package fr.radiofrance.alarmdemo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import fr.radiofrance.alarmdemo.R;
import fr.radiofrance.alarmdemo.listener.OnAlarmActionListener;
import fr.radiofrance.alarmdemo.model.AlarmModel;

public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.ViewHolder> {

    private final Context context;
    private final List<AlarmModel> alarms;
    private final OnAlarmActionListener onAlarmActionListener;

    public AlarmsAdapter(final Context context,
                         @NonNull final List<AlarmModel> alarms,
                         final OnAlarmActionListener onAlarmActionListener) {
        this.context = context;
        this.alarms = alarms;
        this.onAlarmActionListener = onAlarmActionListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_alarm, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (!alarms.isEmpty() && holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < alarms.size()) {
            final AlarmModel alarm = alarms.get(holder.getAdapterPosition());
            holder.timeTextView.setText(context.getString(R.string.alarm_time, getGoodTimeNumber(alarm.getHours()), getGoodTimeNumber(alarm.getMinutes())));
            holder.daysTextView.setText(daysToString(alarm.getDays()));
            holder.activateAlarmSwitch.setChecked(alarm.isActivated());
            holder.activateAlarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (onAlarmActionListener != null) {
                        onAlarmActionListener.onAlarmActivated(alarm, isChecked, holder.getAdapterPosition());
                    }
                }

            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (onAlarmActionListener != null) {
                        onAlarmActionListener.onAlarmClick(alarm, holder.getAdapterPosition());
                    }
                }

            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View view) {
                    if (onAlarmActionListener != null) {
                        onAlarmActionListener.onAlarmLongClick(alarm, holder.getAdapterPosition());
                    }
                    return true;
                }

            });
        }
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public List<AlarmModel> getAlarms() {
        return alarms;
    }

    public void setAlarms(final List<AlarmModel> alarms) {
        if (alarms == null || alarms.isEmpty()) {
            return;
        }

        this.alarms.clear();
        this.alarms.addAll(alarms);
        this.notifyDataSetChanged();
    }

    public void addAlarm(final AlarmModel alarm) {
        if (alarm == null) {
            return;
        }

        alarms.add(alarm);
        notifyItemInserted(alarms.size() - 1);
    }

    public void removeAlarm(final AlarmModel alarm) {
        if (alarm == null || !alarms.contains(alarm)) {
            return;
        }

        final int alarmPosition = alarms.indexOf(alarm);
        alarms.remove(alarmPosition);
        notifyItemRemoved(alarmPosition);
    }

    private String daysToString(final List<Integer> days) {
        String daysAsString = "";
        for (final Integer day : days) {
            daysAsString += "Day #" + day + " ";
        }

        return daysAsString;
    }

    private String getGoodTimeNumber(int number) {
        return String.format(Locale.getDefault(), (number < 10 ? "0" : "") + "%d", number);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView timeTextView;
        private final TextView daysTextView;
        private final SwitchCompat activateAlarmSwitch;

        ViewHolder(final View itemView) {
            super(itemView);

            timeTextView = (TextView) itemView.findViewById(R.id.alarm_time);
            daysTextView = (TextView) itemView.findViewById(R.id.alarm_days);
            activateAlarmSwitch = (SwitchCompat) itemView.findViewById(R.id.alarm_activate);
        }

    }

}
