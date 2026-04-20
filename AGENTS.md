# EcoAdmin — Coding Standards (GGA Review Rules)

Review every staged file against these rules. For each violation, report:
`FAIL [rule-id] file:line — explanation`
If all rules pass, respond exactly: `PASS`

---

## 1. Dependency Injection — spring-boot/di

- NEVER use field injection (`@Autowired` on fields). Always use constructor injection.
- NEVER use `@Autowired` on constructors (Spring infers it automatically with one constructor).

```java
// WRONG
@Service
public class ResiduoService {
    @Autowired
    private ResiduoRepository repository; // FAIL: field injection
}

// CORRECT
@Service
public class ResiduoService {
    private final ResiduoRepository repository;

    public ResiduoService(ResiduoRepository repository) {
        this.repository = repository;
    }
}
```

---

## 2. Transaction Boundaries — spring-boot/tx

- `@Transactional` belongs on **service layer** methods only.
- NEVER place `@Transactional` on controllers or repositories.

---

## 3. DTOs and Value Objects — java-21/records

- Use Java `record` for DTOs, value objects, and command/query objects.
- Validate inputs in the compact constructor, never in a separate method.

```java
// CORRECT
public record UsuarioDTO(String nombre, String email) {
    public UsuarioDTO {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre required");
    }
}
```

---

## 4. Layer Boundaries — hexagonal/layers

- **Domain** (`modelo/`): No framework annotations (`@Service`, `@Repository`, `@Entity` allowed as JPA mapping only). No business logic in getters/setters.
- **Application** (`servicios/`): Orchestrates domain + repositories. No HTTP/web concerns.
- **Infrastructure** (`controladores/`, `repository/`, `config/`): Wires framework adapters. Does NOT contain business logic.

A controller MUST NOT call a repository directly — always go through a service.

```java
// WRONG
@GetMapping("/residuos")
public String list(Model model) {
    model.addAttribute("residuos", residuoRepository.findAll()); // FAIL: controller→repo direct
    return "residuos";
}

// CORRECT
@GetMapping("/residuos")
public String list(Model model) {
    model.addAttribute("residuos", residuoService.findAll());
    return "residuos";
}
```

---

## 5. Configuration — spring-boot/config

- Use `@ConfigurationProperties` records for grouped config. NEVER scatter `@Value` across services.
- Always annotate the config class with `@Validated` and add JSR-303 constraints.

---

## 6. Security — spring-boot/security

- NEVER log passwords, tokens, or sensitive user data.
- NEVER store passwords in plain text — always encode with `PasswordEncoder`.
- Input from HTTP requests MUST be validated before use (use `@Valid` + bean validation).

---

## 7. Conventional Commits — commit-hygiene

Every commit message MUST match:
```
^(build|chore|ci|docs|feat|fix|perf|refactor|revert|style|test)(\([a-z0-9\._-]+\))?!?: .+
```

Valid scopes for this project: `residuos`, `traslados`, `centros`, `usuarios`, `auth`, `config`, `db`, `ui`

- NEVER add `Co-Authored-By` or AI attribution trailers.
- Subject line ≤ 72 characters. Imperative mood: "add" not "added".

---

## 8. Thymeleaf Templates — ui/templates

- NEVER write inline JavaScript business logic in templates.
- Always use `th:text` (not `th:utext`) unless HTML escaping is explicitly intentional and safe.
- Form actions MUST use `th:action="@{/path}"` — never hardcode URLs.
- CSRF tokens must be included in all POST forms (Thymeleaf + Spring Security handles this automatically with `th:action`).

---

## 9. General Java Quality

- No raw types (use generics): `List` → `List<Residuo>`.
- No empty catch blocks — always log or rethrow.
- No `System.out.println` in production code — use SLF4J (`log.info`, `log.error`).
- Methods longer than 30 lines are a smell — suggest extraction.
