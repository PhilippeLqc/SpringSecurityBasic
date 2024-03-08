package com.example.application.Controller;

import com.example.application.Service.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.application.Class.Message;

@AllArgsConstructor
@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void create(@RequestBody Message message) {
        // Save the author of the message

        this.messageService.create(message);
    }
}
