package ru.phystech.java2.students.belousova.database.shell.commands;

import org.springframework.stereotype.Service;
import ru.phystech.java2.students.belousova.database.shell.api.Command;
import ru.phystech.java2.students.belousova.database.state.DatabaseState;

import java.io.IOException;
import java.io.PrintStream;

@Service
public class CommandRemove extends DatabaseCommand {

    @Override
    public int getArgCount() {
        return 1;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public void execute(String[] args, PrintStream outputStream, PrintStream errorStream) throws IOException {
        if (state.getCurrentTable() == null) {
            outputStream.println("no table");
        } else {
            String key = args[1];
            String value = state.removeFromCurrentTable(key);
            if (value == null) {
                outputStream.println("not found");
            } else {
                outputStream.println("removed");
            }
        }
    }
}
