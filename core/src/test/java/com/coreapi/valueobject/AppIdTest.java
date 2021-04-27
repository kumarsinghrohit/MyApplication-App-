package com.coreapi.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import com.coreapi.exception.InvalidAppIdException;

@DisplayName("Testing App Id value object.")
@Tag("Bean")
class AppIdTest {

    @DisplayName("Test App Id with valid data.")
    @Test
    void testAppIdWithValidData() {
        AppId appId = new AppId("AppId");
        assertEquals("AppId", appId.getValue());
    }

    @DisplayName("Test App Id with invalid data.")
    @Test
    void testAppIdWithInvalidData() {
        assertThrows(InvalidAppIdException.class,
                () -> new AppId(""));
    }

    @Test
    @DisplayName("Testing hashcode and equals method's contract")
    public void equalsContract() {
        EqualsVerifier.forClass(AppId.class).usingGetClass().verify();
    }
}
