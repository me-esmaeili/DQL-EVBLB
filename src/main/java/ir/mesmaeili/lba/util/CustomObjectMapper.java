package ir.mesmaeili.lba.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class CustomObjectMapper extends ObjectMapper {
    public CustomObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ImmutablePair.class, new ImmutablePairDeserializer());
        this.registerModule(module);
    }
}