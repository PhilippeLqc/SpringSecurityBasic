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
    private String author;
    // TODO: Add a field to store the date of the message
    // TODO: Add a field to store the time of the message
    // TODO: Add a field to store the author of the message
    // TODO: Add a field to store the recipient of the message

}
