package ru.phystech.java2.students.belousova.database.table.impl;

import org.apache.commons.io.FileUtils;
import ru.phystech.java2.students.belousova.database.table.api.ColumnFormatException;
import ru.phystech.java2.students.belousova.database.table.api.Table;
import ru.phystech.java2.students.belousova.database.table.api.TableProvider;
import ru.phystech.java2.students.belousova.database.table.api.TableRow;
import ru.phystech.java2.students.belousova.database.table.utils.TableUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TableProviderImpl implements TableProvider {
    protected static final String TABLE_NAME_FORMAT = "[A-Za-zА-Яа-я0-9]+";
    protected final ReadWriteLock tableProviderTransactionLock = new ReentrantReadWriteLock(true);
    protected Map<String, TableImpl> tableMap = new HashMap<>();
    protected File dataDirectory;
    protected boolean isClosed = false;

    public TableProviderImpl(File directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("null directory");
        }
        if (!directory.exists()) {
            directory.mkdir();
        } else if (!directory.isDirectory()) {
            throw new IllegalArgumentException("'" + directory.getName() + "' is not a directory");
        }

        dataDirectory = directory;

        if (!directory.canRead()) {
            throw new IOException("directory is unavailable");
        }
        for (File tableFile : directory.listFiles()) {
            tableMap.put(tableFile.getName(), new TableImpl(tableFile, this));
        }
    }

    public void removeTable(String name) {
        checkIfClosed();

        if (name == null) {
            throw new IllegalArgumentException("null name");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        if (!tableMap.containsKey(name)) {
            throw new IllegalStateException("table doesn't exists");
        }

        tableProviderTransactionLock.writeLock().lock();
        try {

            File tableDirectory = new File(dataDirectory, name);
            try {
                FileUtils.deleteDirectory(tableDirectory);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            tableMap.remove(name);
        } finally {
            tableProviderTransactionLock.writeLock().unlock();
        }
    }

    @Override
    public void close() {
        if (!isClosed) {
            tableProviderTransactionLock.writeLock().lock();
            try {
                for (String tableName : tableMap.keySet()) {
                    tableMap.get(tableName).close();
                }
                isClosed = true;
            } finally {
                tableProviderTransactionLock.writeLock().unlock();
            }
        }
    }

    protected void checkIfClosed() {
        if (isClosed) {
            throw new IllegalStateException("TableProvider is closed");
        }
    }

    @Override
    public Table getTable(String name) {
        checkIfClosed();

        if (name == null) {
            throw new IllegalArgumentException("null name");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        if (!name.matches(TABLE_NAME_FORMAT)) {
            throw new IllegalArgumentException("incorrect name");
        }

        tableProviderTransactionLock.writeLock().lock();
        try {
            if (tableMap.get(name) != null) {
                if (tableMap.get(name).isClosed()) {
                    try {
                        File tableFile = new File(dataDirectory, name);
                        TableImpl table = new TableImpl(tableFile, this);
                        tableMap.put(name, table);
                        return table;
                    } catch (IOException e) {
                        throw new RuntimeException("creating new table error: " + e.getMessage());
                    }
                }
            }
        } finally {
            tableProviderTransactionLock.writeLock().unlock();
        }
        checkIfClosed();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        if (!name.matches(TABLE_NAME_FORMAT)) {
            throw new IllegalArgumentException("incorrect name");
        }

        tableProviderTransactionLock.readLock().lock();
        try {
            return tableMap.get(name);
        } finally {
            tableProviderTransactionLock.readLock().unlock();
        }
    }

    @Override
    public Table createTable(String name, List<Class<?>> columnTypes) throws IOException {
        checkIfClosed();

        if (name == null) {
            throw new IllegalArgumentException("null name");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        if (!name.matches(TABLE_NAME_FORMAT)) {
            throw new IllegalArgumentException("incorrect name");
        }


        if (columnTypes == null) {
            throw new IllegalArgumentException("ColumnTypes list is not set");
        }
        if (columnTypes.isEmpty()) {
            throw new IllegalArgumentException("ColumnTypes list is empty");
        }
        File tableFile = new File(dataDirectory, name);
        tableProviderTransactionLock.writeLock().lock();
        tableProviderTransactionLock.readLock().lock();
        try {
            if (tableMap.containsKey(name)) {
                if (tableMap.get(name).isClosed()) {
                    TableImpl table = new TableImpl(tableFile, this);
                    tableMap.put(name, table);
                    return table;
                }
                return null;
            }
            tableFile.mkdir();
            try {
                TableUtils.writeSignature(tableFile, columnTypes);
            } catch (IOException e) {
                throw new IllegalArgumentException("wrong column type table");
            }
            TableImpl table = new TableImpl(tableFile, this);
            tableMap.put(name, table);
            return table;
        } finally {
            tableProviderTransactionLock.writeLock().unlock();
            tableProviderTransactionLock.readLock().unlock();
        }
    }

    @Override
    public TableRow deserialize(Table table, String value) throws ParseException {
        checkIfClosed();

        try {
            return TableUtils.readStorableValue(value, table, this);
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("wrong data format", 0);
        }
    }

    @Override
    public String serialize(Table table, TableRow value) throws ColumnFormatException {
        checkIfClosed();

        List<Class<?>> columnTypes = new ArrayList<>();
        for (int i = 0; i < table.getColumnsCount(); i++) {
            columnTypes.add(table.getColumnType(i));
        }
        return TableUtils.writeStorableToString((TableRowImpl) value, columnTypes);
    }

    @Override
    public TableRowImpl createFor(Table table) {
        checkIfClosed();
        if (table == null) {
            throw new IllegalArgumentException("table cannot be null");
        }
        return new TableRowImpl(table);
    }

    @Override
    public TableRowImpl createFor(Table table, List<?> values)
            throws ColumnFormatException, IndexOutOfBoundsException {
        checkIfClosed();
        if (table == null) {
            throw new IllegalArgumentException("table cannot be null");
        }
        if (values == null) {
            throw new IllegalArgumentException("values cannot be null");
        }
        if (values.size() > table.getColumnsCount()) {
            throw new IndexOutOfBoundsException("too many values");
        }

        TableRowImpl storeable = new TableRowImpl(table);
        int columnIndex = 0;
        for (Object value : values) {
            storeable.setColumnAt(columnIndex, value);
            columnIndex++;
        }
        return storeable;
    }

    @Override
    public String toString() {
        checkIfClosed();
        return getClass().getSimpleName() + "[" + dataDirectory.getAbsolutePath() + "]";
    }
}
