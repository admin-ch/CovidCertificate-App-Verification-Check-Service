package ch.admin.bag.covidcertificate.backend.verification.check.ws.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.Instant;

public class CustomInstantSerializer extends StdSerializer<Instant> {

    public CustomInstantSerializer() {
        this(null);
    }

    public CustomInstantSerializer(Class<Instant> t) {
        super(t);
    }

    @Override
    public void serialize(
            Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeNumber(instant.getEpochSecond());
    }
}
