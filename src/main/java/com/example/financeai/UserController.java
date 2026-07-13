package com.example.financeai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionService transactionService;

    // Home Page
    @GetMapping("/")
    public String home(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            if (error.equals("invalid")) {
                model.addAttribute("errorMessage", "Invalid email or password! Please try again.");
            } else if (error.equals("email_exists")) {
                model.addAttribute("errorMessage", "Email already registered! Please login.");
            }
        }
        return "index";
    }

    // Register
    @PostMapping("/register")
    public String register(User user, HttpSession session) {
        User existingUser = userService.findByEmail(user.getEmail());
        if (existingUser != null) {
            return "redirect:/?error=email_exists";
        }
        
        User savedUser = userService.register(user);
        session.setAttribute("loggedUser", savedUser);
        return "redirect:/dashboard";
    }

    // Login
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session) {
        User user = userService.login(email, password);
        if (user != null) {
            session.setAttribute("loggedUser", user);
            return "redirect:/dashboard";
        }
        return "redirect:/?error=invalid";
    }

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return "redirect:/";
        }
        
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        
        List<Transaction> transactions = transactionService.getByMonth(user.getId(), year, month);
        
        Double monthlyIncome = transactionService.getMonthlySum(user.getId(), "INCOME", year, month);
        Double monthlyExpense = transactionService.getMonthlySum(user.getId(), "EXPENSE", year, month);
        Double monthlySaving = transactionService.getMonthlySum(user.getId(), "SAVING", year, month);
        
        Double totalIncome = transactionService.getTotalIncome(user.getId());
        Double totalExpense = transactionService.getTotalExpense(user.getId());
        Double currentBalance = totalIncome - totalExpense;
        
        model.addAttribute("user", user);
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentBalance", currentBalance);
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("monthlyExpense", monthlyExpense);
        model.addAttribute("monthlySaving", monthlySaving);
        model.addAttribute("currentMonth", YearMonth.of(year, month).toString());
        
        return "dashboard";
    }
    @GetMapping("/api/ai-insights")
    @ResponseBody
    public Map<String, Object> getAIInsights(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Not logged in");
            return error;
        }
        
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        
        return transactionService.getAIInsights(user.getId(), year, month);
    }
    // DELETE the addTransaction method from here - it's already in TransactionController
    
    // Delete Transaction - Remove this too, it's in TransactionController
    // @GetMapping("/delete-transaction/{id}")
    // public String deleteTransaction(...)
    
    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}