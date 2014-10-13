package ru.phystech.java2.students.belousova.database.table.impl;

import ru.phystech.java2.students.belousova.database.table.api.ColumnFormatException;
import ru.phystech.java2.students.belousova.database.table.api.Table;
import ru.phystech.java2.students.belousova.database.table.api.TableRow;
import ru.phystech.java2.students.belousova.database.table.utils.TableUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TableImpl implements Table {
    private List<Class<?>> columnTypes = new ArrayList<>();
    TableProviderImpl tableProvider = null;

    protected Map<String, TableRow> dataBase = new HashMap<>();
    protected ThreadLocal<Map<String, TableRow>> addedKeys;
    protected ThreadLocal<Set<String>> deletedKeys;

    protected final Lock tableTransactionsLock = new ReentrantLock(true);

    protected File dataDirectory = null;

    protected AtomicBoolean isClosed = new AtomicBoolean(false);

    public TableImpl(File directory, TableProviderImpl tableProvider) throws IOException {
        dataDirectory = directory;
        this.tableProvider = tableProvider;
        File signatureFile = new File(directory, "signature.tsv");
        TableUtils.readSignature(signatureFile, columnTypes);
        TableUtils.readTable(directory, this, dataBase, tableProvider);
        addedKeys = new ThreadLocal<Map<String, TableRow>>() {
            @Override
            public Map<String, TableRow> initialValue() {
                return new HashMap<>();
            }
        };
        deletedKeys = new ThreadLocal<Set<String>>() {
            @Override
            public Set<String> initialValue() {
                return new HashSet<>();
            }
        };
    }

    public String getName() {
        checkIfClosed();
        return dataDirectory.getName();
    }

    public int size() {
        checkIfClosed();

        tableTransactionsLock.lock();
        try {
            for (String key : deletedKeys.get()) {
                if (!dataBase.containsKey(key)) {
                    deletedKeys.get().remove(key);
                }
            }
            Set<String> addedKeysSet = addedKeys.get().keySet();
            Set<String> addedKeysForDeletion = new HashSet<>();
            for (String key : addedKeysSet) {
                if (dataBase.containsKey(key)) {
                    if (dataBase.get(key).equals(addedKeys.get().get(key))) {
                        addedKeysForDeletion.add(key);
                        if (deletedKeys.get().contains(key)) {
                            deletedKeys.get().remove(key);
                        }
                    } else {
                        if (!deletedKeys.get().contains(key)) {
                            deletedKeys.get().add(key);
                        }
                    }
                }
            }
            addedKeys.get().keySet().removeAll(addedKeysForDeletion);
            return dataBase.size() + addedKeys.get().size() - deletedKeys.get().size();
        } finally {
            tableTransactionsLock.unlock();
        }
    }

    protected int countChanges() {
        checkIfClosed();

        int changesCounter = addedKeys.get().size() + deletedKeys.get().size();

        for (String key : addedKeys.get().keySet()) {
            if (deletedKeys.get().contains(key)) {
                changesCounter--;
                if (dataBase.get(key).equals(addedKeys.get().get(key))) {
                    changesCounter--;
                }
            }
        }

        return changesCounter;
    }

    public int rollback() {
        checkIfClosed();

        tableTransactionsLock.lock();
        int counter;
        try {
            counter = countChanges();
        } finally {
            tableTransactionsLock.unlock();
        }
        deletedKeys.get().clear();
        addedKeys.get().clear();
        return counter;
    }

    public int getChangesCount() {
        checkIfClosed();

        tableTransactionsLock.lock();
        try {
            return countChanges();
        } finally {
            tableTransactionsLock.unlock();
        }
    }

    public void close() {
        tableTransactionsLock.lock();
        try {
            if (!isClosed.get()) {
                rollback();
                isClosed.set(true);
            }
        } finally {
            tableTransactionsLock.unlock();
        }

    }

    public void checkIfClosed() {
        if (isClosed.get()) {
            throw new IllegalStateException("Table is closed");
        }
    }

    @Override
    public TableRow put(String key, TableRow value) {
        if (key == null) {
            throw new IllegalArgumentException("null key");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("empty key");
        }
        if (value == null) {
            throw new IllegalArgumentException("null value");
        }
        if (value.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("empty value");
        }

        if (!TableUtils.isStorableValid(value, columnTypes)) {
            throw new ColumnFormatException("wrong storeable format");
        }

        if (key.matches(".*\\s+.*")) {
            throw new IllegalArgumentException("key with whitespaces");
        }

        checkIfClosed();

        tableTransactionsLock.lock();
        try {
            if (dataBase.containsKey(key) && !deletedKeys.get().contains(key)) {
                deletedKeys.get().add(key);
                TableRow oldValue = dataBase.get(key);
                addedKeys.get().put(key, value);
                return oldValue;
            }
        } finally {
            tableTransactionsLock.unlock();
        }
        return addedKeys.get().put(key, value);
    }

    @Override
    public TableRow remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("null key");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("empty key");
        }
        if (key.matches(".*\\s+.*")) {
            throw new IllegalArgumentException("key with whitespaces");
        }
        checkIfClosed();

        tableTransactionsLock.lock();
        try {
            if (dataBase.containsKey(key) && !deletedKeys.get().contains(key)) {
                deletedKeys.get().add(key);
                return dataBase.get(key);
            }
        } finally {
            tableTransactionsLock.unlock();
        }
        return addedKeys.get().remove(key);
    }

    @Override
    public TableRow get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("null key");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("empty key");
        }
        if (key.matches(".*\\s+.*")) {
            throw new IllegalArgumentException("key with whitespaces");
        }

        checkIfClosed();

        if (addedKeys.get().containsKey(key)) {
            return addedKeys.get().get(key);
        }
        if (deletedKeys.get().contains(key)) {
            return null;
        }

        tableTransactionsLock.lock();
        try {
            return dataBase.get(key);
        } finally {
            tableTransactionsLock.unlock();
        }
    }

    @Override
    public int commit() throws IOException {
        checkIfClosed();

        tableTransactionsLock.lock();
        try {
            int counter = countChanges();
            for (String key : deletedKeys.get()) {
                dataBase.remove(key);
            }
            dataBase.putAll(addedKeys.get());
            deletedKeys.get().clear();
            addedKeys.get().clear();
            TableUtils.writeTable(dataDirectory, this, dataBase, tableProvider);
            return counter;
        } finally {
            tableTransactionsLock.unlock();
        }
    }

    @Override
    public int getColumnsCount() {
        checkIfClosed();
        return columnTypes.size();
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        checkIfClosed();
        return columnTypes.get(columnIndex);
    }

    @Override
    public boolean isClosed() {
        return isClosed.get();
    }

    @Override
    public String toString() {
        checkIfClosed();
        return getClass().getSimpleName() + "[" + dataDirectory.getAbsolutePath() + "]";
    }
}
