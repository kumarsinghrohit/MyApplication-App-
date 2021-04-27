package com.manager.events;

import com.coreapi.event.AppDeleted;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import com.coreapi.event.AppDeleted;

@DisplayName("Testing AppDeleted")
@Tag("Bean")
public class AppDeletedTest {
    @Test
    @DisplayName("Testing hashcode and equals method's contract")
    public void equalsContract() {
        EqualsVerifier.forClass(AppDeleted.class).usingGetClass().verify();
    }
}
