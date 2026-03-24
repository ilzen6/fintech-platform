package com.example.family_account_service.repository;

import com.example.family_account_service.domain.AccountStatus;
import com.example.family_account_service.domain.FamilyAccountDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyAccountRepository
        extends MongoRepository<FamilyAccountDocument, String> {

    List<FamilyAccountDocument> findByOwnerId(String ownerId);

    Optional<FamilyAccountDocument> findByIdAndOwnerId(String id, String ownerId);

    List<FamilyAccountDocument> findByStatus(AccountStatus status);

    Boolean existsByOwnerIdAndStatus(String ownerId, AccountStatus status);

    Long countByOwnerId(String ownerId);


}
