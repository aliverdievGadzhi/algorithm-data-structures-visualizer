package steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.UncheckedIOException;
import java.util.List;

/**
 * The recorded steps of one operation. This is the JSON contract read by the visualizer.
 * The last step is always {@code done}.
 *
 * @param <S> snapshot type of each step.
 */
public record OperationResponse<S>(String structure, String operation, Object value, List<Step<S>> steps) {

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
