package com.example.family_account_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "family_accounts")
@CompoundIndexes(
        @CompoundIndex(name = "idx_ownerId_status",
        def = "{'ownerId': 1, 'status': 1}", background = true)
)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyAccountDocument {

    @Id
    private String id;

    private String ownerId;
    private String accountName;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal balance;
    private AccountStatus status;
    private List<Member> members;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String lockedBy;
    private Instant lockedAt;
    private Instant lockExpiresAt;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Member {
        private String userId;
        private MemberRole role;
        private Instant joinedAt;
    }


}
