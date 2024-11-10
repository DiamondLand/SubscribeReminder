package ru.abutkov.sub;

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
import ru.abutkov.sub.database.SubscriptionDao;
import ru.abutkov.sub.entity.SubEntity;
import android.widget.LinearLayout;
import android.widget.Spinner;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SubscriptionAdapter.OnSubscriptionClickListener {
    private ListView subscriptionListView;
    private SubscriptionAdapter subscriptionAdapter;
    private List<SubEntity> subscriptionList;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация базы данных Room
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "subscription-database-2")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        // Инициализация списка подписок
        subscriptionListView = findViewById(R.id.subscriptionListView);
        subscriptionList = new ArrayList<>(db.subscriptionDao().getAllSubscriptions());

        // Настройка адаптера и отображение списка
        subscriptionAdapter = new SubscriptionAdapter(this, subscriptionList, this);


        subscriptionListView.setAdapter(subscriptionAdapter);

        // Теперь вызываем обновление дат подписок
        updateSubscriptionDates();

        // Обработка нажатия кнопки для добавления подписки
        FloatingActionButton addSubscriptionButton = findViewById(R.id.addSubscriptionButton);
        addSubscriptionButton.setOnClickListener(v -> showAddSubscriptionDialog());
    }


    // Метод для добавления новой подписки в базу данных и обновления списка
    private void addSubscription(SubEntity subscription) {
        db.subscriptionDao().insertSubscription(subscription);
        subscriptionList.add(subscription);

        // Убедитесь, что subscriptionAdapter не равен null перед вызовом notifyDataSetChanged()
        if (subscriptionAdapter != null) {
            subscriptionAdapter.notifyDataSetChanged();
        }
    }


    // Метод для обновления дат подписок, если они истекли
    private void updateSubscriptionDates() {
        Date currentDate = new Date();
        boolean isUpdated = false;

        for (SubEntity subscription : subscriptionList) {
            if (subscription.getPaymentDate().before(currentDate)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(subscription.getPaymentDate());

                if (subscription.getFrequency().equals("MONTHLY")) {
                    calendar.add(Calendar.MONTH, 1); // Добавить 1 месяц
                } else if (subscription.getFrequency().equals("YEARLY")) {
                    calendar.add(Calendar.YEAR, 1); // Добавить 1 год
                }

                // Установить новую дату и обновить в базе данных
                subscription.setPaymentDate(calendar.getTime());
                db.subscriptionDao().updateSubscription(subscription);
                isUpdated = true;
            }
        }

        // Если данные обновились, перезагружаем список подписок из базы
        if (isUpdated) {
            subscriptionList.clear();
            subscriptionList.addAll(db.subscriptionDao().getAllSubscriptions());
            subscriptionAdapter.notifyDataSetChanged();
        }
    }


    // Метод для обработки нажатия на элемент подписки (редактирование и удаление)
    @Override
    public void onSubscriptionClick(SubEntity subscription) {
        showEditSubscriptionDialog(subscription);
    }

    private void showAddSubscriptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить подписку");

        // Подключаем макет для диалога
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_subscription, null);
        builder.setView(view);

        EditText editTextServiceName = view.findViewById(R.id.editTextServiceName);
        EditText editTextPaymentDate = view.findViewById(R.id.editTextPaymentDate);
        EditText editTextPaymentAmount = view.findViewById(R.id.editTextPaymentAmount);
        Spinner spinnerFrequency = view.findViewById(R.id.spinnerFrequency); // Инициализация Spinner

        AlertDialog dialog = builder.create();

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            String serviceName = editTextServiceName.getText().toString().trim();
            String paymentDateString = editTextPaymentDate.getText().toString().trim();
            String paymentAmountString = editTextPaymentAmount.getText().toString().trim();

            // Проверка на пустые поля
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

            if (hasError) {
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
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

            // Получаем выбранное значение частоты из Spinner
            String frequency = spinnerFrequency.getSelectedItem().toString().equals("Ежемесячно") ? "MONTHLY" : "YEARLY";

            // Создаем новую подписку и добавляем ее в базу данных
            SubEntity newSubscription = new SubEntity(serviceName, paymentDate, paymentAmount, frequency);
            addSubscription(newSubscription);

            // Закрываем диалог после успешного добавления
            dialog.dismiss();
        });

        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showEditSubscriptionDialog(SubEntity subscription) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактировать подписку");

        // Подключаем макет для диалога редактирования
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_subscription, null);
        builder.setView(view);

        EditText editTextServiceName = view.findViewById(R.id.editTextServiceName);
        EditText editTextPaymentDate = view.findViewById(R.id.editTextPaymentDate);
        EditText editTextPaymentAmount = view.findViewById(R.id.editTextPaymentAmount);
        Spinner spinnerFrequency = view.findViewById(R.id.spinnerFrequency);

        // Устанавливаем адаптер для Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.frequency_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);

        // Устанавливаем текущее значение частоты
        if (subscription.getFrequency().equals("MONTHLY")) {
            spinnerFrequency.setSelection(0);
        } else {
            spinnerFrequency.setSelection(1);
        }

        // Заполняем поля текущими значениями подписки
        editTextServiceName.setText(subscription.getServiceName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        editTextPaymentDate.setText(dateFormat.format(subscription.getPaymentDate()));
        editTextPaymentAmount.setText(String.format(Locale.getDefault(), "%.2f", subscription.getPaymentAmount()));

        // Создаем диалог
        AlertDialog dialog = builder.create();

        // Обработчик для кнопки "Сохранить"
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

            if (hasError) {
                return;
            }

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

            // Получаем выбранное значение частоты
            String frequency = (spinnerFrequency.getSelectedItemPosition() == 0) ? "MONTHLY" : "YEARLY";
            subscription.setFrequency(frequency);

            db.subscriptionDao().updateSubscription(subscription);
            subscriptionAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        // Обработчик для кнопки "Отмена"
        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Обработчик для кнопки "Удалить"
        Button deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            db.subscriptionDao().deleteSubscription(subscription);
            subscriptionList.remove(subscription);
            subscriptionAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.show();
    }


}
