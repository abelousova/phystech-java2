package ru.phystech.java2.students.belousova.database.shell.commands;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintStream;

@Service
public class CommandDrop extends DatabaseCommand {

    @Override
    public String getName() {
        return "drop";
    }

    @Override
    public void execute(String[] args, PrintStream outputStream, PrintStream errorStream) throws IOException {
        String key = args[1];
        if (!state.getTable(key)) {
            outputStream.println(key + " not exists");
        } else {
            if (state.getCurrentTable() != null && state.getCurrentTable().equals(key)) {
                state.resetCurrentTable();
            }
            outputStream.println("dropped");
            state.removeTable(key);
        }
    }

    @Override
    public int getArgCount() {
        return 1;
    }
}
