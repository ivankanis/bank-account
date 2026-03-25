package fr.kanis.bankaccount.infrastructure.persistence;

import fr.kanis.bankaccount.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface JpaAccountRepository extends JpaRepository<AccountEntity, UUID> {
}
