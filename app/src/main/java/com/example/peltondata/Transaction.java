package com.example.peltondata;

import java.io.Serializable;

public class Transaction implements Serializable {
    private String transactionCode;
    private double transactionAmount;
    private String transactionDate;
    private String transactionTime;
    private String customerID;

    public Transaction(String transactionCode, double transactionAmount, String transactionDate, String transactionTime, String customerID) {
        this.transactionCode = transactionCode;
        this.transactionAmount = transactionAmount;
        this.transactionDate = transactionDate;
        this.transactionTime = transactionTime;
        this.customerID = customerID;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public String getCustomerID() {
        return customerID;
    }
}
