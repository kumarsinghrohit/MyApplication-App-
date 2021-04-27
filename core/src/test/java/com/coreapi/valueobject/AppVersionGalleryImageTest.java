package com.coreapi.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

@DisplayName("Testing AppVersionGalleryImage value object.")
@Tag("Bean")
class AppVersionGalleryImageTest {

    @Test
    @DisplayName("Testing hashcode and equals method's contract")
    public void equalsContract() {
        EqualsVerifier.forClass(AppVersionGalleryImage.class).usingGetClass().verify();
    }
}
