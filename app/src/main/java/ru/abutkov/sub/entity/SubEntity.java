package ru.abutkov.sub.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "subscriptions")
public class SubEntity {

    @PrimaryKey(autoGenerate = true)
    private int id; // Первичный ключ, генерируемый автоматически

    private String serviceName;
    private Date paymentDate;
    private double paymentAmount;
    private String frequency; // "MONTHLY" или "YEARLY"

    public SubEntity(String serviceName, Date paymentDate, double paymentAmount, String frequency) {
        this.serviceName = serviceName;
        this.paymentDate = paymentDate;
        this.paymentAmount = paymentAmount;
        this.frequency = frequency;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public double getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(double paymentAmount) { this.paymentAmount = paymentAmount; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
}
