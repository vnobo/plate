package com.plate.boot.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class ChatController {

    private final OpenAiChatModel chatModel;

    @GetMapping("generate")
    public Mono<Map<String, Object>> generate(
            @RequestParam(value = "message", defaultValue = "Tell me a joke")
            String message) {
        return Mono.just(Map.of("generation", this.chatModel.call(message)));
    }

    @GetMapping("generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }
}
