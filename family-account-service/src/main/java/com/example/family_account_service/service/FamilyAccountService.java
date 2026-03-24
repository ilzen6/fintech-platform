package com.example.family_account_service.service;

import com.example.family_account_service.domain.AccountStatus;
import com.example.family_account_service.domain.FamilyAccountDocument;
import com.example.family_account_service.dto.AddMemberRequest;
import com.example.family_account_service.dto.CreateFamilyAccountRequest;
import com.example.family_account_service.dto.FamilyAccountResponse;
import com.example.family_account_service.exception.FamilyAccountNotFoundException;
import com.example.family_account_service.exception.MemberAlreadyExistsException;
import com.example.family_account_service.mapper.FamilyAccountMapper;
import com.example.family_account_service.repository.FamilyAccountCustomRepository;
import com.example.family_account_service.repository.FamilyAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class FamilyAccountService {
    private final FamilyAccountMapper accountMapper;
    private final FamilyAccountRepository accountRepository;
    private final FamilyAccountCustomRepository accountCustomRepository;

    @Transactional
    public FamilyAccountResponse createAccount(CreateFamilyAccountRequest request) {
        log.info("Creating family account for ownerId={}", request.getOwnerId());

        FamilyAccountDocument document = accountMapper.toDocument(request);
        FamilyAccountDocument saved = accountRepository.save(document);

        log.info("Family account created successfully id={} ownerId={}",
                saved.getId(), saved.getOwnerId());
        return accountMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FamilyAccountResponse getAccount(String accountId, String ownerId) {
        log.debug("Fetching account id={} ownerId={}", accountId, ownerId);

        FamilyAccountDocument document = accountRepository
                .findByIdAndOwnerId(accountId, ownerId).orElseThrow(() -> {
                    log.warn("Account not found id={} ownerId={}", accountId, ownerId);
                    return new FamilyAccountNotFoundException(accountId);
                });
        return accountMapper.toResponse(document);
    }

    @Transactional
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2)
    )
    public FamilyAccountResponse addMember(String accountId, String ownerId,
                                           AddMemberRequest request) {
        log.info("Adding member userId={} to accountId={}", request.getUserId(), accountId);

        FamilyAccountDocument document = accountRepository
                .findByIdAndOwnerId(accountId, ownerId)
                .orElseThrow(() -> new FamilyAccountNotFoundException(accountId));


        if (document.getMembers() == null) {
            document.setMembers(new ArrayList<>());
        }

        boolean alreadyMember = document.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(request.getUserId()));
        if (alreadyMember) {
            throw new MemberAlreadyExistsException(request.getUserId());
        }

        FamilyAccountDocument.Member member = FamilyAccountDocument.Member.builder()
                .userId(request.getUserId())
                .role(request.getRole())
                .joinedAt(Instant.now())
                .build();


        boolean added = accountCustomRepository.addMember(accountId, member);
        if (!added) {
            throw new FamilyAccountNotFoundException(accountId);
        }

        FamilyAccountDocument updated = accountRepository
                .findByIdAndOwnerId(accountId, ownerId)
                .orElseThrow(() -> new FamilyAccountNotFoundException(accountId));

        log.info("Member userId={} added to accountId={} total members={}",
                request.getUserId(), accountId, updated.getMembers().size());

        return accountMapper.toResponse(updated);
    }

    @Transactional
    public void closeAccount(String accountId, String ownerId) {
        log.info("Closing account id={} ownerId={}", accountId, ownerId);

        FamilyAccountDocument document = accountRepository
                .findByIdAndOwnerId(accountId, ownerId)
                .orElseThrow(() -> new FamilyAccountNotFoundException(accountId));


        if (document.getStatus() == AccountStatus.CLOSED) {
            log.warn("Account id={} is already closed", accountId);
            return;
        }

        document.setStatus(AccountStatus.CLOSED);
        accountRepository.save(document);

        log.info("Account closed successfully id={} ownerId={}", accountId, ownerId);
    }

}
