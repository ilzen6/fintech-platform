package com.example.family_account_service.service;

import com.example.family_account_service.domain.AccountStatus;
import com.example.family_account_service.domain.FamilyAccountDocument;
import com.example.family_account_service.domain.MemberRole;
import com.example.family_account_service.dto.AddMemberRequest;
import com.example.family_account_service.dto.CreateFamilyAccountRequest;
import com.example.family_account_service.dto.FamilyAccountResponse;
import com.example.family_account_service.exception.FamilyAccountNotFoundException;
import com.example.family_account_service.exception.MemberAlreadyExistsException;
import com.example.family_account_service.mapper.FamilyAccountMapper;
import com.example.family_account_service.repository.FamilyAccountCustomRepository;
import com.example.family_account_service.repository.FamilyAccountRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
@DisplayName("FamilyAccountService unit tests")
class FamilyAccountServiceTest {

    // ── Моки всех зависимостей ────────────────────────────────────
    @Mock
    private FamilyAccountRepository accountRepository;

    @Mock
    private FamilyAccountCustomRepository customRepository;

    @Mock
    private FamilyAccountMapper accountMapper;

    // ── Реальный объект который тестируем — Spring инжектирует моки ──
    @InjectMocks
    private FamilyAccountService familyAccountService;

    // ── Общие тестовые данные — инициализируются перед каждым тестом ──
    private CreateFamilyAccountRequest createRequest;
    private AddMemberRequest addMemberRequest;
    private FamilyAccountDocument accountDocument;
    private FamilyAccountDocument savedDocument;
    private FamilyAccountResponse accountResponse;

    @BeforeEach
    void setUp() {


        createRequest = CreateFamilyAccountRequest.builder()
                .ownerId("owner-123")
                .accountName("Семья Петровых")
                .build();

        addMemberRequest = AddMemberRequest.builder()
                .userId("user-456")
                .role(MemberRole.MEMBER)
                .build();

        // Документ ДО сохранения (без id — MongoDB ещё не назначил)
        accountDocument = FamilyAccountDocument.builder()
                .ownerId("owner-123")
                .accountName("Семья Петровых")
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .members(new ArrayList<>())
                .build();

        // Документ ПОСЛЕ сохранения (с id, version, createdAt)
        savedDocument = FamilyAccountDocument.builder()
                .id("account-abc")
                .ownerId("owner-123")
                .accountName("Семья Петровых")
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .members(new ArrayList<>())
                .version(0L)
                .createdAt(Instant.now())
                .build();

        accountResponse = FamilyAccountResponse.builder()
                .id("account-abc")
                .ownerId("owner-123")
                .accountName("Семья Петровых")
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .memberCount(0)
                .createdAt(savedDocument.getCreatedAt())
                .build();
    }

    // ══════════════════════════════════════════════════════════════
    // createAccount
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("createAccount")
    class CreateAccountTests {

        @Test
        @DisplayName("valid request — maps, saves and returns response")
        void createAccount_validRequest_savesAndReturnsResponse() {
            // Given
            Mockito.when(accountMapper.toDocument(createRequest)).thenReturn(accountDocument);
            Mockito.when(accountRepository.save(accountDocument)).thenReturn(savedDocument);
            Mockito.when(accountMapper.toResponse(savedDocument)).thenReturn(accountResponse);

            // When
            FamilyAccountResponse result = familyAccountService.createAccount(createRequest);

            // Then
            Assertions.assertNotNull(result);
            Assertions.assertEquals("account-abc", result.getId());
            Assertions.assertEquals("owner-123", result.getOwnerId());
            Assertions.assertEquals(AccountStatus.ACTIVE, result.getStatus());
            Assertions.assertEquals(BigDecimal.ZERO, result.getBalance());
        }

        @Test
        @DisplayName("valid request — save called with correct document")
        void createAccount_validRequest_savesCorrectDocument() {
            // Given
            Mockito.when(accountMapper.toDocument(createRequest)).thenReturn(accountDocument);
            Mockito.when(accountRepository.save(ArgumentMatchers.any())).thenReturn(savedDocument);
            Mockito.when(accountMapper.toResponse(ArgumentMatchers.any())).thenReturn(accountResponse);

            // When
            familyAccountService.createAccount(createRequest);

            // Then — проверяем что именно передали в save
            // ArgumentCaptor позволяет захватить аргумент и проверить его поля
            ArgumentCaptor<FamilyAccountDocument> captor =
                    ArgumentCaptor.forClass(FamilyAccountDocument.class);
            Mockito.verify(accountRepository).save(captor.capture());

            FamilyAccountDocument captured = captor.getValue();
            Assertions.assertEquals("owner-123", captured.getOwnerId());
            Assertions.assertEquals(AccountStatus.ACTIVE, captured.getStatus());
            Assertions.assertEquals(BigDecimal.ZERO, captured.getBalance());
        }

        @Test
        @DisplayName("mapper and repository called exactly once")
        void createAccount_validRequest_callsMapperAndRepositoryOnce() {
            // Given
            Mockito.when(accountMapper.toDocument(createRequest)).thenReturn(accountDocument);
            Mockito.when(accountRepository.save(accountDocument)).thenReturn(savedDocument);
            Mockito.when(accountMapper.toResponse(savedDocument)).thenReturn(accountResponse);

            // When
            familyAccountService.createAccount(createRequest);

            // Then — verify что каждый компонент вызван ровно 1 раз
            Mockito.verify(accountMapper, Mockito.times(1)).toDocument(createRequest);
            Mockito.verify(accountRepository, Mockito.times(1)).save(accountDocument);
            Mockito.verify(accountMapper, Mockito.times(1)).toResponse(savedDocument);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // getAccount
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getAccount")
    class GetAccountTests {

        @Test
        @DisplayName("existing account — returns response")
        void getAccount_existingAccount_returnsResponse() {
            // Given
            Mockito.when(accountRepository.findByIdAndOwnerId("account-abc", "owner-123"))
                    .thenReturn(Optional.of(savedDocument));
            Mockito.when(accountMapper.toResponse(savedDocument)).thenReturn(accountResponse);

            // When
            FamilyAccountResponse result =
                    familyAccountService.getAccount("account-abc", "owner-123");

            // Then
            Assertions.assertNotNull(result);
            Assertions.assertEquals("account-abc", result.getId());
            Assertions.assertEquals("owner-123", result.getOwnerId());
        }

        @Test
        @DisplayName("non-existent account — throws FamilyAccountNotFoundException")
        void getAccount_notFound_throwsFamilyAccountNotFoundException() {
            // Given
            Mockito.when(accountRepository.findByIdAndOwnerId("bad-id", "owner-123"))
                    .thenReturn(Optional.empty());

            // When + Then
            FamilyAccountNotFoundException exception = Assertions.assertThrows(
                    FamilyAccountNotFoundException.class,
                    () -> familyAccountService.getAccount("bad-id", "owner-123")
            );

            // Проверяем что сообщение содержит id аккаунта
            Assertions.assertTrue(exception.getMessage().contains("bad-id"));
            // Mapper не должен вызываться если аккаунт не найден
            Mockito.verifyNoInteractions(accountMapper);
        }

        @Test
        @DisplayName("wrong ownerId — throws FamilyAccountNotFoundException")
        void getAccount_wrongOwnerId_throwsFamilyAccountNotFoundException() {
            // Given — аккаунт существует но принадлежит другому owner
            Mockito.when(accountRepository.findByIdAndOwnerId("account-abc", "wrong-owner"))
                    .thenReturn(Optional.empty());

            // When + Then
            Assertions.assertThrows(
                    FamilyAccountNotFoundException.class,
                    () -> familyAccountService.getAccount("account-abc", "wrong-owner")
            );
        }
    }

    // ══════════════════════════════════════════════════════════════
    // addMember
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("addMember")
    class AddMemberTests {

        @Test
        @DisplayName("new member — adds successfully and returns updated document")
        void addMember_newMember_addsSuccessfullyAndReturnsUpdated() {
            // Given
            // Первый findByIdAndOwnerId — для проверки дублей
            Mockito.when(accountRepository.findByIdAndOwnerId("account-abc", "owner-123"))
                    .thenReturn(Optional.of(savedDocument))
                    // Второй вызов — после addMember, читаем свежий документ
                    .thenReturn(Optional.of(savedDocument));

            Mockito.when(customRepository.addMember(ArgumentMatchers.eq("account-abc"),
                            ArgumentMatchers.any(FamilyAccountDocument.Member.class)))
                    .thenReturn(true);

            Mockito.when(accountMapper.toResponse(savedDocument)).thenReturn(accountResponse);

            // When
            FamilyAccountResponse result =
                    familyAccountService.addMember("account-abc", "owner-123", addMemberRequest);

            // Then
            Assertions.assertNotNull(result);
            // Проверяем что addMember вызван с правильными аргументами
            ArgumentCaptor<FamilyAccountDocument.Member> memberCaptor =
                    ArgumentCaptor.forClass(FamilyAccountDocument.Member.class);
            Mockito.verify(customRepository).addMember(ArgumentMatchers.eq("account-abc"), memberCaptor.capture());

            FamilyAccountDocument.Member capturedMember = memberCaptor.getValue();
            Assertions.assertEquals("user-456", capturedMember.getUserId());
            Assertions.assertEquals(MemberRole.MEMBER, capturedMember.getRole());
            Assertions.assertNotNull(capturedMember.getJoinedAt());
        }

        @Test
        @DisplayName("member already exists — throws MemberAlreadyExistsException")
        void addMember_memberAlreadyExists_throwsMemberAlreadyExistsException() {
            // Given — документ уже содержит этого пользователя как члена
            FamilyAccountDocument.Member existingMember = FamilyAccountDocument.Member.builder()
                    .userId("user-456")  // тот же userId что в addMemberRequest
                    .role(MemberRole.MEMBER)
                    .joinedAt(Instant.now())
                    .build();

            FamilyAccountDocument documentWithMember = FamilyAccountDocument.builder()
                    .id("account-abc")
                    .ownerId("owner-123")
                    .status(AccountStatus.ACTIVE)
                    .members(new ArrayList<>(List.of(existingMember)))
                    .build();

            Mockito.when(accountRepository.findByIdAndOwnerId("account-abc", "owner-123"))
                    .thenReturn(Optional.of(documentWithMember));

            // When + Then
            MemberAlreadyExistsException exception = Assertions.assertThrows(
                    MemberAlreadyExistsException.class,
                    () -> familyAccountService.addMember("account-abc", "owner-123", addMemberRequest)
            );

            Assertions.assertTrue(exception.getMessage().contains("user-456"));
            // customRepository.addMember не должен вызываться
            Mockito.verify(customRepository, Mockito.never()).addMember(ArgumentMatchers.anyString(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("account not found — throws FamilyAccountNotFoundException")
        void addMember_accountNotFound_throwsFamilyAccountNotFoundException() {
            // Given
            Mockito.when(accountRepository.findByIdAndOwnerId("bad-id", "owner-123"))
                    .thenReturn(Optional.empty());

            // When + Then
            Assertions.assertThrows(
                    FamilyAccountNotFoundException.class,
                    () -> familyAccountService.addMember("bad-id", "owner-123", addMemberRequest)
            );

            Mockito.verify(customRepository, Mockito.never()).addMember(ArgumentMatchers.anyString(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("customRepository returns false — throws FamilyAccountNotFoundException")
        void addMember_customRepositoryReturnsFalse_throwsFamilyAccountNotFoundException() {
            // Given — документ найден но findAndModify не нашёл (race condition)
            Mockito.when(accountRepository.findByIdAndOwnerId("account-abc", "owner-123"))
                    .thenReturn(Optional.of(savedDocument));

            Mockito.when(customRepository.addMember(ArgumentMatchers.eq("account-abc"),
                            ArgumentMatchers.any(FamilyAccountDocument.Member.class)))
                    .thenReturn(false); // findAndModify вернул null

            // When + Then
            Assertions.assertThrows(
                    FamilyAccountNotFoundException.class,
                    () -> familyAccountService.addMember("account-abc", "owner-123", addMemberRequest)
            );
        }
    }

    // ══════════════════════════════════════════════════════════════
    // closeAccount
    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("closeAccount")
    class CloseAccountTests {

        @Test
        @DisplayName("active account — changes status to CLOSED and saves")
        void closeAccount_activeAccount_changesStatusToClosedAndSaves() {
            // Given
            Mockito.when(accountRepository.findByIdAndOwnerId("account-abc", "owner-123"))
                    .thenReturn(Optional.of(savedDocument));
            Mockito.when(accountRepository.save(ArgumentMatchers.any())).thenReturn(savedDocument);

            // When
            familyAccountService.closeAccount("account-abc", "owner-123");

            // Then — проверяем что сохранили документ со статусом CLOSED
            ArgumentCaptor<FamilyAccountDocument> captor =
                    ArgumentCaptor.forClass(FamilyAccountDocument.class);
            Mockito.verify(accountRepository).save(captor.capture());

            Assertions.assertEquals(AccountStatus.CLOSED, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("already closed account — idempotent, save NOT called")
        void closeAccount_alreadyClosed_idempotentSaveNotCalled() {
            // Given — счёт уже закрыт
            FamilyAccountDocument closedDocument = FamilyAccountDocument.builder()
                    .id("account-abc")
                    .ownerId("owner-123")
                    .status(AccountStatus.CLOSED) // уже закрыт
                    .members(new ArrayList<>())
                    .build();

            Mockito.when(accountRepository.findByIdAndOwnerId("account-abc", "owner-123"))
                    .thenReturn(Optional.of(closedDocument));

            // When — повторный вызов не должен падать
            Assertions.assertDoesNotThrow(
                    () -> familyAccountService.closeAccount("account-abc", "owner-123")
            );

            // Then — save не вызывается, идемпотентность соблюдена
            Mockito.verify(accountRepository, Mockito.never()).save(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("account not found — throws FamilyAccountNotFoundException")
        void closeAccount_notFound_throwsFamilyAccountNotFoundException() {
            // Given
            Mockito.when(accountRepository.findByIdAndOwnerId("bad-id", "owner-123"))
                    .thenReturn(Optional.empty());

            // When + Then
            Assertions.assertThrows(
                    FamilyAccountNotFoundException.class,
                    () -> familyAccountService.closeAccount("bad-id", "owner-123")
            );

            Mockito.verify(accountRepository, Mockito.never()).save(ArgumentMatchers.any());
        }
    }
}