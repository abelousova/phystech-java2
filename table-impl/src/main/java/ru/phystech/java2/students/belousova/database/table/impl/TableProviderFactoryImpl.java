package ru.phystech.java2.students.belousova.database.table.impl;

import org.springframework.stereotype.Service;
import ru.phystech.java2.students.belousova.database.table.api.TableProviderFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
public class TableProviderFactoryImpl implements TableProviderFactory {
    private Set<TableProviderImpl> tableProviderSet = new HashSet<>();
    private boolean isClosed = false;

    @Override
    public TableProviderImpl create(String path) throws IOException {
        if (isClosed) {
            throw new IllegalStateException("TableProviderFactory is closed");
        }
        if (path == null) {
            throw new IllegalArgumentException("Path to storage isn't set");
        }
        if (path.trim().isEmpty()) {
            throw new IllegalArgumentException("empty directory");
        }
        TableProviderImpl tableProvider = new TableProviderImpl(new File(path));
        tableProviderSet.add(tableProvider);
        return tableProvider;
    }

    @Override
    public void close() {
        if (!isClosed) {
            for (TableProviderImpl tableProvider : tableProviderSet) {
                tableProvider.close();
            }
            isClosed = true;
        }
    }
}
