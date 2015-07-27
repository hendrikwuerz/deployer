package com.poolingpeople.deployer.stresstest.boundary;


import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

import com.jcraft.jsch.*;


/**
 * Created by hendrik on 22.07.15.
 */
public class SSHExecutor {

    private String ip = "52.18.199.184";
    private String user = "hendrik";

    private File tmpFile;
    private ChannelExec channel;
    private Session session;


    public SSHExecutor(String ip, String user) {
        this.ip = ip;
        this.user = user;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * executes the passed command on the server with the current IP.
     * Will connect with the current user and the private key in resources "jmeter-private-key"
     * Do not forget to call clean() after executing a command
     * @param command
     *          The command to be executed
     * @return
     *          The input stream from the server or null when no connection was possible
     */
    public InputStream execute(String command) {

        try {
            System.out.println("------------------------- Starting command execution");
            JSch jsch = new JSch();

            // Create a tmp file for private key
            tmpFile = File.createTempFile("key", ".key");
            OutputStream outputStream = new FileOutputStream(tmpFile);

            // Copy private key from resources to tmp file
            int read = 0;
            byte[] bytes = new byte[1024];
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("jmeter-private-key");
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.close();

            // add private key to ssh
            System.out.println(tmpFile.getAbsolutePath());
            jsch.addIdentity(tmpFile.getAbsolutePath());

            Files.lines(tmpFile.toPath()).forEach(System.out::println);

            // Create a new session for the command
            session = jsch.getSession(user, ip, 22);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            // execute command
            channel = (ChannelExec) session.openChannel("exec");
            InputStream serverInputStream = channel.getInputStream();
            channel.setCommand(command);
            channel.connect();

            // return stream
            return serverInputStream;

        } catch (JSchException | IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * cleans all opened channels for command execution
     */
    public void clean() {
        channel.disconnect();
        session.disconnect();
        tmpFile.delete();
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /*
    public void runTest() {
        try {
            System.out.println("------------------------- runTest");
            JSch jsch = new JSch();

            File tempFile = File.createTempFile("key", ".key");
            OutputStream outputStream = new FileOutputStream(tempFile);

            int read = 0;
            byte[] bytes = new byte[1024];
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("jmeter-private-key");
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.close();

            System.out.println(tempFile.getAbsolutePath());
            jsch.addIdentity(tempFile.getAbsolutePath());

            Files.lines(tempFile.toPath()).forEach(System.out::println);

            Session session = jsch.getSession(user, ip, 22);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            //channel.setCommand("sudo pwd;");
            channel.setCommand("cd /home/hendrik/docker-jmeter/hendrik/jmeter-master/; echo " + pass + " | sudo -S /home/hendrik/docker-jmeter/hendrik/jmeter-master/example_run_test.sh;");
            channel.connect();

            String msg = null;
            while ((msg = in.readLine()) != null) {
                System.out.println(msg);
            }

            channel.disconnect();
            session.disconnect();

        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
    }
    */
}
