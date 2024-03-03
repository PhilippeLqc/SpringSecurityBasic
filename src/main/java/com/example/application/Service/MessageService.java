package com.example.application.Service;

import com.example.application.Class.Message;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.application.Repository.MessageRepository;

@AllArgsConstructor
@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public void create(Message message) {
        this.messageRepository.save(message);
    }
}
