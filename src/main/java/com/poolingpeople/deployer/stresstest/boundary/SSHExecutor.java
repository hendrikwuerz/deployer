package com.poolingpeople.deployer.stresstest.boundary;


import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

import com.jcraft.jsch.*;


/**
 * Created by hendrik on 22.07.15.
 */
public class SSHExecutor {

    private String ip;
    private String user;

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
     * initialize connection
     */
    private void connect() throws JSchException, IOException {
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
        jsch.addIdentity(tmpFile.getAbsolutePath());

        // Create a new session for the command
        session = jsch.getSession(user, ip, 22);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

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
    public InputStream execute(String command) throws IOException, JSchException {

        connect();

        // execute command
        channel = (ChannelExec) session.openChannel("exec");
        InputStream serverInputStream = channel.getInputStream();
        channel.setCommand(command);
        channel.connect();

        // return stream
        return serverInputStream;


    }

    public File scp(String remoteFile) throws IOException, JSchException, SftpException {

        // connect to the current server
        connect();

        // use a sftp connection
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;

        // Create a tmp file local to store the remote file
        File localFile = File.createTempFile((new File(remoteFile)).getName(), "");

        System.out.println(localFile.getAbsolutePath());

        // copy remote file to local file
        sftpChannel.get(remoteFile, localFile.getAbsolutePath());

        // end all
        sftpChannel.exit();
        session.disconnect();
        tmpFile.delete();

        return localFile;
    }

    /**
     * cleans all opened channels for command execution
     */
    public void clean() {
        channel.disconnect();
        session.disconnect();
        tmpFile.delete();
    }

}
