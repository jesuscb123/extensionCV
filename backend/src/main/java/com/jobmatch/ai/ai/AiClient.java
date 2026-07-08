package com.jobmatch.ai.ai;

/** Abstracción del proveedor de IA (DIP). Implementaciones: Groq (real) y stub (dev). */
public interface AiClient {

    AiAnalysis analyze(String systemPrompt, String userPrompt);
}
