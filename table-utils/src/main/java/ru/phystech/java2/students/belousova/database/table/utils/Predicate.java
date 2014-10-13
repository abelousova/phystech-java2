package ru.phystech.java2.students.belousova.database.table.utils;

public interface Predicate<T> {
    boolean apply(T input);
}
