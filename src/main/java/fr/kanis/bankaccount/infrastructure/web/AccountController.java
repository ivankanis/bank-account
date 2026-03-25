package fr.kanis.bankaccount.infrastructure.web;

import fr.kanis.bankaccount.application.command.CreateAccountCommand;
import fr.kanis.bankaccount.domain.model.AccountId;
import fr.kanis.bankaccount.domain.port.in.CreateAccountUseCase;
import fr.kanis.bankaccount.domain.port.in.GetAccountUseCase;
import fr.kanis.bankaccount.infrastructure.web.dto.AccountResponse;
import fr.kanis.bankaccount.infrastructure.web.dto.CreateAccountRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase, GetAccountUseCase getAccountUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        var command = new CreateAccountCommand(request.ownerName(), request.initialAmount(), request.currencyCode());
        var account = createAccountUseCase.createAccount(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(account));
    }

    @GetMapping
    public List<AccountResponse> getAll() {
        return getAccountUseCase.getAllAccounts().stream()
                .map(AccountResponse::from)
                .toList();
    }

    @GetMapping("/{accountId}")
    public AccountResponse getById(@PathVariable UUID accountId) {
        return AccountResponse.from(getAccountUseCase.getAccount(AccountId.of(accountId)));
    }
}
