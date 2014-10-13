package ru.phystech.java2.students.belousova.database.shell.impl;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.phystech.java2.students.belousova.database.shell.api.Command;
import ru.phystech.java2.students.belousova.database.shell.api.Shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

public class ShellImpl<TState> implements Shell {
    private TState state;
    private Map<String, Command<TState>> commandMap = new HashMap<>();
    final Logger logger = LoggerFactory.getLogger(ShellImpl.class);

    @Autowired
    public void setCommandMap(List<Command<TState>> commandList) {
        for (Command<TState> command : commandList) {
            commandMap.put(command.getName(), command);
        }
    }

    @Override
    public void launch(InputStream inputStream, PrintStream outputStream, PrintStream errorStream) {
        do {
            outputStream.print("$ ");
            Scanner scanner = new Scanner(inputStream);
            String s = scanner.nextLine();
            try {
                stringHandle(s, commandMap, outputStream, errorStream);
            } catch (IOException e) {
                errorStream.println(e.getMessage());
            }
        } while (!Thread.currentThread().isInterrupted());
    }

    @Override
    public void launch(String[] args, PrintStream outputStream, PrintStream errorStream) {
        Joiner joiner = Joiner.on(' ');
        String s = joiner.join(args);
        try {
            stringHandle(s, commandMap, outputStream, errorStream);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
    }

    private void stringHandle(String s, Map<String, Command<TState>> commandList,
                              PrintStream outputStream, PrintStream errorStream) throws IOException {
        String[] commands = s.trim().split("\\s*;\\s*");

        for (String com : commands) {
            String[] tokens = com.split("\\s+", 3);
            try {
                String commandName = tokens[0];
                if (!commandList.containsKey(commandName)) {
                    logger.warn("Invalid command");
                    throw new IOException("Invalid command");
                }

                Command<TState> command = commandList.get(commandName);
                if (command.getArgCount() + 1 > tokens.length) {
                    logger.warn("missing file operands");
                    throw new IOException("missing file operand");
                }
                if (command.getArgCount() + 1 < tokens.length) {
                    logger.warn("too many arguments");
                    throw new IOException("too many arguments");
                }
                logger.debug("execution begin");
                command.execute(tokens, outputStream, errorStream);
                logger.debug("execution ended");
            } catch (IOException e) {
                logger.debug("catch exception, wrap message, rethrow");
                throw new IOException("wrong type (" + tokens[0] + ": " + e.getMessage() + ")", e);
            }
        }
    }
}