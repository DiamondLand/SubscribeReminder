package ru.abutkov.sub.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import ru.abutkov.sub.entity.SubEntity;

@Database(entities = {SubEntity.class}, version = 1)
@TypeConverters({Converters.class}) // для поддержки типов данных, таких как Date
public abstract class AppDatabase extends RoomDatabase {
    public abstract SubscriptionDao subscriptionDao();
}
