package com.kube.demo.monitoring.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.opentelemetry.semconv.ResourceAttributes.*;

/**
 * https://opentelemetry.io/docs/languages/java/exporters/#usage
 ***/
@Configuration
public class OtlpConfig {

    @Bean
    public OpenTelemetry openTelemetry(@Value("${tracing.url}") String endpoint,
                                       @Value("${spring.application.name}") String serviceName,
                                       @Value("${service.version:0.1.0}") String serviceVersion) {
        Resource resource = Resource.getDefault().toBuilder()
                .put(SERVICE_NAME, serviceName)
                .put(SERVICE_NAMESPACE, "spring")
                .put(SERVICE_VERSION, serviceVersion).build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(
                        BatchSpanProcessor
                                .builder(OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).build())
                                .build())
                .setResource(resource)
                .build();

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(
                        PeriodicMetricReader
                                .builder(OtlpGrpcMetricExporter.builder().setEndpoint(endpoint).build())
                                .build())
                .setResource(resource)
                .build();

        SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
                .addLogRecordProcessor(
                        BatchLogRecordProcessor
                                .builder(OtlpGrpcLogRecordExporter.builder().setEndpoint(endpoint).build())
                                .build())
                .setResource(resource)
                .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setMeterProvider(sdkMeterProvider)
                .setLoggerProvider(sdkLoggerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();
        // install log agent in log appender
        OpenTelemetryAppender.install(openTelemetry);
        return openTelemetry;
    }
}
