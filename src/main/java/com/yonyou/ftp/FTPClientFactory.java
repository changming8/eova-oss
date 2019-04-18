package com.yonyou.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FTP工厂  windows下ftp共享目录为d:/source,ftp服务路径的根为"/"
 * @author changming
 * @version v.0.1
 */
public class FTPClientFactory extends BasePooledObjectFactory<FTPClient> {
    private static Logger logger = LoggerFactory.getLogger(FTPClientFactory.class);
    
    private FTPConfig ftpConfig;
    
    private FTPClient client;
    
    /**
     * 
     * @param host 主机
     * @param port 端口
     * @param username 用户名
     * @param password 密码
     */
    public FTPClientFactory(String host, int port, String username, String password) throws Exception {

    	ftpConfig = new FTPConfig();
    	ftpConfig .setHost(host);
    	ftpConfig.setPort(port);
    	ftpConfig.setUsername(username);
    	ftpConfig.setPassword(password);
    	try {
			client = create();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * 新建对象
     */
    @Override
    public FTPClient create() throws Exception {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(ftpConfig.getClientTimeout());
        try {
            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                logger.error("FTPServer 拒绝连接");
                return null;
            }
            boolean result = ftpClient.login(ftpConfig.getUsername(),ftpConfig.getPassword());
            if (!result) {
                logger.error("ftpClient登陆失败!");
                throw new Exception("ftpClient登陆失败! userName:"+ ftpConfig.getUsername() + " ; password:"
                        + ftpConfig.getPassword());
            }
            ftpClient.setFileType(ftpConfig.getTransferFileType());
            ftpClient.setBufferSize(ftpConfig.getBufferSize());
            ftpClient.setControlEncoding(ftpConfig.getEncoding());
            if (ftpConfig.getPassiveMode()) {
                ftpClient.enterLocalPassiveMode();
            }
            ftpClient.changeWorkingDirectory(ftpConfig.getWorkingDirectory());
        } catch (IOException e) {
            logger.error("FTP连接失败：", e);
        }
        return ftpClient;
    }

    @Override
    public PooledObject<FTPClient> wrap(FTPClient ftpClient) {
        return new DefaultPooledObject<FTPClient>(ftpClient);
    }

    /**
         * 销毁对象
     */
    @Override
    public void destroyObject(PooledObject<FTPClient> p) throws Exception {
        FTPClient ftpClient = p.getObject();
        ftpClient.logout();
        super.destroyObject(p);
    }

    /**
         * 验证对象
     */
    @Override
    public boolean validateObject(PooledObject<FTPClient> p) {
        FTPClient ftpClient = p.getObject();
        boolean connect = false;
        try {
            connect = ftpClient.sendNoOp();
            if(connect){                
                ftpClient.changeWorkingDirectory(ftpConfig.getWorkingDirectory());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connect;
    }
    
    /**
     * 上传文件 
     * @param path ftp服务端路径 必须以"/"结尾
     * @param sendfile 本地文件路径（包含文件名）
     * @param fileName 上传后文件名
     */
    public boolean sendFile( String path, String sendfile, String fileName ){
    	
        long start = System.currentTimeMillis();
        InputStream inputStream = null;
        boolean flag = false;
        try {
            File file = new File(sendfile);
            if(!file.exists()) {
            	logger.error("***************************文件不存在**************************");
            	return false;
            }
            if (!client.changeWorkingDirectory(path)) {
            	logger.error("***************************ftp文件路径不存在**************************");
            	return false;
            }
            inputStream = new FileInputStream(file);
            logger.debug("***************************"+new Date()+"  开始上传");
            flag = client.storeFile(path +fileName , inputStream);
            flag = true;
            logger.debug("***************************上传"+flag+" 耗时："+(System.currentTimeMillis()-start));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("***************************io异常***************************");
        }finally{
            if(client != null)  try {client.disconnect();} catch (IOException ioe) {}
        }
        return flag;
    }
    
    /**
	  * 下载文件
	 * @param txtFileDir 源文件全路径（包含文件名）
	 * @param fileName   文件名
	 * @param txtTargetDir 目标路径以"/"结尾
	 */
	public int downLoadFile( String txtFileDir, String fileName, String txtTargetDir ) {
		
	
		logger.debug("***************************"+new Date()+"  开始开始");
		int returnValue = 0;
		long start = System.currentTimeMillis();
		OutputStream ios = null;
           try {
               client.setFileType(FTPClient.BINARY_FILE_TYPE);
               //设置操作系统环境
               FTPClientConfig conf = new FTPClientConfig( OsUtil.getOSname());
               client.configure(conf);
               //判断是否连接成功
               int reply = client.getReplyCode();
               if (!FTPReply.isPositiveCompletion(reply)) {
            	   client.disconnect();
                   logger.error("FTP server refused connection.");
                   return returnValue;
               }
               //设置访问被动模式
               client.setRemoteVerificationEnabled(false);
               client.enterLocalPassiveMode();
               
//               修正文件路径
               boolean dir = client.changeWorkingDirectory(txtFileDir);
               if (dir) {
            	   
            	   File localFile = new File( txtTargetDir+fileName ); 
            	   ios = new FileOutputStream( localFile );
            	   FTPFile f = client.mlistFile(txtFileDir+fileName );
            	   boolean flag = client.retrieveFile(f.getName(), ios);
            	   logger.debug("***************************下载"+flag+" 耗时："+(System.currentTimeMillis()-start));
            	   returnValue = 1;
               }else {
            	   logger.error("ftp服务路径不存在.");
            	   return returnValue;
               }
           } 
           catch (Exception e) {
               e.printStackTrace();
               logger.error(new Date()+"  ftp下载文件发生错误");
           }
           finally {
               if(client != null)  try {client.disconnect();} catch (IOException ioe) {}  
               try {
            	   ios.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
           }
		return returnValue;
	}
	
	public static void main(String[] args) {
		try {
//			new FTPClientFactory("127.0.0.1", 21, "test", "test").downLoadFile("/main/", "aaaaaa.txt", "d:/target/");
			new FTPClientFactory("127.0.0.1", 21, "test", "test").sendFile("/","d:/target/aaaaaa.txt", "aaaaaa.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
