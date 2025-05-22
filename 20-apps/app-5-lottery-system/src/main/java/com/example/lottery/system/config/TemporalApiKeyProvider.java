package com.example.lottery.system.config;

import io.grpc.Metadata;
import io.temporal.serviceclient.GrpcMetadataProvider;

public class TemporalApiKeyProvider implements GrpcMetadataProvider {
    private final String apiKey;

    public TemporalApiKeyProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Metadata getMetadata() {
        Metadata headers = new Metadata();
        Metadata.Key<String> authKey =
                Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(authKey, "Bearer " + apiKey);
        return headers;
    }
}