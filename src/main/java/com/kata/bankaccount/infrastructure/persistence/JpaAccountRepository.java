package com.kata.bankaccount.infrastructure.persistence;

import com.kata.bankaccount.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface JpaAccountRepository extends JpaRepository<AccountEntity, UUID> {
}
