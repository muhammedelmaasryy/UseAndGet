package com.useandget.util;

import java.util.regex.Pattern;

public final class PhoneNumberValidator {

    private static final Pattern EGYPT_MOBILE_PATTERN = Pattern.compile("^01[0125]\\d{8}$");

    private PhoneNumberValidator() {}

    public static boolean isValid(String phoneNumber) {
        return phoneNumber != null && EGYPT_MOBILE_PATTERN.matcher(phoneNumber).matches();
    }

    public static String normalize(String phoneNumber) {
        return phoneNumber == null ? null : phoneNumber.trim();
    }
}
