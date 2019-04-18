package com.yonyou.ftp;

/**
 * FTP属性相关的配置
 * @author changming
 * @version v.0.1
 */
public class FTPConfig{
	
    private String host;
    private int port;
    private String username;
    private String password;
    private boolean passiveMode = true;
    private String encoding = "UTF-8";
    private int clientTimeout = 60000;
    private int threadNum = 10;
    private int transferFileType = 2;
    private boolean renameUploaded = false;
    private int retryTimes = 1200;
    private int bufferSize = 1024;
    private String workingDirectory = "/";
    
    public String getWorkingDirectory() {
        return workingDirectory;
    }
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
    public int getBufferSize() {
        return bufferSize;
    }
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean getPassiveMode() {
        return passiveMode;
    }
    public void setPassiveMode(boolean passiveMode) {
        this.passiveMode = passiveMode;
    }
    public String getEncoding() {
        return encoding;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public int getClientTimeout() {
        return clientTimeout;
    }
    public void setClientTimeout(int clientTimeout) {
        this.clientTimeout = clientTimeout;
    }
    public int getThreadNum() {
        return threadNum;
    }
    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }
    public int getTransferFileType() {
        return transferFileType;
    }
    public void setTransferFileType(int transferFileType) {
        this.transferFileType = transferFileType;
    }
    public boolean isRenameUploaded() {
        return renameUploaded;
    }
    public void setRenameUploaded(boolean renameUploaded) {
        this.renameUploaded = renameUploaded;
    }
    public int getRetryTimes() {
        return retryTimes;
    }
    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

}
