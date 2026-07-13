package com.example.financeai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;

@Controller
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    // Add transaction
    @PostMapping("/add-transaction")
    public String addTransaction(@RequestParam String type,
                                 @RequestParam String date,
                                 @RequestParam String description,
                                 @RequestParam Double amount,
                                 @RequestParam String category,
                                 @RequestParam String paymentMethod,
                                 HttpSession session) {
        
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return "redirect:/";
        }
        
        Transaction transaction = new Transaction();
        transaction.setType(type.toUpperCase());
        transaction.setDate(LocalDate.parse(date));
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setCategory(category);
        transaction.setPaymentMethod(paymentMethod);
        
        transactionService.save(transaction, user.getId());
        
        return "redirect:/dashboard";
    }
    
    // Delete transaction
    @GetMapping("/delete-transaction/{id}")
    public String deleteTransaction(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return "redirect:/";
        }
        transactionService.delete(id);
        return "redirect:/dashboard";
    }
}