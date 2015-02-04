package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class ApplicationDockerPackageTest {

    ApplicationDockerPackage cut;

    @Before
    public void setUp(){
        cut = new ApplicationDockerPackage();
        cut.versionsApi = new VersionsApi();
        cut.version = "0.0.1";
    }

    @Test
    public void testPrepareTarStream() throws Exception {
        cut.prepareTarStream();
        cut.materializeTarFile("/home/alacambra/test.tar.gz");
    }

    @Test
    public void testStreamToFile() throws Exception {

        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("Dockerfile-wf");
        InputStreamReader streamReader = new InputStreamReader(stream, Charset.forName("UTF-8"));
        String targetString = readerToString(streamReader);
//        ByteArrayOutputStream tarByteStream = new ByteArrayOutputStream();
        FileOutputStream tarFos = new FileOutputStream(new File("/home/alacambra/test.tar"));

        try {

            File f = new File("test.txt");
            FileOutputStream fos = null;

            try {
                fos = new FileOutputStream(f);
                fos.write(targetString.getBytes());
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            TarArchiveOutputStream taos = new TarArchiveOutputStream(tarFos, "UTF-8");
            TarArchiveEntry entry = new TarArchiveEntry(f, "UTF-8");
            taos.putArchiveEntry(entry);
            FileUtils.copyFile(f, taos);
            taos.closeArchiveEntry();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String readerToString(Reader initialReader){

        char[] arr = new char[8 * 1024];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;

        try {
            while ((numCharsRead = initialReader.read(arr, 0, arr.length)) != -1) {
                buffer.append(arr, 0, numCharsRead);
            }

            initialReader.close();
            String targetString = buffer.toString();
            return targetString;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}