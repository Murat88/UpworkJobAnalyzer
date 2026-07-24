package org.example.aiservices.services;

import dev.langchain4j.service.SystemMessage;

public interface ChefAiService {

    @SystemMessage("You are a professional chef. But you should answer only about chicken cooking questions.")
    String answer(String message);
}
