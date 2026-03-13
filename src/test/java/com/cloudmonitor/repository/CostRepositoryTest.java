package com.cloudmonitor.repository;

import com.cloudmonitor.model.CostData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests using @DataJpaTest with in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
class CostRepositoryTest {

    @Autowired
    private CostRepository costRepository;

    @BeforeEach
    void setUp() {
        costRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and retrieve cost data")
    void testSaveAndFind() {
        // Arrange
        CostData costData = new CostData(
                LocalDate.of(2026, 3, 1),
                "Amazon EC2",
                "BoxUsage",
                new BigDecimal("25.50"),
                "USD"
        );

        // Act
        CostData saved = costRepository.save(costData);
        Optional<CostData> found = costRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Amazon EC2", found.get().getServiceName());
        assertEquals(new BigDecimal("25.500000"), found.get().getAmount());
    }

    @Test
    @DisplayName("Should find cost data by date range")
    void testFindByDateBetween() {
        // Arrange
        costRepository.save(new CostData(LocalDate.of(2026, 2, 28), "EC2", null,
                new BigDecimal("10"), "USD"));
        costRepository.save(new CostData(LocalDate.of(2026, 3, 1), "EC2", null,
                new BigDecimal("15"), "USD"));
        costRepository.save(new CostData(LocalDate.of(2026, 3, 15), "EC2", null,
                new BigDecimal("20"), "USD"));
        costRepository.save(new CostData(LocalDate.of(2026, 4, 1), "EC2", null,
                new BigDecimal("25"), "USD"));

        // Act
        List<CostData> marchData = costRepository.findByDateBetween(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );

        // Assert
        assertEquals(2, marchData.size());
    }

    @Test
    @DisplayName("Should find cost data by service name and date range")
    void testFindByServiceNameAndDateBetween() {
        // Arrange
        costRepository.save(new CostData(LocalDate.of(2026, 3, 1), "Amazon EC2", null,
                new BigDecimal("10"), "USD"));
        costRepository.save(new CostData(LocalDate.of(2026, 3, 1), "Amazon S3", null,
                new BigDecimal("5"), "USD"));
        costRepository.save(new CostData(LocalDate.of(2026, 3, 2), "Amazon EC2", null,
                new BigDecimal("12"), "USD"));

        // Act
        List<CostData> ec2Data = costRepository.findByServiceNameAndDateBetween(
                "Amazon EC2",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );

        // Assert
        assertEquals(2, ec2Data.size());
        assertTrue(ec2Data.stream().allMatch(c -> c.getServiceName().equals("Amazon EC2")));
    }

    @Test
    @DisplayName("Should find by date and service name for upsert")
    void testFindByDateAndServiceName() {
        // Arrange
        CostData saved = costRepository.save(new CostData(
                LocalDate.of(2026, 3, 1),
                "Amazon EC2",
                null,
                new BigDecimal("25"),
                "USD"
        ));

        // Act
        Optional<CostData> found = costRepository.findByDateAndServiceName(
                LocalDate.of(2026, 3, 1),
                "Amazon EC2"
        );

        // Assert
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Should return empty when no matching record exists")
    void testFindByDateAndServiceName_NotFound() {
        // Act
        Optional<CostData> found = costRepository.findByDateAndServiceName(
                LocalDate.of(2026, 3, 1),
                "NonExistent Service"
        );

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should get distinct service names")
    void testFindDistinctServiceNames() {
        // Arrange
        costRepository.save(new CostData(LocalDate.of(2026, 3, 1), "Amazon EC2", null,
                new BigDecimal("10"), "USD"));
        costRepository.save(new CostData(LocalDate.of(2026, 3, 2), "Amazon EC2", null,
                new BigDecimal("15"), "USD"));
        costRepository.save(new CostData(LocalDate.of(2026, 3, 1), "Amazon S3", null,
                new BigDecimal("5"), "USD"));
        costRepository.save(new CostData(LocalDate.of(2026, 3, 1), "Amazon RDS", null,
                new BigDecimal("20"), "USD"));

        // Act
        List<String> services = costRepository.findDistinctServiceNames();

        // Assert
        assertEquals(3, services.size());
        assertTrue(services.contains("Amazon EC2"));
        assertTrue(services.contains("Amazon S3"));
        assertTrue(services.contains("Amazon RDS"));
    }

    @Test
    @DisplayName("Should calculate total cost for date range")
    void testGetTotalCostBetween() {
        // Arrange
        costRepository.save(new CostData(LocalDate.of(2026, 3, 1), "EC2", null,
                new BigDecimal("10.50"), "USD"));
        costRepository.save(new CostData(LocalDate.of(2026, 3, 2), "EC2", null,
                new BigDecimal("15.25"), "USD"));
        costRepository.save(new CostData(LocalDate.of(2026, 3, 3), "S3", null,
                new BigDecimal("5.00"), "USD"));

        // Act
        BigDecimal total = costRepository.getTotalCostBetween(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );

        // Assert
        assertEquals(new BigDecimal("30.750000"), total);
    }

    @Test
    @DisplayName("Should return zero for empty date range")
    void testGetTotalCostBetween_Empty() {
        // Act
        BigDecimal total = costRepository.getTotalCostBetween(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(total));
    }

    @Test
    @DisplayName("Should check if data exists for date")
    void testExistsByDate() {
        // Arrange
        costRepository.save(new CostData(LocalDate.of(2026, 3, 1), "EC2", null,
                new BigDecimal("10"), "USD"));

        // Act & Assert
        assertTrue(costRepository.existsByDate(LocalDate.of(2026, 3, 1)));
        assertFalse(costRepository.existsByDate(LocalDate.of(2026, 3, 2)));
    }
}
