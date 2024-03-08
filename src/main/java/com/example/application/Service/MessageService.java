package com.example.application.Service;

import com.example.application.Class.Message;
import com.example.application.Class.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.application.Repository.MessageRepository;

@AllArgsConstructor
@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public void create(Message message) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        message.setUser(user);
        this.messageRepository.save(message);
    }
}
