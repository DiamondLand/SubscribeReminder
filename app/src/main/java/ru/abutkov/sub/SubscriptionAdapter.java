package ru.abutkov.sub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import ru.abutkov.sub.entity.SubEntity;

public class SubscriptionAdapter extends ArrayAdapter<SubEntity> {

    public interface OnSubscriptionClickListener {
        void onSubscriptionClick(SubEntity subscription);
    }

    private final OnSubscriptionClickListener listener;

    public SubscriptionAdapter(Context context, List<SubEntity> subscriptions, OnSubscriptionClickListener listener) {
        super(context, 0, subscriptions);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_subscription, parent, false);
        }

        SubEntity subscription = getItem(position);

        TextView tvServiceName = convertView.findViewById(R.id.tvServiceName);
        TextView tvPaymentDate = convertView.findViewById(R.id.tvPaymentDate);
        TextView tvPaymentAmount = convertView.findViewById(R.id.tvPaymentAmount);
        TextView tvFrequency = convertView.findViewById(R.id.tvFrequency);

        tvServiceName.setText(subscription.getServiceName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        tvPaymentDate.setText("Дата платежа: " + dateFormat.format(subscription.getPaymentDate()));
        tvPaymentAmount.setText(String.format("Сумма: %.2f ₽", subscription.getPaymentAmount()));
        tvFrequency.setText(subscription.getFrequency().equals("MONTHLY") ? "Ежемесячная" : "Ежегодная");

        // Установка обработчика клика для каждого элемента
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSubscriptionClick(subscription);
            }
        });

        return convertView;
    }
}
