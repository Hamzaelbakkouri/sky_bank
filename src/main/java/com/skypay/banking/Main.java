package com.skypay.banking;

import com.skypay.banking.service.Account;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== SkyPay Banking Service ===\n");

        Account account = new Account();

        account.deposit(1000, LocalDate.of(2012, 1, 10));
        account.deposit(2000, LocalDate.of(2012, 1, 13));
        account.withdraw(500, LocalDate.of(2012, 1, 14));

        account.printStatement();
    }
}
