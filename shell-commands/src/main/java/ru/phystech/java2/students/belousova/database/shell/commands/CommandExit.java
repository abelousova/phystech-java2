package ru.phystech.java2.students.belousova.database.shell.commands;

import org.springframework.stereotype.Service;
import ru.phystech.java2.students.belousova.database.shell.api.Command;
import ru.phystech.java2.students.belousova.database.state.DatabaseState;

import java.io.PrintStream;

@Service
public class CommandExit extends DatabaseCommand {

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public int getArgCount() {
        return 0;
    }

    @Override
    public void execute(String[] args, PrintStream outputStream, PrintStream errorStream) {
        outputStream.println("exit");
        System.exit(0);
    }
}
