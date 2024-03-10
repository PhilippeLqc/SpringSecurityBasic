package com.example.application.Class;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    @ManyToOne
    private User user;
    // TODO: Add a field to store the date of the message
    // TODO: Add a field to store the time of the message
    // TODO: Add a field to store the status of the message (read, unread, etc.)
    // TODO: Add a field to store the type of the message (text, image, etc.)
    // TODO: Add a field to store the message's receiver

}
