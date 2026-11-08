package com.SMS.student.management.system.marks;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GradeCalculator {
	private static final BigDecimal NINETY = BigDecimal.valueOf(90);
	private static final BigDecimal EIGHTY = BigDecimal.valueOf(80);
	private static final BigDecimal SEVENTY = BigDecimal.valueOf(70);
	private static final BigDecimal SIXTY = BigDecimal.valueOf(60);
	private static final BigDecimal FIFTY = BigDecimal.valueOf(50);

	private GradeCalculator() { }

	public static BigDecimal percentage(BigDecimal marksObtained, BigDecimal maximumMarks) {
		return marksObtained.multiply(BigDecimal.valueOf(100)).divide(maximumMarks, 4, RoundingMode.HALF_UP);
	}

	public static String grade(BigDecimal percentage) {
		if (percentage.compareTo(NINETY) >= 0) return "A+";
		if (percentage.compareTo(EIGHTY) >= 0) return "A";
		if (percentage.compareTo(SEVENTY) >= 0) return "B";
		if (percentage.compareTo(SIXTY) >= 0) return "C";
		if (percentage.compareTo(FIFTY) >= 0) return "D";
		return "F";
	}
}