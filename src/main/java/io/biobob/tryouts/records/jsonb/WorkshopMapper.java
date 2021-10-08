package io.biobob.tryouts.records.jsonb;

import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;
import org.eclipse.yasson.FieldAccessStrategy;

import static java.util.Objects.*;

public interface WorkshopMapper {

    JsonbConfig config = new JsonbConfig()
            /*
             * Existing strategy from Yasson is used - this is not ideal because it is not part of specification.
             * There are two possible ways that could solve this problem:
             *   - future version of JSON-B could detect that class is actually record and use proper strategy by default
             *   - common implementations of strategies should be part of spec from 2.1.0 - see https://github.com/eclipse-ee4j/jsonb-api/issues/164
             */
            .withPropertyVisibilityStrategy(new FieldAccessStrategy());

    static String toJson(Workshop workshop) {
        try (var jsonb = JsonbBuilder.create(config)) {
            return jsonb.toJson(requireNonNull(workshop));
        } catch (JsonbException | NullPointerException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close Jsonb instance.", e);
        }
    }

    static Workshop fromJson(String json) {
        try (var jsonb = JsonbBuilder.create(config)) {
            return jsonb.fromJson(requireNonNull(json), Workshop.class);
        } catch (JsonbException | NullPointerException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close Jsonb instance.", e);
        }
    }

}
