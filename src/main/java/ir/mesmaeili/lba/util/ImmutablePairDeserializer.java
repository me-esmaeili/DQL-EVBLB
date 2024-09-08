package ir.mesmaeili.lba.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;

public class ImmutablePairDeserializer extends JsonDeserializer<ImmutablePair<Integer, Integer>> {
    @Override
    public ImmutablePair<Integer, Integer> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String key = node.fieldNames().next();
        int value = node.get(key).asInt();
        return new ImmutablePair<>(Integer.parseInt(key), value);
    }
}