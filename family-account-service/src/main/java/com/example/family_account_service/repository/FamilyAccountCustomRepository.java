package com.example.family_account_service.repository;

import com.example.family_account_service.domain.FamilyAccountDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;


import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FamilyAccountCustomRepository {
    private final MongoTemplate mongoTemplate;

    public Optional<FamilyAccountDocument> acquireLock(String accountId, String instanceId) {
        Query query = Query.query(
                new Criteria().andOperator(
                        Criteria.where("_id").is(accountId),
                        new Criteria().orOperator(
                            Criteria.where("lockedBy").exists(false),
                            Criteria.where("lockedBy").is(null),
                            Criteria.where("lockExpiresAt").lt(Instant.now())

                        )
                )
        );

        Update update = new Update()
                .set("lockedBy", instanceId)
                .set("lockedAt", Instant.now())
                .set("lockExpiresAt", Instant.now().plusSeconds(30));

        FamilyAccountDocument result = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options()
                        .upsert(false)
                        .returnNew(true),
                FamilyAccountDocument.class
        );
        if (result == null) {
            log.warn("Could not acquire lock on accountId={} — already locked", accountId);
        } else {
            log.debug("Lock acquired on accountId={} by instanceId={}", accountId, instanceId);
        }
        return Optional.ofNullable(result);
    }

    public boolean releaseLock(String accountId, String instanceId) {
        Query query = Query.query(
                Criteria.where("_id").is(accountId)
                        .and("lockedBy").is(instanceId)
        );

        Update update = new Update()
                .unset("lockedBy")
                .unset("lockedAt")
                .unset("lockExpiresAt");

        FamilyAccountDocument document = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options()
                        .upsert(false).returnNew(true),
                FamilyAccountDocument.class
        );

        boolean released = document != null;

        if (!released) {
            log.warn("Lock not released — accountId={} not locked by instanceId={}",
                    accountId, instanceId);
        } else {
            log.debug("Lock released on accountId={}", accountId);
        }
        return released;
    }


    public boolean addMember(String accountId, FamilyAccountDocument.Member member) {
        Query query = Query.query(Criteria.where("_id").is(accountId));
        Update update = new Update()
                .push("members", member);

        FamilyAccountDocument document = mongoTemplate.findAndModify(query,
                update, FindAndModifyOptions.options().upsert(false).returnNew(true),
                FamilyAccountDocument.class);
        log.debug("Member userId={} added to accountId={}", member.getUserId(), accountId);
        return document != null;

    }






}
