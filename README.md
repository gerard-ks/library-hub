# LibraryHub - Système de Gestion de Bibliothèque

> Projet d'apprentissage démontrant l'implémentation d'un **domain riche** avec une architecture moderne pour un backend monolithe Java/Spring Boot.

---

## 📋 Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Architecture](#architecture)
- [Domain Riche](#domain-riche)
- [Choix techniques](#choix-techniques)
- [Structure du projet](#structure-du-projet)
- [Modules](#modules)
- [Règles métier](#règles-métier)
- [Installation](#installation)
- [Tests](#tests)

---

## 🎯 Vue d'ensemble

LibraryHub est un système de gestion de bibliothèque permettant de :
- Gérer les membres et leur capacité d'emprunt
- Gérer le catalogue de livres
- Gérer les emprunts et retours de livres avec gestion des retards

**Ce projet est un backend monolithe** déployé comme une seule application avec une seule base de données.

**L'accent est mis sur un domain riche** avec des règles métier complexes encapsulées dans les entités.

---

## 🏛️ Architecture

### Type d'architecture

**Backend Monolithe avec une approche hybride :**
- ✅ **Clean Architecture** (Uncle Bob)
- ✅ **Architecture Hexagonale** (Ports & Adapters)
- ✅ **Domain-Driven Design Tactique** (DDD)
- ✅ **CQS** (Command Query Separation)

### Pourquoi cette combinaison ?

| Pattern | Apport |
|---------|--------|
| **Clean Architecture** | Séparation en couches concentriques (Domain → Application → Infrastructure → Presentation) |
| **Hexagonale** | Isolation du domaine via Ports (interfaces) et Adapters (implémentations) |
| **DDD Tactique** | Aggregates, Value Objects, Repositories, Bounded Contexts, Domain Services |
| **CQS** | Séparation explicite Commandes (write) / Queries (read) |

---

## 💎 Domain Riche

### Les 3 Aggregates

```
┌────────────────────────────────────────────────────────┐
│                   DOMAIN LIBRARY                       │
│                                                        │
│  Member ─────emprunte────────> Book                   │
│     │                            │                     │
│     │                            │                     │
│     └──────── Loan ──────────────┘                    │
│                                                        │
│  Règles métier riches et encapsulées                  │
└────────────────────────────────────────────────────────┘
```

#### 1. **Member** (Membre)

```java
/**
 * Aggregate Root : Membre de la bibliothèque
 * Focus : Capacité d'emprunt
 */
public class Member {
    private MemberId id;
    private String name;                    // min 2 caractères
    private boolean canBorrow;              // peut emprunter ? (défaut: true)
    private int maxSimultaneousLoans;       // limite d'emprunts (défaut: 5)
    
    // Règles métier riches
    public boolean isEligibleToBorrow(int currentActiveLoans);
    public void forbidBorrowing();
    public void allowBorrowing();
    public void adjustBorrowingLimit(int newLimit);
}
```

**Règles métier encapsulées :**
- ✅ Validation du nom (minimum 2 caractères)
- ✅ Gestion de la capacité d'emprunt (activation/désactivation)
- ✅ Gestion dynamique de la limite d'emprunts (1-20)
- ✅ Calcul d'éligibilité (peut emprunter ET sous la limite)

---

#### 2. **Book** (Livre)

```java
/**
 * Aggregate Root : Livre de la bibliothèque
 * Focus : Disponibilité et validité
 */
public class Book {
    private BookId id;
    private Isbn isbn;                      // Value Object (10 ou 13 chiffres)
    private String title;                   // obligatoire
    private String author;                  // obligatoire
    private boolean available;              // disponible pour emprunt ?
    
    // Règles métier riches
    public void markAsUnavailable();
    public void markAsAvailable();
    public boolean canBeBorrowed();
}
```

**Règles métier encapsulées :**
- ✅ Validation ISBN complexe (format 10 ou 13 chiffres + nettoyage)
- ✅ Gestion de la disponibilité avec protection
- ✅ Protection contre double marquage (déjà disponible/indisponible)
- ✅ Validation titre et auteur

**Value Object Isbn :**
```java
public record Isbn(String value) {
    // Validation automatique dans le constructeur
    // Méthode formatted() pour affichage
}
```

---

#### 3. **Loan** (Emprunt)

```java
/**
 * Aggregate Root : Emprunt d'un livre par un membre
 * Focus : Cycle de vie de l'emprunt et retards
 */
public class Loan {
    private LoanId id;
    private String memberId;                // Référence OPAQUE vers Member
    private String bookId;                  // Référence OPAQUE vers Book
    private LocalDate borrowDate;
    private LocalDate dueDate;              // date limite de retour
    private LocalDate returnDate;           // null si pas encore retourné
    private LoanStatus status;              // ACTIVE, RETURNED, OVERDUE
    
    // Règles métier riches
    public void returnBook();
    public boolean isOverdue();
    public int calculateLateDays();
    public void markAsOverdue();
}
```

**Règles métier encapsulées :**
- ✅ Calcul automatique de la date limite (+14 jours par défaut)
- ✅ Détection automatique des retards
- ✅ Calcul des jours de retard (en cours ou après retour)
- ✅ Gestion des transitions d'état (ACTIVE → OVERDUE → RETURNED)
- ✅ Protection contre double retour

**Enum LoanStatus :**
```java
public enum LoanStatus {
    ACTIVE,    // Emprunt en cours, pas de retard
    OVERDUE,   // Emprunt en cours, en retard
    RETURNED;  // Livre retourné
}
```

---

### Principe DDD : Références opaques entre Aggregates

**IMPORTANT :** Les aggregates ne se référencent que par leurs identifiants (String), jamais par leurs Value Objects ou entités complètes.

```java
// ✅ BON : Référence opaque
public class Loan {
    private String memberId;    // pas MemberId
    private String bookId;      // pas BookId
}

// ❌ MAUVAIS : Couplage entre aggregates
public class Loan {
    private MemberId memberId;  // Value Object de Member
    private BookId bookId;      // Value Object de Book
}
```

**Avantages :**
- ✅ Aucun couplage entre aggregates
- ✅ Indépendance totale (changements isolés)
- ✅ Respect strict du DDD

---

### Domain Service

```java
/**
 * Service du domaine : coordonne plusieurs aggregates
 * sans les coupler directement.
 */
@DomainService
public class LoanEligibilityService {
    
    /**
     * Vérifie si un membre peut emprunter un livre.
     * 
     * Règles coordonnées :
     * - Member : canBorrow + limite non atteinte
     * - Book : disponible
     */
    public LoanEligibilityResult checkEligibility(
            Member member, 
            Book book, 
            int currentActiveLoans) {
        // Logique de coordination
    }
}
```

---

## 🔧 Choix techniques

### Organisation du code : Maven Multi-Module

**Pourquoi Maven Multi-Module pour un monolithe ?**

Bien que l'application soit un **monolithe** (1 seul déploiement), j'ai choisi d'organiser le code en **modules Maven séparés** pour :

1. **Isolation forte des couches**
    - Maven empêche physiquement les dépendances illégales entre couches
    - Impossible d'importer `infrastructure` depuis `domain` → erreur de compilation

2. **Respect strict de Clean Architecture**
    - Les dépendances sont contrôlées par les `pom.xml`
    - Le domain reste pur (zéro dépendance externe)

3. **Lisibilité et maintenabilité**
    - Chaque couche est un module avec ses propres responsabilités
    - Facilite l'onboarding des nouveaux développeurs

4. **Évolution facilitée**
    - Si un jour on veut extraire un Bounded Context en micro-service
    - Les modules sont déjà isolés et prêts à être déplacés

### Alternative : Single Module

Un **monolithe avec Clean Architecture** peut aussi être organisé en **un seul module Maven** avec des packages Java :
```
src/main/java/
├── domain/          (package)
├── application/     (package)
├── infrastructure/  (package)
└── presentation/    (package)
```

**Différence :**
- Single Module = Discipline des développeurs pour respecter les dépendances
- Multi-Module = Maven force le respect des dépendances

**J'ai choisi Multi-Module** pour avoir une barrière technique forte.

---

## 📦 Structure du projet
```
library-hub/
├── pom.xml                         (Parent POM)
│
├── domain/                         (Module - Cœur métier)
│   ├── pom.xml
│   └── src/main/java/ci/org/domain/
│       ├── member/
│       │   ├── entities/           (Member)
│       │   ├── valueobjects/       (MemberId)
│       │   ├── repositories/       (MemberRepository interface)
│       │   └── exceptions/         (MemberException...)
│       ├── book/
│       │   ├── entities/           (Book)
│       │   ├── valueobjects/       (BookId, Isbn)
│       │   ├── repositories/       (BookRepository interface)
│       │   └── exceptions/         (BookException...)
│       ├── loan/
│       │   ├── entities/           (Loan)
│       │   ├── valueobjects/       (LoanId, LoanStatus)
│       │   ├── repositories/       (LoanRepository interface)
│       │   ├── services/           (LoanEligibilityService)
│       │   └── exceptions/         (LoanException...)
│       └── shared/
│           └── valueobjects/       (Value Objects communs)
│
├── application/                    (Module - Use Cases)
│   ├── pom.xml
│   └── src/main/java/ci/org/application/
│       ├── member/
│       │   ├── usecases/           (CreateMember, UpdateMember...)
│       │   ├── commands/           (CreateMemberCommand...)
│       │   ├── queries/            (GetMemberQuery...)
│       │   └── dtos/               (MemberResponse...)
│       ├── book/
│       │   ├── usecases/           (CreateBook, UpdateBook...)
│       │   └── dtos/
│       └── loan/
│           ├── usecases/           (BorrowBook, ReturnBook...)
│           └── dtos/
│
├── infrastructure/                 (Module - Adapters techniques)
│   ├── pom.xml
│   └── src/main/java/ci/org/infrastructure/
│       ├── persistence/memory/     (Phase 1: InMemory)
│       │   ├── InMemoryMemberRepository
│       │   ├── InMemoryBookRepository
│       │   └── InMemoryLoanRepository
│       ├── persistence/jpa/        (Phase 2: PostgreSQL)
│       └── config/
│
├── presentation/                   (Module - API REST)
│   ├── pom.xml
│   └── src/main/java/ci/org/presentation/
│       └── rest/
│           ├── controllers/        (MemberController...)
│           ├── requests/           (CreateMemberRequest...)
│           ├── responses/          (ErrorResponse...)
│           ├── mappers/            (MemberMapper...)
│           └── exceptions/         (GlobalExceptionHandler)
│
├── shared/                         (Module - Shared Kernel)
│   ├── pom.xml
│   └── src/main/java/ci/org/shared/
│       ├── kernel/                 (UseCase interfaces, annotations)
│       │   ├── CommandUseCase
│       │   ├── QueryUseCase
│       │   ├── @UseCase
│       │   └── @DomainService
│       └── exceptions/             (Base exceptions)
│           ├── DomainException
│           └── InfrastructureException
│
├── common/                         (Module - Utilitaires communs)
│   └── pom.xml
│
└── starter/                        (Module - Point d'entrée)
    ├── pom.xml
    └── src/
        ├── main/java/ci/org/starter/
        │   └── LibraryHubApplication.java  (@SpringBootApplication)
        │
        └── test/java/ci/org/starter/
            └── integration/        (Tests d'intégration)
```

---

## 📚 Modules

### 1. `domain/` - Cœur métier (Domain Riche)

**Responsabilité :** Contient toute la logique métier pure avec des règles complexes encapsulées.

**Contenu :**
- **Entities** : Member, Book, Loan (avec logique métier riche)
- **Value Objects** : MemberId, BookId, LoanId, Isbn, LoanStatus
- **Domain Services** : LoanEligibilityService
- **Repository Interfaces** : Ports vers l'infrastructure
- **Domain Exceptions** : Erreurs métier typées et hiérarchisées

**Règles strictes :**
- ❌ **AUCUNE dépendance externe** (pas de Spring, pas de JPA, pas de Jackson)
- ✅ Java pur uniquement
- ✅ Règles métier encapsulées dans les entités
- ✅ Validation dans les constructeurs et méthodes
- ✅ Invariants protégés (impossible d'avoir un état invalide)

**Exemple :**
```java
// Exception métier
public class MemberCannotBorrowException extends MemberException {
    public MemberCannotBorrowException(MemberId memberId) {
        super("Member " + memberId.value() + " is not allowed to borrow");
    }
}

// Entité avec règle métier riche
public class Member {
    
    /**
     * Vérifie l'éligibilité à emprunter.
     * Règle : canBorrow = true ET emprunts < limite
     */
    public boolean isEligibleToBorrow(int currentActiveLoans) {
        if (!this.canBorrow) {
            return false;
        }
        
        return currentActiveLoans < this.maxSimultaneousLoans;
    }
    
    /**
     * Ajuste la limite d'emprunts avec validation.
     */
    public void adjustBorrowingLimit(int newLimit) {
        if (newLimit < 1 || newLimit > 20) {
            throw new InvalidBorrowingLimitException(newLimit);
        }
        this.maxSimultaneousLoans = newLimit;
    }
}
```

---

### 2. `application/` - Use Cases

**Responsabilité :** Orchestration de la logique métier (coordination des aggregates).

**Contenu :**
- Use Cases (CreateMember, BorrowBook, ReturnBook, etc.)
- Commands (Write operations)
- Queries (Read operations)
- DTOs (Data Transfer Objects)
- Mappers (Domain ↔ DTO)

**Dépendances :**
- ✅ Dépend de `domain` (interfaces uniquement)
- ❌ Ne dépend PAS de `infrastructure` ou `presentation`

**Gestion des exceptions :**
- Les use cases laissent remonter les exceptions métier du domain
- Aucune try-catch sauf si nécessaire pour la logique métier
- Les exceptions sont gérées au niveau de la couche presentation

**Exemple :**
```java
@UseCase("Borrow a book")
public class BorrowBookUseCase 
        implements CommandUseCase<BorrowBookCommand, LoanResponse> {
    
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final LoanEligibilityService loanEligibilityService;
    
    @Override
    public LoanResponse execute(BorrowBookCommand command) {
        // 1. Récupérer member et book
        Member member = memberRepository.findById(command.memberId())
            .orElseThrow(() -> new MemberNotFoundException(command.memberId()));
            
        Book book = bookRepository.findById(command.bookId())
            .orElseThrow(() -> new BookNotFoundException(command.bookId()));
        
        // 2. Compter les emprunts actifs du membre
        int activeLoans = loanRepository.countActiveByMember(member.getId());
        
        // 3. Vérifier l'éligibilité (Domain Service)
        LoanEligibilityResult eligibility = 
            loanEligibilityService.checkEligibility(member, book, activeLoans);
        eligibility.throwIfNotEligible();
        
        // 4. Créer l'emprunt (Domain logic)
        Loan loan = Loan.create(
            member.getId().value(), 
            book.getId().value(), 
            14  // 14 jours de prêt
        );
        
        // 5. Marquer le livre comme indisponible (Domain logic)
        book.markAsUnavailable();
        
        // 6. Sauvegarder
        Loan savedLoan = loanRepository.save(loan);
        bookRepository.save(book);
        
        // 7. Retourner DTO
        return LoanMapper.toResponse(savedLoan);
    }
}
```

---

### 3. `infrastructure/` - Adapters

**Responsabilité :** Implémentation des ports du domain.

**Contenu :**
- Repositories InMemory (Phase 1)
- Repositories JPA (Phase 2)
- Configuration Spring

**Dépendances :**
- ✅ Dépend de `domain` (implémente les interfaces)
- ✅ Peut utiliser Spring, JPA, etc.

**Gestion des exceptions :**
- Convertit les exceptions techniques en exceptions métier si nécessaire
- Lance des exceptions d'infrastructure pour les problèmes techniques

**Exemple :**
```java
@Repository
public class InMemoryLoanRepository implements LoanRepository {
    private final Map<LoanId, Loan> store = new ConcurrentHashMap<>();
    
    @Override
    public Loan save(Loan loan) {
        store.put(loan.getId(), loan);
        return loan;
    }
    
    @Override
    public int countActiveByMember(String memberId) {
        return (int) store.values().stream()
            .filter(loan -> loan.getMemberId().equals(memberId))
            .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE 
                         || loan.getStatus() == LoanStatus.OVERDUE)
            .count();
    }
}
```

---

### 4. `presentation/` - API REST

**Responsabilité :** Exposition de l'API HTTP.

**Contenu :**
- Controllers REST
- Request DTOs (validation Bean Validation)
- Response DTOs
- Global Exception Handler

**Dépendances :**
- ✅ Dépend de `application` (use cases)
- ❌ Ne connaît PAS `infrastructure` ou `domain`

**Exemple :**
```java
@RestController
@RequestMapping("/api/loans")
public class LoanController {
    
    private final CommandUseCase<BorrowBookCommand, LoanResponse> borrowBookUseCase;
    
    @PostMapping("/borrow")
    public ResponseEntity<LoanResponse> borrowBook(
            @Valid @RequestBody BorrowBookRequest request) {
        
        LoanResponse response = borrowBookUseCase.execute(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

// Global Exception Handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(LoanNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleLoanNotAllowed(
            LoanNotAllowedException ex) {
        
        ErrorResponse error = new ErrorResponse(
            "LOAN_NOT_ALLOWED",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

---

### 5. `shared/` - Shared Kernel

**Responsabilité :** Concepts partagés par tous les modules.

**Contenu :**
- Interfaces Use Case (CommandUseCase, QueryUseCase)
- Annotations (@UseCase, @DomainService)
- Base Exceptions (DomainException, InfrastructureException)

**Exemple :**
```java
// Interface de use case
public interface CommandUseCase<C, R> {
    R execute(C command);
}

// Annotation
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface UseCase {
    String value() default "";
}

// Exception de base
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
```

---

### 6. `starter/` - Point d'entrée

**Responsabilité :** Assembler tous les modules et démarrer l'application.

**Contenu :**
- `@SpringBootApplication`
- Dépendances vers TOUS les modules
- Tests d'intégration (accès à tout le contexte)

---

## 📐 Règles métier

### Member (Membre)

#### Règles d'identité
1. Un membre possède un identifiant unique (MemberId)
2. Un membre possède un nom valide (minimum 2 caractères)

#### Règles sur la capacité d'emprunt
3. Par défaut, un membre peut emprunter (`canBorrow = true`)
4. Un membre peut être interdit d'emprunt (réversible)
5. La capacité d'emprunt peut être vérifiée à tout moment

#### Règles sur le nombre d'emprunts
6. Chaque membre a une limite d'emprunts simultanés (défaut: 5)
7. La limite est configurable (entre 1 et 20)
8. Un membre ne peut pas dépasser sa limite

#### Règle d'éligibilité
9. **Un membre peut emprunter SI ET SEULEMENT SI :**
    - Il est autorisé à emprunter (`canBorrow = true`)
    - **ET** il n'a pas atteint sa limite (`activeLoans < maxSimultaneousLoans`)

**Exceptions :**
- `MemberNotFoundException` (404 Not Found)
- `InvalidMemberNameException` (400 Bad Request)
- `MemberCannotBorrowException` (400 Bad Request)
- `BorrowingLimitReachedException` (400 Bad Request)
- `InvalidBorrowingLimitException` (400 Bad Request)

---

### Book (Livre)

#### Règles d'identité
1. Un livre possède un identifiant unique (BookId)
2. Un livre possède un ISBN unique (format 10 ou 13 chiffres)
3. Un livre possède un titre et un auteur obligatoires

#### Règles sur la disponibilité
4. Par défaut, un livre est disponible (`available = true`)
5. Un livre peut être marqué comme indisponible (emprunté)
6. Un livre peut être marqué comme disponible (retourné)
7. Protection contre double marquage (déjà disponible/indisponible)

#### Règle d'emprunt
8. **Un livre peut être emprunté SI ET SEULEMENT SI :**
    - Il est disponible (`available = true`)

**Value Object Isbn :**
- Format accepté : 10 ou 13 chiffres
- Nettoyage automatique (espaces et tirets)
- Méthode `formatted()` pour affichage standard

**Exceptions :**
- `BookNotFoundException` (404 Not Found)
- `InvalidIsbnException` (400 Bad Request)
- `BookAlreadyAvailableException` (400 Bad Request)
- `BookAlreadyUnavailableException` (400 Bad Request)
- `InvalidBookTitleException` (400 Bad Request)
- `InvalidBookAuthorException` (400 Bad Request)

---

### Loan (Emprunt)

#### Règles de création
1. Un emprunt lie un membre à un livre (références par String)
2. Date d'emprunt = date du jour
3. Date limite = date d'emprunt + durée (ex: 14 jours)
4. Statut initial = ACTIVE
5. Date de retour = null

#### Règles sur le retour
6. Un emprunt ne peut être retourné qu'une seule fois
7. Le retour enregistre la date actuelle
8. Le retour change le statut à RETURNED

#### Règles sur les retards
9. Un emprunt est en retard si : aujourd'hui > dueDate ET status ≠ RETURNED
10. Un emprunt retourné n'est plus considéré en retard
11. Le nombre de jours de retard peut être calculé :
    - Si RETURNED : jours entre dueDate et returnDate
    - Si ACTIVE/OVERDUE : jours entre dueDate et aujourd'hui
12. Un emprunt peut être marqué comme en retard (ACTIVE → OVERDUE)

#### États et transitions
13. Statuts possibles : ACTIVE, OVERDUE, RETURNED
14. Transitions :
    ```
    ACTIVE ──(retard)──> OVERDUE ──(retour)──> RETURNED
       │                                          ▲
       └──────────(retour)─────────────────────┘
    ```

**Exceptions :**
- `LoanNotFoundException` (404 Not Found)
- `LoanAlreadyReturnedException` (400 Bad Request)
- `LoanNotOverdueYetException` (400 Bad Request)
- `CannotMarkReturnedLoanAsOverdueException` (400 Bad Request)
- `InvalidMemberIdException` (400 Bad Request)
- `InvalidBookIdException` (400 Bad Request)

---

### Domain Service : LoanEligibilityService

**Responsabilité :** Coordonner plusieurs aggregates pour vérifier l'éligibilité d'un emprunt.

**Règle coordonnée :**
```
Un emprunt est possible SI ET SEULEMENT SI :
  - Member.canBorrow = true
  ET Member.activeLoans < Member.maxSimultaneousLoans
  ET Book.available = true
```

**Exceptions :**
- `LoanNotAllowedException` (400 Bad Request)
    - Raisons multiples possibles dans le message

---

## 🎯 Hiérarchie des exceptions

```
Throwable
└── Exception
    └── RuntimeException
        ├── DomainException (shared)
        │   ├── MemberException (domain/member)
        │   │   ├── MemberNotFoundException
        │   │   ├── InvalidMemberNameException
        │   │   ├── MemberCannotBorrowException
        │   │   ├── BorrowingLimitReachedException
        │   │   └── InvalidBorrowingLimitException
        │   │
        │   ├── BookException (domain/book)
        │   │   ├── BookNotFoundException
        │   │   ├── InvalidIsbnException
        │   │   ├── BookAlreadyAvailableException
        │   │   ├── BookAlreadyUnavailableException
        │   │   ├── InvalidBookTitleException
        │   │   └── InvalidBookAuthorException
        │   │
        │   └── LoanException (domain/loan)
        │       ├── LoanNotFoundException
        │       ├── LoanAlreadyReturnedException
        │       ├── LoanNotOverdueYetException
        │       ├── CannotMarkReturnedLoanAsOverdueException
        │       ├── LoanNotAllowedException
        │       ├── InvalidMemberIdException
        │       └── InvalidBookIdException
        │
        └── InfrastructureException (shared)
            ├── PersistenceException
            └── ConfigurationException
```

---

## 🚀 Installation

### Prérequis

- Java 21+
- Maven 3.9+

### Build
```bash
# À la racine du projet
mvn clean install
```

### Lancement
```bash
cd starter
mvn spring-boot:run
```

L'application démarre sur `http://localhost:8080`

---

## 🧪 Tests

### Structure des tests
```
domain/src/test/          → Tests unitaires des entités (règles métier)
application/src/test/     → Tests unitaires des use cases (avec mocks)
starter/src/test/         → Tests d'intégration (HTTP → DB)
```

### Lancer les tests
```bash
# Tous les tests
mvn test

# Tests d'un module spécifique
mvn test -pl domain

# Tests d'intégration uniquement
mvn test -pl starter
```

### Exemple de test du domain riche
```java
class MemberTest {
    
    @Test
    void shouldBeEligibleWhenCanBorrowAndUnderLimit() {
        // Given
        Member member = Member.create("John Doe");
        
        // When
        boolean eligible = member.isEligibleToBorrow(3);
        
        // Then
        assertTrue(eligible);  // 3 < 5 (limite par défaut)
    }
    
    @Test
    void shouldNotBeEligibleWhenLimitReached() {
        // Given
        Member member = Member.create("John Doe");
        
        // When
        boolean eligible = member.isEligibleToBorrow(5);
        
        // Then
        assertFalse(eligible);  // 5 = 5 (limite atteinte)
    }
    
    @Test
    void shouldNotBeEligibleWhenCannotBorrow() {
        // Given
        Member member = Member.create("John Doe");
        member.forbidBorrowing();
        
        // When
        boolean eligible = member.isEligibleToBorrow(2);
        
        // Then
        assertFalse(eligible);  // canBorrow = false
    }
    
    @Test
    void shouldThrowExceptionWhenAdjustingLimitToZero() {
        // Given
        Member member = Member.create("John Doe");
        
        // When & Then
        assertThrows(InvalidBorrowingLimitException.class, () -> {
            member.adjustBorrowingLimit(0);
        });
    }
}
```

### Exemple de test d'intégration
```java
@SpringBootTest
@AutoConfigureMockMvc
class LoanControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldBorrowBookSuccessfully() throws Exception {
        // Given : un membre et un livre existent
        
        // When : emprunt du livre
        mockMvc.perform(post("/api/loans/borrow")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "memberId": "member-123",
                  "bookId": "book-456"
                }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
    
    @Test
    void shouldReturn400WhenMemberCannotBorrow() throws Exception {
        // Given : un membre interdit d'emprunt
        
        // When : tentative d'emprunt
        mockMvc.perform(post("/api/loans/borrow")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "memberId": "member-forbidden",
                  "bookId": "book-456"
                }
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("LOAN_NOT_ALLOWED"));
    }
}
```

---

## 🔄 Phases d'implémentation

### Phase 1 : In-Memory (Actuel)

- Stockage en mémoire (ConcurrentHashMap)
- Données perdues au redémarrage
- Parfait pour développement et tests du domain riche

### Phase 2 : PostgreSQL (Future)

- Migration vers base de données relationnelle
- Implémentation JPA des repositories
- **Aucun impact sur domain et application** (grâce à l'architecture hexagonale)
- Gestion des transactions (@Transactional)

---

## 📊 Bounded Contexts

L'application est organisée en 3 contextes métier indépendants :

| Context | Responsabilité | Agrégat Principal | Communications |
|---------|----------------|-------------------|----------------|
| **Member** | Gestion des membres et capacité d'emprunt | Member | Référencé par Loan (String) |
| **Book** | Gestion du catalogue de livres | Book | Référencé par Loan (String) |
| **Loan** | Gestion des emprunts et retours | Loan | Référence Member et Book (String) |

**Communication entre contextes :**
- Par identifiants String (références opaques)
- Via les repositories (ports)
- Coordination via Domain Services (LoanEligibilityService)
- Aucun couplage direct entre aggregates

---

## 🎓 Objectifs pédagogiques

Ce projet démontre :

1. ✅ **Domain Riche** avec règles métier complexes encapsulées
2. ✅ **Clean Architecture** dans un contexte réel
3. ✅ **Hexagonal Architecture** avec ports et adapters
4. ✅ **DDD Tactique** (Aggregates, Value Objects, Domain Services, Bounded Contexts)
5. ✅ **Isolation des Aggregates** (références opaques par String)
6. ✅ **CQS léger** (séparation Commands/Queries)
7. ✅ **Maven Multi-Module** pour organisation forte
8. ✅ **Gestion d'erreurs par exceptions** hiérarchisées et typées
9. ✅ **Tests à tous les niveaux** (unitaires du domain, intégration)
10. ✅ **Global Exception Handling** avec Spring

---

## 📝 Licence

Projet éducatif - Libre d'utilisation

---

## 👤 Auteur

Projet d'apprentissage personnel  
Gerard KS

---

## 🙏 Références

- Clean Architecture (Robert C. Martin)
- Hexagonal Architecture (Alistair Cockburn)
- Domain-Driven Design (Eric Evans)
- Implementing Domain-Driven Design (Vaughn Vernon)
- Domain-Driven Design Distilled (Vaughn Vernon)