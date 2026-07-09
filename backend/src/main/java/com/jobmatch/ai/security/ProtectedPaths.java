package com.jobmatch.ai.security;

import java.util.List;
import java.util.stream.Stream;

/** Prefijos de rutas protegidas por API key y rate limiting, compartidos por los distintos filtros. */
public final class ProtectedPaths {

    public static final List<String> PREFIXES = List.of("/api/v1/analyses", "/api/v1/answers", "/api/v1/cv");

    private ProtectedPaths() {
    }

    public static boolean matches(String uri) {
        return PREFIXES.stream().anyMatch(uri::startsWith);
    }

    /** Patrones con comodín (`/prefix` y `/prefix/*`) para registrar filtros por URL. */
    public static String[] patterns() {
        return PREFIXES.stream().flatMap(prefix -> Stream.of(prefix, prefix + "/*")).toArray(String[]::new);
    }
}
