package com.manager.events;

import com.coreapi.event.AppVersionDeleted;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import com.coreapi.event.AppVersionDeleted;

@DisplayName("Testing AppVersionDeleted")
@Tag("Bean")
public class AppVersionDeletedTest {
    @Test
    @DisplayName("Testing hashcode and equals method's contract")
    public void equalsContract() {
        EqualsVerifier.forClass(AppVersionDeleted.class).usingGetClass().verify();
    }
}
