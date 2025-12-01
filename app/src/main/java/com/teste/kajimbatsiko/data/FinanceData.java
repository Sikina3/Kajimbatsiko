package com.teste.kajimbatsiko.data;

import com.teste.kajimbatsiko.data.rooms.ExpenseSum;

import java.util.List;

public class FinanceData {
    public List<ExpenseSum> expenses;
    public List<ExpenseSum> incomes;

    public FinanceData(List<ExpenseSum> expenses, List<ExpenseSum> incomes){
        this.expenses = expenses;
        this.incomes = incomes;
    }
}
