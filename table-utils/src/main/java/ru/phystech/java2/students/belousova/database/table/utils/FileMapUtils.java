package ru.phystech.java2.students.belousova.database.table.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileMapUtils {
    public static void read(File file, Map<String, String> map, Predicate<String> validator) throws IOException {
        if (file.length() == 0) {
            return;
        }
        try {
            InputStream is = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is, 4096);
            DataInputStream dis = new DataInputStream(bis);

            int fileLength = (int) file.length();

            try {
                int position = 0;
                String key1 = readKey(dis, validator);
                position += key1.getBytes(StandardCharsets.UTF_8).length;
                int offset1 = dis.readInt();
                int firstOffset = offset1;
                position += 5;

                while (position != firstOffset) {
                    String key2 = readKey(dis, validator);
                    position += key2.getBytes(StandardCharsets.UTF_8).length;
                    int offset2 = dis.readInt();
                    position += 5;
                    String value = readValue(dis, offset1, offset2, position, fileLength);
                    map.put(key1, value);
                    offset1 = offset2;
                    key1 = key2;
                }
                String value = readValue(dis, offset1, fileLength, position, fileLength);
                map.put(key1, value);
            } finally {
                closeStream(dis);
            }

        } catch (IOException e) {
            throw new IOException("cannot read '" + file.getName() + "'", e);
        }
    }

    private static String readKey(DataInputStream dis, Predicate<String> predicate) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte b = dis.readByte();
        int length = 0;
        while (b != 0) {
            bos.write(b);
            b = dis.readByte();
            length++;
            if (length > 1024 * 1024) {
                throw new IOException("wrong data format");
            }
        }
        if (length == 0) {
            throw new IOException("wrong data format");
        }

        String key = bos.toString(StandardCharsets.UTF_8.toString());
        if (!predicate.apply(key)) {
            throw new IOException("wrong data format");
        }

        return key;
    }

    private static String readValue(DataInputStream dis, int offset1,
                                    int offset2, int position, int length) throws IOException {
        dis.mark(length);
        dis.skip(offset1 - position);
        byte[] buffer = new byte[offset2 - offset1];
        dis.read(buffer);
        String value = new String(buffer, StandardCharsets.UTF_8);
        dis.reset();
        return value;
    }

    public static void write(File file, Map<String, String> map) throws IOException {
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os, 4096);
            DataOutputStream dos = new DataOutputStream(bos);

            try {
                long offset = 0;
                for (String key : map.keySet()) {
                    offset += key.getBytes(StandardCharsets.UTF_8).length + 5;
                }

                List<String> values = new ArrayList<String>(map.keySet().size());
                for (String key : map.keySet()) {
                    String value = map.get(key);
                    values.add(value);
                    dos.write(key.getBytes(StandardCharsets.UTF_8));
                    dos.write('\0');
                    dos.writeInt((int) offset);
                    offset += value.getBytes(StandardCharsets.UTF_8).length;
                }

                for (String value : values) {
                    dos.write(value.getBytes());
                }
            } finally {
                closeStream(dos);
            }

        } catch (IOException e) {
            throw new IOException("cannot write '" + file.getName() + "'", e);
        }
    }

    public static void closeStream(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            //do nothing
        }
    }
}
