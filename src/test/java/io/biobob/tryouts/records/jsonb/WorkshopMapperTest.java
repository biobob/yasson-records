package io.biobob.tryouts.records.jsonb;

import jakarta.json.bind.JsonbException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkshopMapperTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
        Java, Java programming language, 1999, 12, 15, 10,  0,  0, '{"date":"1999-12-15T10:00:00","description":"Java programming language","title":"Java"}'
        TDD,  Test Driven Development,   2000,  1,  1,  0,  0,  0, '{"date":"2000-01-01T00:00:00","description":"Test Driven Development","title":"TDD"}'
        BDD,  '',                        2010,  5, 22,  2, 13, 59, '{"date":"2010-05-22T02:13:59","description":"","title":"BDD"}'
        '',   '',                        2321, 12, 31, 23, 59, 59, '{"date":"2321-12-31T23:59:59","description":"","title":""}'
    """)
    void shouldSerialize(String title, String description, int year, int month, int day, int hour, int minute, int second, String expectedJson) {
        var workshop = new Workshop(title, LocalDateTime.of(year, month, day, hour, minute, second), description);
        assertEquals(expectedJson, WorkshopMapper.toJson(workshop));
    }

    @Test
    void shouldFailSerializationOfNull() {
        assertThrows(NullPointerException.class, () -> {
            WorkshopMapper.toJson(null);
        });
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        '{"date":"1998-11-14T09:08:07","description":"Java programming language","title":"Java"}', Java, Java programming language, 1998, 11, 14,  9,  8,  7
        '{"date":"2001-02-02T00:00:00","description":"Test Driven Development","title":"TDD"}',    TDD,  Test Driven Development,   2001,  2,  2,  0,  0,  0
        '{"date":"2010-05-22T02:13:59","description":"","title":"BDD"}',                           BDD,  '',                        2010,  5, 22,  2, 13, 59
        '{"date":"2321-12-31T23:59:59","description":"","title":""}',                              '',   '',                        2321, 12, 31, 23, 59, 59
    """)
    void shouldDeserialize(String json, String title, String description, int year, int month, int day, int hour, int minute, int second) {
        var workshop = WorkshopMapper.fromJson(json);
        assertNotNull(workshop);
        assertEquals(title, workshop.title());
        assertEquals(description, workshop.description());
        assertEquals(LocalDateTime.of(year, month, day, hour, minute, second), workshop.date());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        ''
        not json
        {}
        {"foo"="bar"}
        '{"a"="b","c"="d"}'
        '{"date":"","description":"","title":""}'
        '{"date":"wrong format","description":"something","title":"A"}'
        '{"date":"1980-01-01","description":"something","title":"A"}'
    """)
    void shouldFailDeserializationOfInvalidJson(String json) {
        assertThrows(JsonbException.class, () -> {
            WorkshopMapper.fromJson(json);
        });
    }

    /**
     * This is the problematic part described in the README. Specification dictates this behavior when using
     * <code>@JsonbCreator</code> annotation. The implementation respects it and following test is proving that.
     * In real world it would be much more useful to have a way for parsing JSONs with absent fields because it's common
     * practice for many frameworks to use absent field as representation of <code>null</code> field value.
     */
    @ParameterizedTest
    @CsvSource(textBlock = """
        '{"description":"Java programming language","title":"Java"}'
        '{"date":"1998-11-14T09:08:07","title":"Java"}'
        '{"date":"1998-11-14T09:08:07","description":"Java programming language"}'
        '{"title":"Java"}'
        '{"description":"Java programming language"}'
        '{"date":"1998-11-14T09:08:07"}'
    """)
    void shouldFailDeserializationOfJsonWithAbsentField(String json) {
        assertThrows(JsonbException.class, () -> {
            WorkshopMapper.fromJson(json);
        });
    }

    @Test
    void shouldFailDeserializationOfNull() {
        assertThrows(NullPointerException.class, () -> {
            WorkshopMapper.fromJson(null);
        });
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        '{"date":"1999-12-15T10:00:00","description":"Java programming language","title":"Java"}'
        '{"date":"2000-01-01T00:00:00","description":"Test Driven Development","title":"TDD"}'
        '{"date":"2010-05-22T02:13:59","description":"","title":"BDD"}'
        '{"date":"2321-12-31T23:59:59","description":"","title":""}'
    """)
    void shouldPreserveJsonEquivalence(String json) {
        var workshop = WorkshopMapper.fromJson(json);
        var roundTripJson = WorkshopMapper.toJson(workshop);
        assertEquals(json, roundTripJson);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        Java, Java programming language, 1999, 12, 15, 10,  0,  0
        TDD,  Test Driven Development,   2000,  1,  1,  0,  0,  0
        BDD,  '',                        2010,  5, 22,  2, 13, 59
        '',   '',                        2321, 12, 31, 23, 59, 59
    """)
    void shouldPreserveObjectEquivalence(String title, String description, int year, int month, int day, int hour, int minute, int second) {
        var workshop = new Workshop(title, LocalDateTime.of(year, month, day, hour, minute, second), description);
        var json = WorkshopMapper.toJson(workshop);
        var roundTripWorkshop = WorkshopMapper.fromJson(json);
        assertEquals(workshop, roundTripWorkshop);
    }

}
