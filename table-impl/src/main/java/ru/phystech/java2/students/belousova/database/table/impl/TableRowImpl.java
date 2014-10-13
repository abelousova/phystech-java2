package ru.phystech.java2.students.belousova.database.table.impl;

import ru.phystech.java2.students.belousova.database.table.api.ColumnFormatException;
import ru.phystech.java2.students.belousova.database.table.api.Table;
import ru.phystech.java2.students.belousova.database.table.api.TableRow;

import java.util.ArrayList;
import java.util.List;

public class TableRowImpl implements TableRow {
    private List<Object> columns = new ArrayList<>();
    private Table table;

    public TableRowImpl(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("table cannot be null");
        }
        this.table = table;
        for (int i = 0; i < table.getColumnsCount(); i++) {
            columns.add(null);
        }
    }

    private void checkColumnIndexBounds(int columnIndex) {
        if (columnIndex < 0 || columnIndex > columns.size()) {
            throw new IndexOutOfBoundsException("index is out of bounds");
        }
    }

    private void checkColumnFormat(int columnIndex, Class<?> valueClass) {
        if (!valueClass.equals(table.getColumnType(columnIndex))) {
            throw new ColumnFormatException("wrong column format");
        }
    }

    @Override
    public void setColumnAt(int columnIndex, Object value) throws ColumnFormatException, IndexOutOfBoundsException {
        checkColumnIndexBounds(columnIndex);
        if (value != null) {
            checkColumnFormat(columnIndex, value.getClass());
        }
        columns.set(columnIndex, value);
    }

    @Override
    public Object getColumnAt(int columnIndex) throws IndexOutOfBoundsException {
        checkColumnIndexBounds(columnIndex);
        return columns.get(columnIndex);
    }

    @Override
    public Integer getIntAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkColumnIndexBounds(columnIndex);
        checkColumnFormat(columnIndex, Integer.class);
        return (Integer) columns.get(columnIndex);
    }

    @Override
    public Long getLongAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkColumnIndexBounds(columnIndex);
        checkColumnFormat(columnIndex, Long.class);
        return (Long) columns.get(columnIndex);
    }

    @Override
    public Byte getByteAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkColumnIndexBounds(columnIndex);
        checkColumnFormat(columnIndex, Byte.class);
        return (Byte) columns.get(columnIndex);
    }

    @Override
    public Float getFloatAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkColumnIndexBounds(columnIndex);
        checkColumnFormat(columnIndex, Float.class);
        return (Float) columns.get(columnIndex);
    }

    @Override
    public Double getDoubleAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkColumnIndexBounds(columnIndex);
        checkColumnFormat(columnIndex, Double.class);
        return (Double) columns.get(columnIndex);
    }

    @Override
    public Boolean getBooleanAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkColumnIndexBounds(columnIndex);
        checkColumnFormat(columnIndex, Boolean.class);
        return (Boolean) columns.get(columnIndex);
    }

    @Override
    public String getStringAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        checkColumnIndexBounds(columnIndex);
        checkColumnFormat(columnIndex, String.class);
        return (String) columns.get(columnIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().getName().equals(this.getClass().getName())) {
            return false;
        }
        TableRowImpl line = (TableRowImpl) obj;
        for (int i = 0; i < line.table.getColumnsCount(); i++) {
            if (!line.table.getColumnType(i).equals(table.getColumnType(i))) {
                return false;
            }
        }
        return line.columns.equals(columns);
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        return table.getColumnType(columnIndex);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getClass().getSimpleName());
        stringBuilder.append("[");
        boolean first = true;
        for (Object value : columns) {
            if (!first) {
                stringBuilder.append(",");
            }
            first = false;
            if (value != null) {
                stringBuilder.append(value.toString());
            }
        }
        stringBuilder.append("]");

        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
