# SkyPay Banking Service

A simple banking application that allows users to deposit, withdraw, and print account statements.

## Features

- **Deposit**: Add funds to your account
- **Withdraw**: Remove funds from your account (with balance validation)
- **Print Statement**: View transaction history in reverse chronological order

## Requirements

- Java 17 or higher
- Maven 3.6+

## Build

```bash
mvn clean compile
```

## Run Tests

```bash
mvn test
```

## Usage

```java
Account account = new Account();

// Make deposits
account.deposit(1000);
account.deposit(2000);

// Make withdrawal
account.withdraw(500);

// Print statement
account.printStatement();
```

### Output Example

```
Date       || Amount || Balance
14/01/2012 || -500    || 2500
13/01/2012 || 2000    || 3000
10/01/2012 || 1000    || 1000
```

## Project Structure

```
src/
├── main/java/com/skypay/banking/
│   ├── Main.java
│   ├── exception/
│   │   ├── InsufficientFundsException.java
│   │   └── InvalidAmountException.java
│   ├── model/
│   │   └── Transaction.java
│   └── service/
│       ├── Account.java
│       └── AccountService.java
└── test/java/com/skypay/banking/
    └── service/
        └── AccountTest.java
```

## API

### Account

| Method | Description |
|--------|-------------|
| `deposit(int amount)` | Deposit funds into the account |
| `deposit(int amount, LocalDate date)` | Deposit funds with a specific date |
| `withdraw(int amount)` | Withdraw funds from the account |
| `withdraw(int amount, LocalDate date)` | Withdraw funds with a specific date |
| `printStatement()` | Print the account statement |
| `getBalance()` | Get the current balance |
| `getTransactions()` | Get a list of all transactions |

### Exceptions

- **InvalidAmountException**: Thrown when deposit/withdraw amount is zero or negative
- **InsufficientFundsException**: Thrown when withdrawal amount exceeds available balance
