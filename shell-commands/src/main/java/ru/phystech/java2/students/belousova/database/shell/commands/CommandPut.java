package ru.phystech.java2.students.belousova.database.shell.commands;

import org.springframework.stereotype.Service;
import ru.phystech.java2.students.belousova.database.shell.api.Command;
import ru.phystech.java2.students.belousova.database.state.DatabaseState;

import java.io.IOException;
import java.io.PrintStream;

@Service
public class CommandPut extends DatabaseCommand {

    @Override
    public String getName() {
        return "put";
    }

    @Override
    public int getArgCount() {
        return 2;
    }

    @Override
    public void execute(String[] args, PrintStream outputStream, PrintStream errorStream) throws IOException {
        if (state.getCurrentTable() == null) {
            outputStream.println("no table");
        } else {
            String key = args[1];
            String value = args[2];
            String oldValue = state.putToCurrentTable(key, value);
            if (oldValue == null) {
                outputStream.println("new");
            } else {
                outputStream.println("overwrite");
                outputStream.println(oldValue);
            }
        }
    }
}
