package com.bancoportfolio.controller;

import com.bancoportfolio.model.Account;
import com.bancoportfolio.model.Transaction;
import com.bancoportfolio.service.BankService;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody @Valid AccountRequest request) {
        Account account = bankService.createAccount(request.getCustomerName());
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        return bankService.getAccount(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/accounts/{id}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable Long id, @RequestBody @Valid AmountRequest request) {
        return bankService.getAccount(id)
                .map(account -> ResponseEntity.ok(bankService.deposit(id, request.getAmount())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/accounts/{id}/withdraw")
    public ResponseEntity<Account> withdraw(@PathVariable Long id, @RequestBody @Valid AmountRequest request) {
        return bankService.getAccount(id)
                .map(account -> ResponseEntity.ok(bankService.withdraw(id, request.getAmount())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody @Valid TransferRequest request) {
        if (request.getFromId().equals(request.getToId())) {
            return ResponseEntity.badRequest().body("Contas de origem e destino devem ser diferentes");
        }

        bankService.transfer(request.getFromId(), request.getToId(), request.getAmount());
        return ResponseEntity.ok("Transferência realizada");
    }

    @GetMapping("/accounts/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id) {
        return bankService.getAccount(id)
                .map(account -> ResponseEntity.ok(account.getBalance()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable Long id) {
        return bankService.getAccount(id)
                .map(account -> ResponseEntity.ok(bankService.getTransactions(id)))
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() == null ? "Erro inesperado" : ex.getMessage();
        if (message.contains("Conta não encontrada")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        if (message.contains("Saldo insuficiente")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
        String error = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .findFirst()
                .orElse("Dados inválidos");
        return ResponseEntity.badRequest().body(error);
    }

}

class AccountRequest {
    @NotBlank
    private String customerName;

    public AccountRequest() {}

    public AccountRequest(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}

class AmountRequest {
    @NotNull
    @Positive
    private BigDecimal amount;

    public AmountRequest() {}

    public AmountRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

class TransferRequest {
    @NotNull
    private Long fromId;

    @NotNull
    private Long toId;

    @NotNull
    @Positive
    private BigDecimal amount;

    public TransferRequest() {}

    public TransferRequest(Long fromId, Long toId, BigDecimal amount) {
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
    }

    public Long getFromId() {
        return fromId;
    }

    public void setFromId(Long fromId) {
        this.fromId = fromId;
    }

    public Long getToId() {
        return toId;
    }

    public void setToId(Long toId) {
        this.toId = toId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
