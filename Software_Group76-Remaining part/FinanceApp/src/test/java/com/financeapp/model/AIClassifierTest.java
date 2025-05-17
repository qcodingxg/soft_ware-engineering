package com.financeapp.model;

import com.financeapp.model.AIClassifier;
import com.financeapp.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AIClassifierTest {

    private AIClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new AIClassifier();
    }

    @Test
    void testClassify_AlreadyClassified() throws IOException {
        Transaction transaction = new Transaction(
                LocalDate.now(), "Shopping", 50.0, "New clothes");

        Transaction result = classifier.classify(transaction);
        assertEquals("Shopping", result.getCategory());
    }

    @Test
    void testClassify_HolidayPeriod() throws IOException {
        // 2024 Spring Festival period: Feb 4-24
        Transaction transaction = new Transaction(
                LocalDate.of(2024, 2, 10), null, 100.0, "New Year gift");

        Transaction result = classifier.classify(transaction);
        assertEquals("Holiday", result.getCategory());
    }

    @Test
    void testClassify_NonHolidayPeriod() throws IOException {
        Transaction transaction = new Transaction(
                LocalDate.of(2024, 3, 1), null, 5.0, "Morning coffee");

        Transaction result = classifier.classify(transaction);
        assertNotEquals("Holiday", result.getCategory());
    }

    @Test
    void testIsHolidayPeriod_WithinRange() {
        // 2024 Spring Festival period: Feb 4-24
        assertTrue(classifier.isHolidayPeriod(LocalDate.of(2024, 2, 10)));
        assertTrue(classifier.isHolidayPeriod(LocalDate.of(2024, 2, 4)));  // start date
        assertTrue(classifier.isHolidayPeriod(LocalDate.of(2024, 2, 24))); // end date
    }

    @Test
    void testIsHolidayPeriod_OutsideRange() {
        assertFalse(classifier.isHolidayPeriod(LocalDate.of(2024, 2, 3)));  // before
        assertFalse(classifier.isHolidayPeriod(LocalDate.of(2024, 2, 25))); // after
        assertFalse(classifier.isHolidayPeriod(LocalDate.of(2023, 2, 10))); // wrong year
    }

    @Test
    void testRecordAndLoadCorrections() {
        Transaction transaction = new Transaction(
                LocalDate.now(), "Misc", 10.0, "Starbucks coffee");

        // Record correction
        classifier.recordCorrection(transaction, "Food");

        // Create new classifier to test loading
        AIClassifier newClassifier = new AIClassifier();

        // Test that the correction was loaded
        Transaction testTransaction = new Transaction(
                LocalDate.now(), null, 10.0, "Starbucks coffee");

        try {
            Transaction result = newClassifier.classify(testTransaction);
            assertEquals("Food", result.getCategory());
        } catch (IOException e) {
            fail("IOException occurred");
        }
    }

    // Note: The actual API call to DeepSeek is not tested here
    // In a real project, you would mock the API call
}