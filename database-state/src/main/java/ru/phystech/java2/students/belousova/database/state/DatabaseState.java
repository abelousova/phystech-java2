package ru.phystech.java2.students.belousova.database.state;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.phystech.java2.students.belousova.database.table.api.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseState {
    @Value("${db.path}")
    private String path;
    private TableProvider tableProvider;
    private Table currentTable;

    @Autowired
    public void setTableProvider(TableProviderFactory tableProviderFactory) throws IOException {
        tableProvider = tableProviderFactory.create(path);
    }

    public boolean getTable(String name) {
        return (tableProvider.getTable(name) != null);
    }

    public boolean createTable(String name) {
        throw new UnsupportedOperationException("you can't create table without a signature in this version");
    }

    public boolean createTableWithSignature(String name, String[] signature) throws IOException {
        if (tableProvider.getTable(name) != null) {
            return false;
        }
        List<Class<?>> columnTypes = new ArrayList<>();
        for (String type : signature) {
            TypesEnum typesEnum = TypesEnum.getBySignature(type);
            if (typesEnum == null) {
                throw new IOException("bad value type: " + type);
            }
            columnTypes.add(typesEnum.getClazz());
        }
        tableProvider.createTable(name, columnTypes);
        return true;
    }

    public void removeTable(String name) {
        try {
            tableProvider.removeTable(name);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public void setCurrentTable(String name) {
        currentTable = tableProvider.getTable(name);
    }

    public void resetCurrentTable() {
        currentTable = null;
    }

    public String getFromCurrentTable(String key) {
        TableRow storeable = currentTable.get(key);
        if (storeable == null) {
            return null;
        }
        return tableProvider.serialize(currentTable, storeable);
    }

    public String putToCurrentTable(String key, String value) {
        try {
            TableRow newValue = tableProvider.deserialize(currentTable, value);
            TableRow oldValue = currentTable.put(key, newValue);
            if (oldValue != null) {
                return tableProvider.serialize(currentTable, oldValue);
            } else {
                return null;
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public String removeFromCurrentTable(String key) {
        TableRow oldValue = currentTable.remove(key);
        if (oldValue != null) {
            return tableProvider.serialize(currentTable, oldValue);
        } else {
            return null;
        }
    }

    public String getCurrentTable() {
        if (currentTable == null) {
            return null;
        }
        return currentTable.getName();
    }

    public int commitCurrentTable() {
        try {
            return currentTable.commit();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public int sizeOfCurrentTable() {
        return currentTable.size();
    }

    public int rollbackCurrentTable() {
        return currentTable.rollback();
    }

    public int getChangesCountOfCurrentTable() {
        return currentTable.getChangesCount();
    }
}
