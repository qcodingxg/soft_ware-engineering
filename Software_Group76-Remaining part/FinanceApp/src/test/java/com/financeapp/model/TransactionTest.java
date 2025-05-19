package com.financeapp.model;

import com.financeapp.model.Transaction;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void testDefaultConstructor() {
        Transaction transaction = new Transaction();

        assertEquals(LocalDate.now(), transaction.getDate());
        assertEquals("Uncategorized", transaction.getCategory());
        assertEquals(0.0, transaction.getAmount(), 0.001);
        assertEquals("", transaction.getDescription());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDate date = LocalDate.of(2024, 5, 15);
        Transaction transaction = new Transaction(date, "Shopping", 99.99, "New shoes");

        assertEquals(date, transaction.getDate());
        assertEquals("Shopping", transaction.getCategory());
        assertEquals(99.99, transaction.getAmount(), 0.001);
        assertEquals("New shoes", transaction.getDescription());
    }

    @Test
    void testToCSV() {
        LocalDate date = LocalDate.of(2024, 6, 20);
        Transaction transaction = new Transaction(date, "Food", 25.50, "Dinner with friends");

        String expected = "2024-06-20,Food,25.50,Dinner with friends";
        assertEquals(expected, transaction.toCSV());
    }

    @Test
    void testFromCSV() {
        String csv = "2024-07-10,Transportation,5.75,Subway ticket";
        Transaction transaction = Transaction.fromCSV(csv);

        assertEquals(LocalDate.of(2024, 7, 10), transaction.getDate());
        assertEquals("Transportation", transaction.getCategory());
        assertEquals(5.75, transaction.getAmount(), 0.001);
        assertEquals("Subway ticket", transaction.getDescription());
    }

    @Test
    void testFromCSV_EmptyCategory() {
        String csv = "2024-07-10,,5.75,Subway ticket";
        Transaction transaction = Transaction.fromCSV(csv);

        assertEquals("Uncategorized", transaction.getCategory());
    }

    @Test
    void testFromCSV_InvalidDate() {
        String csv = "2024-13-10,Transportation,5.75,Subway ticket";
        assertThrows(IllegalArgumentException.class, () -> Transaction.fromCSV(csv));
    }

    @Test
    void testFromCSV_InvalidAmount() {
        String csv = "2024-07-10,Transportation,invalid,Subway ticket";
        assertThrows(IllegalArgumentException.class, () -> Transaction.fromCSV(csv));
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDate date = LocalDate.now();
        Transaction t1 = new Transaction(date, "Food", 15.0, "Lunch");
        Transaction t2 = new Transaction(date, "Food", 15.0, "Lunch");
        Transaction t3 = new Transaction(date, "Food", 20.0, "Dinner");

        assertEquals(t1, t2);
        assertNotEquals(t1, t3);
        assertEquals(t1.hashCode(), t2.hashCode());
        assertNotEquals(t1.hashCode(), t3.hashCode());
    }

    @Test
    void testToString() {
        Transaction transaction = new Transaction(
                LocalDate.of(2024, 8, 1),
                "Entertainment",
                120.0,
                "Concert tickets");

        String expected = "Transaction{date=2024-08-01, category='Entertainment', " +
                "amount=120.0, description='Concert tickets'}";
        assertEquals(expected, transaction.toString());
    }
}