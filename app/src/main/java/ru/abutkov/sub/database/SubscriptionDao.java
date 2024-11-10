package ru.abutkov.sub.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import ru.abutkov.sub.entity.SubEntity;

@Dao
public interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions ORDER BY paymentDate ASC")
    List<SubEntity> getAllSubscriptions();

    @Insert
    void insertSubscription(SubEntity subscription);

    @Update
    void updateSubscription(SubEntity subscription);

    @Delete
    void deleteSubscription(SubEntity subscription);

    // Метод для удаления всех записей из таблицы
    @Query("DELETE FROM subscriptions")
    void deleteAllSubscriptions();
}
