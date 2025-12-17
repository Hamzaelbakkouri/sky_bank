package com.skypay.banking.service;

import com.skypay.banking.exception.InsufficientFundsException;
import com.skypay.banking.exception.InvalidAmountException;
import com.skypay.banking.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private ByteArrayOutputStream outputStream;
    private Account account;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        account = new Account(printStream);
    }

    private String getOutput() {
        return outputStream.toString();
    }

    @Nested
    @DisplayName("Deposit Tests")
    class DepositTests {

        @Test
        @DisplayName("Should increase balance when depositing")
        void shouldIncreaseBalanceWhenDepositing() {
            account.deposit(1000);
            assertEquals(1000, account.getBalance());
        }

        @Test
        @DisplayName("Should accumulate balance with multiple deposits")
        void shouldAccumulateBalanceWithMultipleDeposits() {
            account.deposit(1000);
            account.deposit(2000);
            assertEquals(3000, account.getBalance());
        }

        @Test
        @DisplayName("Should record transaction when depositing")
        void shouldRecordTransactionWhenDepositing() {
            account.deposit(1000);
            assertEquals(1, account.getTransactions().size());
        }

        @Test
        @DisplayName("Should throw exception for zero deposit")
        void shouldThrowExceptionForZeroDeposit() {
            InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> account.deposit(0)
            );
            assertTrue(exception.getMessage().contains("Amount must be positive"));
        }

        @Test
        @DisplayName("Should throw exception for negative deposit")
        void shouldThrowExceptionForNegativeDeposit() {
            InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> account.deposit(-100)
            );
            assertTrue(exception.getMessage().contains("Amount must be positive"));
        }
    }

    @Nested
    @DisplayName("Withdraw Tests")
    class WithdrawTests {

        @Test
        @DisplayName("Should decrease balance when withdrawing")
        void shouldDecreaseBalanceWhenWithdrawing() {
            account.deposit(1000);
            account.withdraw(500);
            assertEquals(500, account.getBalance());
        }

        @Test
        @DisplayName("Should record withdrawal as negative amount")
        void shouldRecordWithdrawalAsNegativeAmount() {
            account.deposit(1000);
            account.withdraw(500);

            Transaction withdrawal = account.getTransactions().get(1);
            assertEquals(-500, withdrawal.getAmount());
        }

        @Test
        @DisplayName("Should throw exception for zero withdrawal")
        void shouldThrowExceptionForZeroWithdrawal() {
            account.deposit(1000);
            InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> account.withdraw(0)
            );
            assertTrue(exception.getMessage().contains("Amount must be positive"));
        }

        @Test
        @DisplayName("Should throw exception for negative withdrawal")
        void shouldThrowExceptionForNegativeWithdrawal() {
            account.deposit(1000);
            InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> account.withdraw(-100)
            );
            assertTrue(exception.getMessage().contains("Amount must be positive"));
        }

        @Test
        @DisplayName("Should throw exception when insufficient funds")
        void shouldThrowExceptionWhenInsufficientFunds() {
            account.deposit(500);
            InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> account.withdraw(1000)
            );
            assertTrue(exception.getMessage().contains("Insufficient funds"));
        }

        @Test
        @DisplayName("Should allow withdrawal of entire balance")
        void shouldAllowWithdrawalOfEntireBalance() {
            account.deposit(1000);
            account.withdraw(1000);
            assertEquals(0, account.getBalance());
        }
    }

    @Nested
    @DisplayName("Print Statement Tests")
    class PrintStatementTests {

        @Test
        @DisplayName("Should print header")
        void shouldPrintHeader() {
            account.printStatement();

            String output = getOutput();
            assertTrue(output.contains("Date       || Amount || Balance"));
        }

        @Test
        @DisplayName("Should print transactions in reverse chronological order")
        void shouldPrintTransactionsInReverseOrder() {
            account.deposit(1000, LocalDate.of(2012, 1, 10));
            account.deposit(2000, LocalDate.of(2012, 1, 13));
            account.withdraw(500, LocalDate.of(2012, 1, 14));

            account.printStatement();

            String output = getOutput();
            int jan14Index = output.indexOf("14/01/2012");
            int jan13Index = output.indexOf("13/01/2012");
            int jan10Index = output.indexOf("10/01/2012");

            assertTrue(jan14Index < jan13Index);
            assertTrue(jan13Index < jan10Index);
        }

        @Test
        @DisplayName("Should print only header when no transactions")
        void shouldPrintOnlyHeaderWhenNoTransactions() {
            account.printStatement();

            String output = getOutput().trim();
            assertEquals("Date       || Amount || Balance", output);
        }

        @Test
        @DisplayName("Acceptance test - full scenario")
        void acceptanceTest() {
            account.deposit(1000, LocalDate.of(2012, 1, 10));
            account.deposit(2000, LocalDate.of(2012, 1, 13));
            account.withdraw(500, LocalDate.of(2012, 1, 14));

            account.printStatement();

            String output = getOutput();
            assertTrue(output.contains("Date       || Amount || Balance"));
            assertTrue(output.contains("14/01/2012"));
            assertTrue(output.contains("-500"));
            assertTrue(output.contains("2500"));
            assertTrue(output.contains("13/01/2012"));
            assertTrue(output.contains("2000"));
            assertTrue(output.contains("3000"));
            assertTrue(output.contains("10/01/2012"));
            assertTrue(output.contains("1000"));
        }
    }

    @Nested
    @DisplayName("Transaction Tests")
    class TransactionTests {

        @Test
        @DisplayName("Should record correct balance after each transaction")
        void shouldRecordCorrectBalanceAfterEachTransaction() {
            account.deposit(1000, LocalDate.of(2012, 1, 10));
            account.deposit(2000, LocalDate.of(2012, 1, 13));
            account.withdraw(500, LocalDate.of(2012, 1, 14));

            var transactions = account.getTransactions();

            assertEquals(1000, transactions.get(0).getBalance());
            assertEquals(3000, transactions.get(1).getBalance());
            assertEquals(2500, transactions.get(2).getBalance());
        }

        @Test
        @DisplayName("Should format date correctly")
        void shouldFormatDateCorrectly() {
            account.deposit(1000, LocalDate.of(2012, 1, 10));

            Transaction transaction = account.getTransactions().get(0);
            assertEquals("10/01/2012", transaction.getFormattedDate());
        }

        @Test
        @DisplayName("Should return defensive copy of transactions")
        void shouldReturnDefensiveCopyOfTransactions() {
            account.deposit(1000);
            var transactions = account.getTransactions();
            transactions.clear();

            assertEquals(1, account.getTransactions().size());
        }
    }
}
