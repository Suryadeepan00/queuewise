package com.example.queuewise.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tokenNumber;

    @Column(nullable = false)
    private String serviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus status;

    protected Token() {
        // Required by JPA
    }

    public Token(String tokenNumber, String serviceType) {
        this.tokenNumber = tokenNumber;
        this.serviceType = serviceType;
        this.status = TokenStatus.WAITING;
    }

    public Long getId() {
        return id;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public String getServiceType() {
        return serviceType;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }
}