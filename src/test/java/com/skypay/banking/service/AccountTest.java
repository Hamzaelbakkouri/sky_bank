package com.skypay.banking.service;

import com.skypay.banking.exception.InsufficientFundsException;
import com.skypay.banking.exception.InvalidAmountException;
import com.skypay.banking.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintStream;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountTest {

    @Mock
    private PrintStream mockPrinter;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account(mockPrinter);
    }

    @Nested
    @DisplayName("Deposit tests")
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
    @DisplayName("Withdraw tests")
    class WithdrawTests {

        @Test
        @DisplayName("Should decrease balance when withdrawing")
        void shouldDecreaseBalanceWhenWithdrawing() {
            account.deposit(1000);
            account.withdraw(500);
            assertEquals(500, account.getBalance());
        }

        @Test
        @DisplayName("Should record withdrawal as negative amount in transaction")
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
    @DisplayName("Print statement tests")
    class PrintStatementTests {

        @Test
        @DisplayName("Should print header")
        void shouldPrintHeader() {
            account.printStatement();

            verify(mockPrinter).println("Date       || Amount || Balance");
        }

        @Test
        @DisplayName("Should print transactions in reverse chronological order")
        void shouldPrintTransactionsInReverseOrder() {
            account.deposit(1000, LocalDate.of(2012, 1, 10));
            account.deposit(2000, LocalDate.of(2012, 1, 13));
            account.withdraw(500, LocalDate.of(2012, 1, 14));

            account.printStatement();

            InOrder inOrder = inOrder(mockPrinter);
            inOrder.verify(mockPrinter).println("Date       || Amount || Balance");
            inOrder.verify(mockPrinter).println(contains("14/01/2012"));
            inOrder.verify(mockPrinter).println(contains("13/01/2012"));
            inOrder.verify(mockPrinter).println(contains("10/01/2012"));
        }

        @Test
        @DisplayName("Should print correct amounts for each transaction")
        void shouldPrintCorrectAmounts() {
            account.deposit(1000, LocalDate.of(2012, 1, 10));
            account.deposit(2000, LocalDate.of(2012, 1, 13));
            account.withdraw(500, LocalDate.of(2012, 1, 14));

            account.printStatement();

            verify(mockPrinter).println(contains("-500"));
            verify(mockPrinter).println(contains("2000"));
            verify(mockPrinter, times(2)).println(contains("1000"));
        }

        @Test
        @DisplayName("Should print correct balances for each transaction")
        void shouldPrintCorrectBalances() {
            account.deposit(1000, LocalDate.of(2012, 1, 10));
            account.deposit(2000, LocalDate.of(2012, 1, 13));
            account.withdraw(500, LocalDate.of(2012, 1, 14));

            account.printStatement();

            verify(mockPrinter).println(contains("2500"));
            verify(mockPrinter).println(contains("3000"));
        }

        @Test
        @DisplayName("Acceptance test - should match expected output format")
        void acceptanceTest() {
            account.deposit(1000, LocalDate.of(2012, 1, 10));
            account.deposit(2000, LocalDate.of(2012, 1, 13));
            account.withdraw(500, LocalDate.of(2012, 1, 14));

            account.printStatement();

            InOrder inOrder = inOrder(mockPrinter);
            inOrder.verify(mockPrinter).println("Date       || Amount || Balance");
            inOrder.verify(mockPrinter).println("14/01/2012 || -500    || 2500");
            inOrder.verify(mockPrinter).println("13/01/2012 || 2000    || 3000");
            inOrder.verify(mockPrinter).println("10/01/2012 || 1000    || 1000");
            verifyNoMoreInteractions(mockPrinter);
        }

        @Test
        @DisplayName("Should print only header when no transactions")
        void shouldPrintOnlyHeaderWhenNoTransactions() {
            account.printStatement();

            verify(mockPrinter).println("Date       || Amount || Balance");
            verifyNoMoreInteractions(mockPrinter);
        }
    }

    @Nested
    @DisplayName("Transaction tests")
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
