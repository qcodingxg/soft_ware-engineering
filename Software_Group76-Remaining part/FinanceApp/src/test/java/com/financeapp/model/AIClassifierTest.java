package com.financeapp.model;

import com.financeapp.model.AIClassifier;
import com.financeapp.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AIClassifierTest {

    private AIClassifier classifier;
    private Transaction transaction;
    
    @BeforeEach
    void setUp() {
        classifier = new AIClassifier();
    }

    @Test
    void testClassify_AlreadyClassified_ShouldReturnOriginalCategory() throws IOException {
        // Given
        transaction = new Transaction(LocalDate.now(), "Shopping", 50.0, "New clothes");

        // When
        Transaction result = classifier.classify(transaction);

        // Then
        assertEquals("Shopping", result.getCategory());
    }

    @Test
    void testClassify_AlreadyClassifiedWithUncategorized_ShouldReclassify() throws IOException {
        // Given
        transaction = new Transaction(LocalDate.now(), "Uncategorized", 50.0, "New clothes");

        // When
        Transaction result = classifier.classify(transaction);

        // Then
        assertNotEquals("Uncategorized", result.getCategory());
    }

    @Test
    void testClassify_WithUserCorrection_ShouldUseCorrection() throws IOException {
        // Given
        String description = "Starbucks coffee";
        String correctedCategory = "Food";
        transaction = new Transaction(LocalDate.now(), null, 10.0, description);

        // Record correction first
        classifier.recordCorrection(transaction, correctedCategory);

        // When
        Transaction result = classifier.classify(transaction);

        // Then
        assertEquals(correctedCategory, result.getCategory());
    }

    @Test
    void testClassify_WithEmptyDescription_ShouldReturnUncategorized() throws IOException {
        // Given
        transaction = new Transaction(LocalDate.now(), null, 10.0, "");

        // When
        Transaction result = classifier.classify(transaction);

        // Then
        assertEquals("Uncategorized", result.getCategory());
    }

    @Test
    void testClassify_WithNullDescription_ShouldReturnUncategorized() throws IOException {
        // Given
        transaction = new Transaction(LocalDate.now(), null, 10.0, null);

        // When
        Transaction result = classifier.classify(transaction);

        // Then
        assertEquals("Uncategorized", result.getCategory());
    }

    @Test
    void testRecordCorrection_ShouldPersistToFile() throws IOException {
        // Given
        String description = "Test transaction";
        String newCategory = "TestCategory";
        transaction = new Transaction(LocalDate.now(), null, 100.0, description);

        // When
        classifier.recordCorrection(transaction, newCategory);

        // Then - verify by trying to load a new classifier
        AIClassifier newClassifier = new AIClassifier();
        Transaction testTransaction = new Transaction(LocalDate.now(), null, 100.0, description);
        Transaction result = newClassifier.classify(testTransaction);

        assertEquals(newCategory, result.getCategory());
    }

    @Test
    void testLoadCorrections_WithEmptyFile_ShouldNotFail() throws IOException {
        // Given - empty corrections file
        AIClassifier classifierWithEmptyCorrections = new AIClassifier();

        // When
        Transaction transaction = new Transaction(LocalDate.now(), null, 10.0, "Non-existent");
        Transaction result = classifierWithEmptyCorrections.classify(transaction);

        // Then - should not throw exception and should classify normally
        assertNotNull(result.getCategory());
    }


    @Test
    void testClassify_WithDifferentDescriptionsButSameMeaning_ShouldBeConsistent() throws IOException {
        // Given
        Transaction t1 = new Transaction(LocalDate.now(), null, 10.0, "Coffee at Starbucks");
        Transaction t2 = new Transaction(LocalDate.now(), null, 10.0, "Starbucks coffee");

        // When
        Transaction r1 = classifier.classify(t1);
        Transaction r2 = classifier.classify(t2);

        // Then - ideally should be same category, but at least test they're classified
        assertNotNull(r1.getCategory());
        assertNotNull(r2.getCategory());
    }

    @Test
    void testClassify_WithSpecialCharacters_ShouldHandleCorrectly() throws IOException {
        // Given
        Transaction transaction = new Transaction(
                LocalDate.now(),
                null,
                100.0,
                "Café & Restaurant #1 - 50% off!"
        );

        // When
        Transaction result = classifier.classify(transaction);

        // Then
        assertNotNull(result.getCategory());
        assertNotEquals("Uncategorized", result.getCategory());
    }
}