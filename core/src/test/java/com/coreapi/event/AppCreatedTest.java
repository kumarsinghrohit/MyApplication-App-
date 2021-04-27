package com.coreapi.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

@DisplayName("Testing AppCreated")
@Tag("Bean")
public class AppCreatedTest {
    @Test
    @DisplayName("Testing hashcode and equals method's contract")
    public void equalsContract() {
        EqualsVerifier.forClass(AppCreated.class).usingGetClass().verify();
    }
}
