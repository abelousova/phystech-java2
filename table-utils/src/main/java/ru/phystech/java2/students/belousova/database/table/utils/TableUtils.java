package ru.phystech.java2.students.belousova.database.table.utils;

import ru.phystech.java2.students.belousova.database.table.api.*;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableUtils {
    public static void readSignature(File file, List<Class<?>> columnTypes) throws IOException {
        if (!file.exists()) {
            throw new IOException("signature.tsv doesn't exist");
        }
        if (file.length() == 0) {
            throw new IOException("signature.tsv is empty");
        }

        try {
            InputStream is = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is, 4096);
            DataInputStream dis = new DataInputStream(bis);
            try {
                int position = 0;
                while (position != file.length()) {
                    String type = readType(dis);
                    position += type.getBytes(StandardCharsets.UTF_8).length + 1;
                    TypesEnum typesEnum = TypesEnum.getBySignature(type);
                    if (typesEnum == null) {
                        throw new IOException("read error");
                    }
                    Class<?> classType = typesEnum.getClazz();
                    columnTypes.add(classType);
                }
            } finally {
                FileMapUtils.closeStream(dis);
            }
        } catch (IOException e) {
            throw new IOException("read error", e);
        }
    }

    private static String readType(DataInputStream dis) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte b = dis.readByte();
        int length = 0;
        while (b != ' ') {
            bos.write(b);
            b = dis.readByte();
            length++;
            if (length > 1024 * 1024) {
                throw new IOException("signature.tsv has wrong format");
            }
        }
        if (length == 0) {
            throw new IOException("signature.tsv has wrong format");
        }
        return bos.toString(StandardCharsets.UTF_8.toString());
    }

    public static void readTable(File file, Table table, Map<String, TableRow> dataBase,
                                 TableProvider tableProvider) throws IOException {
        Map<String, String> stringMap = new HashMap<>();
        MultiFileUtils.read(file, stringMap);

        for (String key : stringMap.keySet()) {
            try {
                TableRow value = tableProvider.deserialize(table, stringMap.get(key));
                dataBase.put(key, value);
            } catch (ParseException e) {
                throw new IOException("read error", e);
            }
        }
    }

    public static void writeSignature(File directory, List<Class<?>> columnTypes) throws IOException {
        File signatureFile = new File(directory, "signature.tsv");
        signatureFile.createNewFile();
        OutputStream os = new FileOutputStream(signatureFile);
        BufferedOutputStream bos = new BufferedOutputStream(os, 4096);
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            for (Class<?> type : columnTypes) {
                if (type == null) {
                    throw new IllegalArgumentException("wrong column type");
                }
                TypesEnum typesEnum = TypesEnum.getByClass(type);
                if (typesEnum == null) {
                    throw new IOException("write error");
                }
                String typeString = TypesEnum.getByClass(type).getSignature();
                dos.write(typeString.getBytes(StandardCharsets.UTF_8));
                dos.write(' ');
            }
        } finally {
            FileMapUtils.closeStream(dos);
        }
    }

    public static TableRow readStorableValue(String s, Table table, TableProvider tableProvider) throws ParseException {
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        TableRow line = tableProvider.createFor(table);
        try {
            XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(s));
            try {
                if (!reader.hasNext()) {
                    throw new ParseException("input string is empty", 0);
                }
                reader.nextTag();
                if (!reader.getLocalName().equals("row")) {
                    throw new ParseException("invalid xml format", reader.getLocation().getCharacterOffset());
                }
                int columnIndex = 0;
                while (reader.hasNext()) {
                    reader.nextTag();
                    if (!reader.getLocalName().equals("col")) {
                        if (reader.getLocalName().equals("null")) {
                            reader.nextTag();
                            if (!reader.isEndElement()) {
                                throw new ParseException("invalid xml format",
                                        reader.getLocation().getCharacterOffset());
                            }
                            columnIndex++;

                        } else {
                            if (!reader.isEndElement()) {
                                throw new ParseException("invalid xml format",
                                        reader.getLocation().getCharacterOffset());
                            }
                            if (reader.isEndElement() && reader.getLocalName().equals("row")) {
                                break;
                            }
                        }
                    }
                    if (reader.isEndElement()) {
                        continue;
                    }
                    if (reader.next() == XMLStreamConstants.CHARACTERS) {
                        String text = reader.getText();
                        line.setColumnAt(columnIndex, parseValue(text, table.getColumnType(columnIndex)));
                    } else {
                        reader.nextTag();
                        if (!reader.getLocalName().equals("null")) {
                            throw new ParseException("invalid xml format",
                                    reader.getLocation().getCharacterOffset());
                        }
                        reader.nextTag();
                    }
                    columnIndex++;
                }
            } finally {
                reader.close();
            }
        } catch (XMLStreamException e) {
            throw new ParseException("xml reading error", 0);
        } catch (ColumnFormatException e) {
            throw new ParseException("xml reading error", 0);
        }
        return line;
    }

    private static Object parseValue(String s, Class<?> classType) {
        try {
            switch (classType.getName()) {
                case "java.lang.Integer":
                    return Integer.parseInt(s);
                case "java.lang.Long":
                    return Long.parseLong(s);
                case "java.lang.Byte":
                    return Byte.parseByte(s);
                case "java.lang.Float":
                    return Float.parseFloat(s);
                case "java.lang.Double":
                    return Double.parseDouble(s);
                case "java.lang.Boolean":
                    return Boolean.parseBoolean(s);
                case "java.lang.String":
                    return s;
                default:
                    throw new ColumnFormatException("wrong column format");
            }
        } catch (NumberFormatException e) {
            throw new ColumnFormatException("column format error");
        }
    }

    public static String writeStorableToString(TableRow tableRow, List<Class<?>> columnTypes) {
        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        StringWriter stringWriter = new StringWriter();
        try {
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stringWriter);
            try {
                writer.writeStartElement("row");
                for (int i = 0; i < columnTypes.size(); i++) {
                    if (tableRow.getColumnAt(i) == null) {
                        writer.writeStartElement("null");
                        writer.writeEndElement();
                    } else {
                        writer.writeStartElement("col");
                        writer.writeCharacters(getStringFromElement(tableRow, i, columnTypes.get(i)));
                        writer.writeEndElement();
                    }
                }
                writer.writeEndElement();
            } finally {
                writer.close();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        } finally {
            FileMapUtils.closeStream(stringWriter);
        }
        return stringWriter.toString();
    }

    private static String getStringFromElement(TableRow tableRow, int columnIndex, Class<?> columnType) {
        switch (columnType.getName()) {
            case "java.lang.Integer":
                return Integer.toString(tableRow.getIntAt(columnIndex));
            case "java.lang.Long":
                return Long.toString(tableRow.getLongAt(columnIndex));
            case "java.lang.Byte":
                return Byte.toString(tableRow.getByteAt(columnIndex));
            case "java.lang.Float":
                return Float.toString(tableRow.getFloatAt(columnIndex));
            case "java.lang.Double":
                return Double.toString(tableRow.getDoubleAt(columnIndex));
            case "java.lang.Boolean":
                return Boolean.toString(tableRow.getBooleanAt(columnIndex));
            case "java.lang.String":
                return tableRow.getStringAt(columnIndex);
            default:
                throw new ColumnFormatException("wrong column format");
        }
    }

    public static void writeTable(File file, Table table, Map<String, TableRow> storeableMap,
                                  TableProvider tableProvider) throws IOException {
        Map<String, String> stringMap = new HashMap<>();
        for (String key : storeableMap.keySet()) {
            stringMap.put(key, tableProvider.serialize(table, storeableMap.get(key)));
        }
        MultiFileUtils.write(file, stringMap);
    }

    private static Object getValueWithType(TableRow storeable, int columnIndex,
                                           Class<?> columnType) throws ColumnFormatException {
        switch (columnType.getName()) {
            case "java.lang.Integer":
                return storeable.getIntAt(columnIndex);
            case "java.lang.Long":
                return storeable.getLongAt(columnIndex);
            case "java.lang.Byte":
                return storeable.getByteAt(columnIndex);
            case "java.lang.Float":
                return storeable.getFloatAt(columnIndex);
            case "java.lang.Double":
                return storeable.getDoubleAt(columnIndex);
            case "java.lang.Boolean":
                return storeable.getBooleanAt(columnIndex);
            case "java.lang.String":
                return storeable.getStringAt(columnIndex);
            default:
                throw new ColumnFormatException("wrong column format");
        }
    }

    public static boolean isStorableValid(TableRow value, List<Class<?>> columnTypes) throws ColumnFormatException {
        int columnIndex = 0;
        try {
            for (Class<?> columnType : columnTypes) {
                getValueWithType(value, columnIndex, columnType);
                columnIndex++;
            }
            try {
                value.getColumnAt(columnIndex);
                return false;
            } catch (IndexOutOfBoundsException e) {
                return true;
            }
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }
}
