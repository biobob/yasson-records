package io.biobob.tryouts.records.jsonb;

import jakarta.json.bind.annotation.JsonbCreator;

import java.time.LocalDateTime;

public record Workshop(String title, LocalDateTime date, String description) {

    /*
     * By applying annotation to compact constructor there is no need to repeat components again.
     *
     * Interesting possibility is planned for JSON-B 2.1.0 specification where we can externalize creator annotation
     * to the JsonbConfig using new API - see https://github.com/eclipse-ee4j/jsonb-api/issues/88
     * On the other hand we will lose advantage of no need to specify constructor parameters.
     */
    @JsonbCreator
    public Workshop {}

}
