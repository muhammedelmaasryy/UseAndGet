package com.useandget.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneNumberValidatorTest {

    @Test
    void isValid_acceptsWellFormedEgyptianMobileNumber() {
        assertTrue(PhoneNumberValidator.isValid("01001234567"));
        assertTrue(PhoneNumberValidator.isValid("01123456789"));
    }

    @Test
    void isValid_rejectsWrongLength() {
        assertFalse(PhoneNumberValidator.isValid("0100123456"));
        assertFalse(PhoneNumberValidator.isValid("010012345678"));
    }

    @Test
    void isValid_rejectsNonNumericOrNull() {
        assertFalse(PhoneNumberValidator.isValid("0100abc4567"));
        assertFalse(PhoneNumberValidator.isValid(null));
        assertFalse(PhoneNumberValidator.isValid(""));
    }

    @Test
    void isValid_rejectsWrongPrefix() {
        assertFalse(PhoneNumberValidator.isValid("02001234567"));
    }
}
