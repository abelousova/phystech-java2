package ru.phystech.java2.students.belousova.database.table.api;

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Управляющий класс для работы с {@link Table таблицами}
 *
 * Предполагает, что актуальная версия с устройства хранения, сохраняется при создании
 * экземпляра объекта. Далее ввод-вывод выполняется только в момент создания и удаления
 * таблиц.
 *
 * Данный интерфейс не является потокобезопасным.
 */
public interface TableProvider extends Closeable {

    /**
     * Возвращает таблицу с указанным названием.
     *
     * Последовательные вызовы метода с одинаковыми аргументами должны возвращать один и тот же объект таблицы,
     * если он не был удален с помощью {@link #removeTable(String)}.
     *
     * @param name Название таблицы.
     * @return Объект, представляющий таблицу. Если таблицы с указанным именем не существует, возвращает null.
     *
     * @throws IllegalArgumentException Если название таблицы null или имеет недопустимое значение.
     */
    Table getTable(String name);

    /**
     * Создаёт таблицу с указанным названием.
     * Создает новую таблицу. Совершает необходимые дисковые операции.
     *
     * @param name Название таблицы.
     * @param columnTypes Типы колонок таблицы. Не может быть пустой.
     * @return Объект, представляющий таблицу. Если таблица с указанным именем существует, возвращает null.
     *
     * @throws IllegalArgumentException Если название таблицы null или имеет недопустимое значение. Если список типов
     *                                  колонок null или содержит недопустимые значения.
     * @throws java.io.IOException При ошибках ввода/вывода.
     */
    Table createTable(String name, List<Class<?>> columnTypes) throws IOException;

    /**
     * Удаляет существующую таблицу с указанным названием.
     *
     * Объект удаленной таблицы, если был кем-то взят с помощью {@link #getTable(String)},
     * с этого момента должен бросать {@link IllegalStateException}.
     *
     * @param name Название таблицы.
     *
     * @throws IllegalArgumentException Если название таблицы null или имеет недопустимое значение.
     * @throws IllegalStateException Если таблицы с указанным названием не существует.
     * @throws java.io.IOException - при ошибках ввода/вывода.
     */
    void removeTable(String name) throws IOException;

    /**
     * Преобразовывает строку в объект {@link TableRow}, соответствующий структуре таблицы.
     *
     * @param table Таблица, которой должен принадлежать {@link TableRow}.
     * @param value Строка, из которой нужно прочитать {@link TableRow}.
     * @return Прочитанный {@link TableRow}.
     *
     * @throws java.text.ParseException - при каких-либо несоответстиях в прочитанных данных.
     */
    TableRow deserialize(Table table, String value) throws ParseException;

    /**
     * Преобразовывает объект {@link TableRow} в строку.
     *
     * @param table Таблица, которой должен принадлежать {@link TableRow}.
     * @param value {@link TableRow}, который нужно записать.
     * @return Строка с записанным значением.
     *
     * @throws ColumnFormatException При несоответствии типа в {@link TableRow} и типа колонки в таблице.
     */
    String serialize(Table table, TableRow value) throws ColumnFormatException;

    /**
     * Создает новый пустой {@link TableRow} для указанной таблицы.
     *
     * @param table Таблица, которой должен принадлежать {@link TableRow}.
     * @return Пустой {@link TableRow}, нацеленный на использование с этой таблицей.
     */
    TableRow createFor(Table table);

    /**
     * Создает новый {@link TableRow} для указанной таблицы, подставляя туда переданные значения.
     *
     * @param table Таблица, которой должен принадлежать {@link TableRow}.
     * @param values Список значений, которыми нужно проинициализировать поля Storeable.
     * @return {@link TableRow}, проинициализированный переданными значениями.
     * @throws ColumnFormatException При несоответствии типа переданного значения и колонки.
     * @throws IndexOutOfBoundsException При несоответствии числа переданных значений и числа колонок.
     */
    TableRow createFor(Table table, List<?> values) throws ColumnFormatException, IndexOutOfBoundsException;
}

