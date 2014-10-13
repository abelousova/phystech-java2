package ru.phystech.java2.students.belousova.database.shell.api;

import java.io.InputStream;
import java.io.PrintStream;

public interface Shell {
    public void launch(InputStream inputStream, PrintStream outputStream, PrintStream errorStream);
    public void launch(String[] commands, PrintStream outputStream, PrintStream errorStream);
}
