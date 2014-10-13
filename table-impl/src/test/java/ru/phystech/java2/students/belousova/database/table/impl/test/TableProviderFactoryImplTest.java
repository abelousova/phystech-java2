package ru.phystech.java2.students.belousova.database.table.impl.test;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import ru.phystech.java2.students.belousova.database.table.api.TableProviderFactory;
import ru.phystech.java2.students.belousova.database.table.impl.TableProviderFactoryImpl;

import java.io.File;

public class TableProviderFactoryImplTest {
    private TableProviderFactory tableProviderFactory = new TableProviderFactoryImpl();

    @After
    public void tearDown() throws Exception {
        File file = new File("javatest");
        if (file.exists()) {
            FileUtils.deleteDirectory(file);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNull() throws Exception {
        tableProviderFactory.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEmpty() throws Exception {
        tableProviderFactory.create("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNl() throws Exception {
        tableProviderFactory.create("    ");
    }

    @Test
    public void testCreateNotExisted() throws Exception {
        Assert.assertNotNull(tableProviderFactory.create("javatest"));
    }
}
