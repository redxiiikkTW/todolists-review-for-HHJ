package com.example.todolists_springboot.handler.exception;

public class TaskNotFoundException extends RuntimeException{
    public TaskNotFoundException(){
        super("Task not found");
    }
}
