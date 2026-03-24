package com.example.family_account_service.exception;

public class FamilyAccountNotFoundException extends RuntimeException {
    public FamilyAccountNotFoundException(String accountId) {
        super("Account with id:" + accountId + " not found");
    }
}
