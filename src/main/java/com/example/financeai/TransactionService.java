package com.example.financeai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Transaction save(Transaction transaction, Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            transaction.setUser(user.get());
            return transactionRepository.save(transaction);
        }
        return null;
    }
    
    public List<Transaction> getByMonth(Integer userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);
    }
    
    public List<Transaction> getByTypeAndMonth(Integer userId, String type, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return transactionRepository.findByUserIdAndTypeAndDateBetweenOrderByDateDesc(userId, type, start, end);
    }
    
    public Double getMonthlySum(Integer userId, String type, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        Double sum = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(userId, type, start, end);
        return sum != null ? sum : 0.0;
    }
    
    public Double getTotalIncome(Integer userId) {
        Double sum = transactionRepository.sumAllIncomeByUserId(userId);
        return sum != null ? sum : 0.0;
    }
    
    public Double getTotalExpense(Integer userId) {
        Double sum = transactionRepository.sumAllExpensesByUserId(userId);
        return sum != null ? sum : 0.0;
    }
    
    public void delete(Long id) {
        transactionRepository.deleteById(id);
    }
    public Map<String, Object> getAIInsights(Integer userId, int year, int month) {
        Map<String, Object> insights = new HashMap<>();
        
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        
        // Get current month data
        Double monthlyIncome = getMonthlySum(userId, "INCOME", year, month);
        Double monthlyExpense = getMonthlySum(userId, "EXPENSE", year, month);
        Double monthlySaving = getMonthlySum(userId, "SAVING", year, month);
        
        // Get all-time data
        Double totalIncome = getTotalIncome(userId);
        Double totalExpense = getTotalExpense(userId);
        Double currentBalance = totalIncome - totalExpense;
        
        // Get all transactions for this month
        List<Transaction> transactions = getByMonth(userId, year, month);
        
        // Find top expense category
        Map<String, Double> categorySpending = new HashMap<>();
        for (Transaction t : transactions) {
            if ("EXPENSE".equals(t.getType())) {
                categorySpending.put(t.getCategory(), 
                    categorySpending.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
            }
        }
        
        String topCategory = "";
        Double topAmount = 0.0;
        for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
            if (entry.getValue() > topAmount) {
                topAmount = entry.getValue();
                topCategory = entry.getKey();
            }
        }
        
        // Generate insights
        List<String> adviceList = new ArrayList<>();
        String summary = "";
        
        // 1. Balance check
        if (currentBalance < 0) {
            adviceList.add("⚠️ Your balance is negative! You need to reduce expenses immediately.");
        } else if (currentBalance < 500) {
            adviceList.add("💡 Your balance is low. Try to save more this month.");
        } else if (currentBalance > 5000) {
            adviceList.add("🎉 Great job! You have a healthy balance. Consider investing some.");
        }
        
        // 2. Income vs Expense ratio
        if (monthlyIncome > 0) {
            double expenseRatio = (monthlyExpense / monthlyIncome) * 100;
            if (expenseRatio > 70) {
                adviceList.add("⚠️ You're spending " + String.format("%.0f", expenseRatio) + "% of your income. Try to reduce expenses.");
            } else if (expenseRatio < 50) {
                adviceList.add("✅ Excellent! You're spending only " + String.format("%.0f", expenseRatio) + "% of your income. Keep it up!");
            } else {
                adviceList.add("📊 You're spending " + String.format("%.0f", expenseRatio) + "% of your income. Try to save more.");
            }
        }
        
        // 3. Top spending category
        if (!topCategory.isEmpty()) {
            adviceList.add("🔍 Your highest expense is **" + topCategory + "** ($" + String.format("%.2f", topAmount) + "). Consider reducing it.");
        }
        
        // 4. Savings advice
        if (monthlySaving == 0) {
            adviceList.add("💰 You haven't saved anything this month. Start with 10% of your income.");
        } else if (monthlySaving < monthlyIncome * 0.2) {
            adviceList.add("💪 Good start! Try to save at least 20% of your income.");
        } else {
            adviceList.add("🌟 Amazing! You're saving " + String.format("%.0f", (monthlySaving/monthlyIncome)*100) + "% of your income.");
        }
        
        // 5. Specific suggestions based on spending
        if (topCategory.equalsIgnoreCase("Restaurant") || topCategory.equalsIgnoreCase("Food")) {
            adviceList.add("🍽️ You spend a lot on food/dining. Try cooking at home to save money.");
        }
        if (topCategory.equalsIgnoreCase("Shopping") || topCategory.equalsIgnoreCase("Clothing")) {
            adviceList.add("🛍️ High shopping expenses. Consider waiting for sales or using a shopping list.");
        }
        if (topCategory.equalsIgnoreCase("Entertainment")) {
            adviceList.add("🎬 Consider reducing entertainment expenses. Try free alternatives.");
        }
        
        // 6. Monthly summary
        summary = "📊 **Monthly Summary for " + ym.getMonth() + " " + ym.getYear() + "**\n" +
                  "• Income: $" + String.format("%.2f", monthlyIncome) + "\n" +
                  "• Expenses: $" + String.format("%.2f", monthlyExpense) + "\n" +
                  "• Savings: $" + String.format("%.2f", monthlySaving) + "\n" +
                  "• Balance: $" + String.format("%.2f", currentBalance);
        
        // 7. General tips
        if (adviceList.isEmpty()) {
            adviceList.add("📈 You're doing well! Keep tracking your finances.");
            adviceList.add("💡 Tip: Review your spending weekly.");
            adviceList.add("🎯 Set a savings goal for next month.");
        }
        
        // Add random tips
        String[] tips = {
            "📖 Track every expense, no matter how small.",
            "🎯 Set specific financial goals.",
            "📊 Review your budget monthly.",
            "💳 Use cash for small purchases.",
            "🏦 Automate your savings.",
            "📈 Invest in index funds for long-term growth.",
            "✂️ Cut unnecessary subscriptions.",
            "🛒 Shop with a list to avoid impulse buys.",
            "☕ Skip one coffee a day and save $100+ monthly.",
            "📱 Use finance apps to track spending."
        };
        
        if (adviceList.size() < 5) {
            Random rand = new Random();
            int count = 0;
            while (count < 2 && count < tips.length) {
                int idx = rand.nextInt(tips.length);
                String tip = tips[idx];
                if (!adviceList.contains(tip)) {
                    adviceList.add("💡 " + tip);
                    count++;
                }
            }
        }
        
        insights.put("summary", summary);
        insights.put("advice", adviceList);
        insights.put("monthlyIncome", monthlyIncome);
        insights.put("monthlyExpense", monthlyExpense);
        insights.put("monthlySaving", monthlySaving);
        insights.put("currentBalance", currentBalance);
        insights.put("topCategory", topCategory);
        insights.put("topAmount", topAmount);
        
        return insights;
    }
}