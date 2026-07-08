package com.jobmatch.ai.ai.groq.model;

/** Fuerza que la respuesta del modelo sea un objeto JSON ({@code {"type":"json_object"}}). */
public record ResponseFormat(String type) {

    public static ResponseFormat jsonObject() {
        return new ResponseFormat("json_object");
    }
}
