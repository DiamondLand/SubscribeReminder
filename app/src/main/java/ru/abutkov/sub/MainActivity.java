package ru.abutkov.sub;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import android.app.AlertDialog;
import android.widget.EditText;
import android.view.LayoutInflater;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import ru.abutkov.sub.database.AppDatabase;
import ru.abutkov.sub.entity.SubEntity;
import android.widget.Spinner;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements SubscriptionAdapter.OnSubscriptionClickListener {
    private ListView subscriptionListView;
    private SubscriptionAdapter subscriptionAdapter;
    private List<SubEntity> subscriptionList;
    private AppDatabase db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "subscription-database")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        subscriptionListView = findViewById(R.id.subscriptionListView);
        subscriptionList = new ArrayList<>(db.subscriptionDao().getAllSubscriptions());

        subscriptionAdapter = new SubscriptionAdapter(this, subscriptionList, this);
        subscriptionListView.setAdapter(subscriptionAdapter);

        updateSubscriptionDates();

        FloatingActionButton addSubscriptionButton = findViewById(R.id.addSubscriptionButton);
        addSubscriptionButton.setOnClickListener(v -> showAddSubscriptionDialog());
    }

    private void addSubscription(SubEntity subscription) {
        db.subscriptionDao().insertSubscription(subscription);
        subscriptionList.add(subscription);

        if (subscriptionAdapter != null) {
            subscriptionAdapter.notifyDataSetChanged();
        }
    }

    private void updateSubscriptionDates() {
        Date currentDate = new Date();
        boolean isUpdated = false;

        for (SubEntity subscription : subscriptionList) {
            if (subscription.getPaymentDate().before(currentDate)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(subscription.getPaymentDate());

                if (subscription.getFrequency().equals("MONTHLY")) {
                    calendar.add(Calendar.MONTH, 1);
                } else if (subscription.getFrequency().equals("YEARLY")) {
                    calendar.add(Calendar.YEAR, 1);
                }

                subscription.setPaymentDate(calendar.getTime());
                db.subscriptionDao().updateSubscription(subscription);
                isUpdated = true;
            }
        }

        if (isUpdated) {
            subscriptionList.clear();
            subscriptionList.addAll(db.subscriptionDao().getAllSubscriptions());
            subscriptionAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSubscriptionClick(SubEntity subscription) {
        showEditSubscriptionDialog(subscription);
    }

    private void showAddSubscriptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить подписку");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_subscription, null);
        builder.setView(view);

        EditText editTextServiceName = view.findViewById(R.id.editTextServiceName);
        EditText editTextPaymentDate = view.findViewById(R.id.editTextPaymentDate);
        EditText editTextPaymentAmount = view.findViewById(R.id.editTextPaymentAmount);
        Spinner spinnerFrequency = view.findViewById(R.id.spinnerFrequency);

        editTextPaymentDate.setOnClickListener(v -> showDatePickerDialog(editTextPaymentDate));

        AlertDialog dialog = builder.create();

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            String serviceName = editTextServiceName.getText().toString().trim();
            String paymentDateString = editTextPaymentDate.getText().toString().trim();
            String paymentAmountString = editTextPaymentAmount.getText().toString().trim();

            boolean hasError = false;
            if (serviceName.isEmpty()) {
                editTextServiceName.setError("Заполните поле");
                hasError = true;
            }
            if (paymentDateString.isEmpty()) {
                editTextPaymentDate.setError("Заполните поле");
                hasError = true;
            }
            if (paymentAmountString.isEmpty()) {
                editTextPaymentAmount.setError("Заполните поле");
                hasError = true;
            }

            if (hasError) return;

            Date paymentDate;
            try {
                paymentDate = dateFormat.parse(paymentDateString);
            } catch (ParseException e) {
                paymentDate = new Date();
            }

            double paymentAmount;
            try {
                paymentAmount = Double.parseDouble(paymentAmountString.replace(",", "."));
            } catch (NumberFormatException e) {
                paymentAmount = 0.0;
            }

            String frequency = spinnerFrequency.getSelectedItem().toString().equals("Ежемесячно") ? "MONTHLY" : "YEARLY";

            SubEntity newSubscription = new SubEntity(serviceName, paymentDate, paymentAmount, frequency);
            addSubscription(newSubscription);
            dialog.dismiss();
        });

        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showEditSubscriptionDialog(SubEntity subscription) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактировать подписку");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_subscription, null);
        builder.setView(view);

        EditText editTextServiceName = view.findViewById(R.id.editTextServiceName);
        EditText editTextPaymentDate = view.findViewById(R.id.editTextPaymentDate);
        EditText editTextPaymentAmount = view.findViewById(R.id.editTextPaymentAmount);
        Spinner spinnerFrequency = view.findViewById(R.id.spinnerFrequency);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.frequency_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);

        if (subscription.getFrequency().equals("MONTHLY")) {
            spinnerFrequency.setSelection(0);
        } else {
            spinnerFrequency.setSelection(1);
        }

        editTextServiceName.setText(subscription.getServiceName());
        editTextPaymentDate.setText(dateFormat.format(subscription.getPaymentDate()));
        editTextPaymentAmount.setText(String.format(Locale.getDefault(), "%.2f", subscription.getPaymentAmount()));

        editTextPaymentDate.setOnClickListener(v -> showDatePickerDialog(editTextPaymentDate));

        AlertDialog dialog = builder.create();

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            String serviceName = editTextServiceName.getText().toString().trim();
            String paymentDateString = editTextPaymentDate.getText().toString().trim();
            String paymentAmountString = editTextPaymentAmount.getText().toString().trim();

            boolean hasError = false;
            if (serviceName.isEmpty()) {
                editTextServiceName.setError("Заполните поле");
                hasError = true;
            }
            if (paymentDateString.isEmpty()) {
                editTextPaymentDate.setError("Заполните поле");
                hasError = true;
            }
            if (paymentAmountString.isEmpty()) {
                editTextPaymentAmount.setError("Заполните поле");
                hasError = true;
            }

            if (hasError) return;

            try {
                subscription.setPaymentDate(dateFormat.parse(paymentDateString));
            } catch (ParseException e) {
                subscription.setPaymentDate(new Date());
            }

            try {
                subscription.setPaymentAmount(Double.parseDouble(paymentAmountString.replace(",", ".")));
            } catch (NumberFormatException e) {
                subscription.setPaymentAmount(0.0);
            }

            subscription.setServiceName(serviceName);
            String frequency = (spinnerFrequency.getSelectedItemPosition() == 0) ? "MONTHLY" : "YEARLY";
            subscription.setFrequency(frequency);

            db.subscriptionDao().updateSubscription(subscription);
            subscriptionAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        Button deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            db.subscriptionDao().deleteSubscription(subscription);
            subscriptionList.remove(subscription);
            subscriptionAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            editText.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
