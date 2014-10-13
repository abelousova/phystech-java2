package ru.phystech.java2.students.belousova.database.shell.commands;

import org.springframework.stereotype.Service;
import ru.phystech.java2.students.belousova.database.shell.api.Command;
import ru.phystech.java2.students.belousova.database.state.DatabaseState;

import java.io.IOException;
import java.io.PrintStream;

@Service
public class CommandCommit extends DatabaseCommand {
    @Override
    public String getName() {
        return "commit";
    }

    @Override
    public void execute(String[] args, PrintStream outputStream, PrintStream errorStream) throws IOException {
        if (state.getCurrentTable() == null) {
            outputStream.println("no table");
        } else {
            outputStream.println(state.commitCurrentTable());
        }
    }

    @Override
    public int getArgCount() {
        return 0;
    }
}
