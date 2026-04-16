# 💳 Bank API

A simple RESTful banking API built with Spring Boot. This project simulates basic banking operations such as account creation, deposits, withdrawals, transfers, and transaction history tracking.

---

## 🚀 Technologies Used

* Java 11+
* Spring Boot
* Spring Data JPA
* Hibernate
* H2 Database (in-memory)
* Maven

---

## 📁 Project Structure Overview

### 📌 Controller Layer

#### `BankController.java`

This class is responsible for handling HTTP requests and exposing REST endpoints for the API.

**Main Features:**

* Create a new account
* Retrieve account details
* Deposit and withdraw money
* Transfer funds between accounts
* Get account balance
* Retrieve transaction history

**Endpoints:**

| Method | Endpoint                          | Description               |
| ------ | --------------------------------- | ------------------------- |
| POST   | `/api/accounts`                   | Create a new account      |
| GET    | `/api/accounts/{id}`              | Get account details       |
| POST   | `/api/accounts/{id}/deposit`      | Deposit money             |
| POST   | `/api/accounts/{id}/withdraw`     | Withdraw money            |
| POST   | `/api/transfer`                   | Transfer between accounts |
| GET    | `/api/accounts/{id}/balance`      | Get account balance       |
| GET    | `/api/accounts/{id}/transactions` | Get transaction history   |

**Validation & Error Handling:**

* Uses `@Valid` for request validation
* Handles business errors like:

  * Account not found
  * Insufficient balance
* Returns appropriate HTTP status codes

---

### 📌 Service Layer

#### `BankService.java`

This class contains the core business logic of the application. It is annotated with `@Service` and manages all banking operations using the repository layer.

**Main Features:**

* Create new bank accounts with a randomly generated account number
* Deposit money into an account and record the transaction
* Withdraw money with insufficient balance validation
* Transfer funds between two distinct accounts with balance and identity validation
* Retrieve account details and current balance
* Fetch transaction history ordered by most recent first

**Methods:**

| Method                                              | Description                                           |
| --------------------------------------------------- | ----------------------------------------------------- |
| `createAccount(String customerName)`                | Creates and persists a new account                    |
| `deposit(Long accountId, BigDecimal amount)`        | Adds funds to an account and records a DEPOSIT        |
| `withdraw(Long accountId, BigDecimal amount)`       | Deducts funds from an account and records a WITHDRAW  |
| `transfer(Long fromId, Long toId, BigDecimal amount)` | Transfers funds between accounts and records both sides as TRANSFER |
| `getAccount(Long id)`                               | Returns an `Optional<Account>` by ID                  |
| `getBalance(Long id)`                               | Returns the current balance, defaulting to zero       |
| `getTransactions(Long accountId)`                   | Returns a list of transactions ordered by timestamp   |

**Business Rules & Validations:**

* Throws `RuntimeException("Saldo insuficiente")` if withdrawal or transfer amount exceeds available balance
* Throws `RuntimeException("Contas iguais")` if origin and destination accounts are the same in a transfer
* Throws `RuntimeException("Conta não encontrada")` if the account ID does not exist
* All write operations are wrapped in `@Transactional` to ensure data consistency
* Transfer creates two separate transaction records — one for the sender and one for the receiver

---

### 📌 Model Layer

#### `Account.java`

Represents a bank account entity.

**Fields:**

* `id` → Unique identifier
* `number` → Unique account number
* `customerName` → Account owner's name
* `balance` → Current account balance
* `transactions` → List of related transactions

**Key Features:**

* Uses JPA annotations for persistence
* One-to-many relationship with transactions
* Lazy loading for transaction list

---

#### `Transaction.java`

Represents a financial transaction.

**Fields:**

* `id` → Unique identifier
* `amount` → Transaction amount
* `type` → Transaction type (DEPOSIT, WITHDRAW, TRANSFER)
* `timestamp` → Date and time of the transaction
* `fromAccountId` → Source account (for transfers)
* `toAccountId` → Destination account (for transfers)
* `account` → Associated account

**Key Features:**

* Many-to-one relationship with `Account`
* Uses `@JsonIgnore` to prevent circular serialization
* Enum-based transaction type

---

### 📌 Repository Layer

#### `AccountRepository.java`

Handles database operations for accounts.

**Features:**

* Extends `JpaRepository`
* Provides CRUD operations
* Custom method:

  * `findByNumber(String number)`

---

#### `TransactionRepository.java`

Handles database operations for transactions.

**Features:**

* Extends `JpaRepository`
* Custom query to fetch transactions by account:

```java
SELECT t FROM Transaction t WHERE t.account.id = :accountId ORDER BY t.timestamp DESC
```

* Returns transactions ordered by most recent first

---

## ⚙️ How to Run the Project

### 1. Clone the repository

```bash
git clone https://github.com/your-username/bank-api.git
cd bank-api
```

### 2. Run the application

```bash
mvn spring-boot:run
```

### 3. Access the API

```
http://localhost:9999
```

### 4. Access H2 Database Console

```
http://localhost:9999/h2-console
```

---

## ⚠️ Notes

* The project uses an in-memory database (data is lost when the application stops)
* Make sure no other service is using port `9999`
* Transactions are loaded lazily, so service layer should handle transactional context properly

---

## 📌 Future Improvements

* Add authentication (JWT / Spring Security)
* Implement pagination for transactions
* Add DTO layer for better API design
* Improve error handling with custom exceptions
* Add unit and integration tests

---

## 👨‍💻 Author

Pedro — Backend Developer in progress 🚀
