package com.example.financeai;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String type; // INCOME, EXPENSE, SAVING
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(nullable = false)
    private String category;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public Transaction() {}
    
    // Getters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getType() { return type; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public Double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setType(String type) { this.type = type; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setCategory(String category) { this.category = category; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}