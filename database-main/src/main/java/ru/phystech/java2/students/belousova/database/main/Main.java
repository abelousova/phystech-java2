package ru.phystech.java2.students.belousova.database.main;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.phystech.java2.students.belousova.database.shell.impl.DatabaseShellImpl;

public class Main {
    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        DatabaseShellImpl shell = ctx.getBean(DatabaseShellImpl.class);
        shell.launch(System.in, System.out, System.err);
    }
}
