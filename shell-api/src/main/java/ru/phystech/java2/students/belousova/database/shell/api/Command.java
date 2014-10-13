package ru.phystech.java2.students.belousova.database.shell.api;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintStream;

public abstract class Command<TState> {
    protected TState state;

    @Autowired
    public void setState(TState state) {
        this.state = state;
    }

    public abstract String getName();

    public abstract void execute(String[] args, PrintStream outputStream, PrintStream errorStream) throws IOException;

    public abstract int getArgCount();
}