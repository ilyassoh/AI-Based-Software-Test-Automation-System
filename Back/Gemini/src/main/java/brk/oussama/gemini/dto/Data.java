package brk.oussama.gemini.dto;

import brk.oussama.gemini.service.TestType;

public record Data(
         String file,
         TestType testType,
       String additionalPrompt
) {
}
