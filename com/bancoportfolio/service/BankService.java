package com.bancoportfolio.service;

import com.bancoportfolio.model.Account;
import com.bancoportfolio.model.Transaction;
import com.bancoportfolio.repository.AccountRepository;
import com.bancoportfolio.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BankService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BankService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Account createAccount(String customerName) {
        Account account = new Account();
        account.setNumber(UUID.randomUUID().toString().substring(0, 10));
        account.setCustomerName(customerName);
        return accountRepository.save(account);
    }

    @Transactional
    public Account deposit(Long accountId, BigDecimal amount) {
        Account account = findAccount(accountId);

        account.setBalance(account.getBalance().add(amount));

        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setType(Transaction.TransactionType.DEPOSIT);
        tx.setAccount(account);

        transactionRepository.save(tx);
        return accountRepository.save(account);
    }

    @Transactional
    public Account withdraw(Long accountId, BigDecimal amount) {
        Account account = findAccount(accountId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Saldo insuficiente");
        }

        account.setBalance(account.getBalance().subtract(amount));

        Transaction tx = new Transaction();
        tx.setAmount(amount.negate());
        tx.setType(Transaction.TransactionType.WITHDRAW);
        tx.setAccount(account);

        transactionRepository.save(tx);
        return accountRepository.save(account);
    }

    @Transactional
    @SuppressWarnings("null")
    public void transfer(Long fromId, Long toId, BigDecimal amount) {

        if (fromId.equals(toId)) {
            throw new RuntimeException("Contas iguais");
        }

        Account from = findAccount(fromId);
        Account to = findAccount(toId);

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Saldo insuficiente");
        }

        // Atualiza saldo
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        // Transação origem
        Transaction txFrom = new Transaction();
        txFrom.setAmount(amount.negate());
        txFrom.setType(Transaction.TransactionType.TRANSFER);
        txFrom.setFromAccountId(fromId);
        txFrom.setToAccountId(toId);
        txFrom.setAccount(from);

        // Transação destino
        Transaction txTo = new Transaction();
        txTo.setAmount(amount);
        txTo.setType(Transaction.TransactionType.TRANSFER);
        txTo.setFromAccountId(fromId);
        txTo.setToAccountId(toId);
        txTo.setAccount(to);

        transactionRepository.save(txFrom);
        transactionRepository.save(txTo);

        accountRepository.save(from);
        accountRepository.save(to);
    }

    public Optional<Account> getAccount(Long id) {
        return accountRepository.findById(id);
    }

    public BigDecimal getBalance(Long id) {
        return getAccount(id)
                .map(Account::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactions(Long accountId) {
        return transactionRepository.findByAccountIdOrderByTimestampDesc(accountId);
    }

    private Account findAccount(Long id) {
        return getAccount(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
    }
}