package ru.phystech.java2.students.belousova.database.shell.commands;

import org.springframework.stereotype.Service;
import ru.phystech.java2.students.belousova.database.shell.api.Command;
import ru.phystech.java2.students.belousova.database.state.DatabaseState;

import java.io.IOException;
import java.io.PrintStream;

@Service
public class CommandCreate extends DatabaseCommand {

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public void execute(String[] args, PrintStream outputStream, PrintStream errorStream) throws IOException {
        String key = args[1];
        if (state.getTable(key)) {
            System.out.println(key + " exists");
        } else {
            String signature = args[2].trim();
            if (!signature.startsWith("(") || !signature.endsWith(")")) {
                throw new IOException("wrong argument type");
            }
            signature = signature.substring(1, signature.length() - 1);
            String[] types = signature.split("\\s+");
            state.createTableWithSignature(key, types);
            System.out.println("created");
        }
    }

    @Override
    public int getArgCount() {
        return 1;
    }
}
