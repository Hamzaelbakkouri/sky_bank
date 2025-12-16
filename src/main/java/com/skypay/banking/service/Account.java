package com.skypay.banking.service;

import com.skypay.banking.exception.InsufficientFundsException;
import com.skypay.banking.exception.InvalidAmountException;
import com.skypay.banking.model.Transaction;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Account implements AccountService {
    private static final String STATEMENT_HEADER = "Date       || Amount || Balance";

    private int balance;
    private final List<Transaction> transactions;
    private final PrintStream printer;

    public Account() {
        this(System.out);
    }

    public Account(PrintStream printer) {
        this.balance = 0;
        this.transactions = new ArrayList<>();
        this.printer = printer;
    }

    @Override
    public void deposit(int amount) {
        deposit(amount, LocalDate.now());
    }

    public void deposit(int amount, LocalDate date) {
        validateAmount(amount);
        balance += amount;
        transactions.add(new Transaction(date, amount, balance));
    }

    @Override
    public void withdraw(int amount) {
        withdraw(amount, LocalDate.now());
    }

    public void withdraw(int amount, LocalDate date) {
        validateAmount(amount);
        validateSufficientFunds(amount);
        balance -= amount;
        transactions.add(new Transaction(date, -amount, balance));
    }

    @Override
    public void printStatement() {
        printer.println(STATEMENT_HEADER);
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction transaction = transactions.get(i);
            printer.println(formatTransaction(transaction));
        }
    }

    public int getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be positive. Received: " + amount);
        }
    }

    private void validateSufficientFunds(int amount) {
        if (amount > balance) {
            throw new InsufficientFundsException(
                "Insufficient funds. Requested: " + amount + ", Available: " + balance
            );
        }
    }

    private String formatTransaction(Transaction transaction) {
        return String.format("%s || %d    || %d",
            transaction.getFormattedDate(),
            transaction.getAmount(),
            transaction.getBalance()
        );
    }
}
