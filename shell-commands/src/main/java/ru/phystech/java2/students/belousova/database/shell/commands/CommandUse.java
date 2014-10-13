package ru.phystech.java2.students.belousova.database.shell.commands;

import org.springframework.stereotype.Service;
import ru.phystech.java2.students.belousova.database.shell.api.Command;
import ru.phystech.java2.students.belousova.database.state.DatabaseState;

import java.io.IOException;
import java.io.PrintStream;

@Service
public class CommandUse extends DatabaseCommand {

    @Override
    public String getName() {
        return "use";
    }

    @Override
    public void execute(String[] args, PrintStream outputStream, PrintStream errorStream) throws IOException {
        String tableName = args[1];
        if (!state.getTable(tableName)) {
            outputStream.println(tableName + " not exists");
        } else {
            if (state.getCurrentTable() != null) {
                if (state.getChangesCountOfCurrentTable() > 0) {
                    outputStream.println(state.getChangesCountOfCurrentTable() + " unsaved changes");
                    return;
                }
            }
            state.setCurrentTable(tableName);
            outputStream.println("using " + tableName);
        }
    }

    @Override
    public int getArgCount() {
        return 1;
    }
}
