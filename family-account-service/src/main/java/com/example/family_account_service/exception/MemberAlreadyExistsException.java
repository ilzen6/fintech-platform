package com.example.family_account_service.exception;

public class MemberAlreadyExistsException extends RuntimeException {
    public MemberAlreadyExistsException(String userId) {
        super("Member with id:" + userId + " already exists");
    }
}
