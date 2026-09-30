package com.example.queuewise.repository;

import com.example.queuewise.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.queuewise.model.TokenStatus;
import java.util.List;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    List<Token> findByStatusOrderByIdAsc(TokenStatus status);

    Optional<Token> findFirstByOrderByIdDesc();

    Optional<Token> findByTokenNumber(String tokenNumber);
}