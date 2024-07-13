package org.kif.reincarceration.util;

public class RomanNumeralUtil {

    private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    public static String toRoman(int number) {
        ConsoleUtil.sendDebug("Converting " + number + " to Roman numeral");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < VALUES.length; i++) {
            while (number >= VALUES[i]) {
                result.append(SYMBOLS[i]);
                number -= VALUES[i];
            }
        }
        ConsoleUtil.sendDebug("Converted " + number + " to Roman numeral: " + result);
        return result.toString();
    }
}
