package com.SMS.student.management.system.marks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class GradeCalculatorTest {
	@Test
	void calculatesAllGradeBoundaries() {
		assertEquals("A+", GradeCalculator.grade(BigDecimal.valueOf(95)));
		assertEquals("A+", GradeCalculator.grade(BigDecimal.valueOf(90)));
		assertEquals("A", GradeCalculator.grade(BigDecimal.valueOf(89)));
		assertEquals("A", GradeCalculator.grade(BigDecimal.valueOf(80)));
		assertEquals("B", GradeCalculator.grade(BigDecimal.valueOf(79)));
		assertEquals("B", GradeCalculator.grade(BigDecimal.valueOf(70)));
		assertEquals("C", GradeCalculator.grade(BigDecimal.valueOf(69)));
		assertEquals("C", GradeCalculator.grade(BigDecimal.valueOf(60)));
		assertEquals("D", GradeCalculator.grade(BigDecimal.valueOf(59)));
		assertEquals("D", GradeCalculator.grade(BigDecimal.valueOf(50)));
		assertEquals("F", GradeCalculator.grade(BigDecimal.valueOf(49)));
		assertEquals("F", GradeCalculator.grade(BigDecimal.ZERO));
	}

	@Test
	void calculatesPercentageWithFourDecimalPlaces() {
		assertEquals(new BigDecimal("66.6667"), GradeCalculator.percentage(BigDecimal.valueOf(2), BigDecimal.valueOf(3)));
	}
}
