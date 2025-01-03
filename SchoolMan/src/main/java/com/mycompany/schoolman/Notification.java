/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.schoolman;

/**
 *
 * @author Administrator
 */
class Notification {
    private final String message;
    private final String noteId;

    public Notification(String message, String noteId) {
        this.message = message;
        this.noteId = noteId;
    }

    public String getMessage() {
        return message;
    }

    public String getNoteId() {
        return noteId;
    }

    @Override
    public String toString() {
        return message; // When JList displays, it will show the message
    }
}


  