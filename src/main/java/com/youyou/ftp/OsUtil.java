package com.youyou.ftp;

import org.apache.commons.net.ftp.FTPClientConfig;

/**
 * 
 * @author changming
 * @version v.0.1
 */
public class OsUtil {

	private static final String OS = System.getProperty("os.name").toLowerCase();  

    public static boolean isLinux(){  
        return OS.indexOf("linux")>=0;  
    }  

    public static boolean isMacOS(){  
        return OS.indexOf("mac")>=0&&OS.indexOf("os")>0&&OS.indexOf("x")<0;  
    }  

    public static boolean isWindows(){  
        return OS.indexOf("windows")>=0;  
    }  

    public static boolean isOS2(){  
        return OS.indexOf("os/2")>=0;  
    }  

    public static boolean isOpenVMS(){  
        return OS.indexOf("openvms")>=0;  
    }  

    /** 
     * 获取操作系统名字 
     * @return 操作系统名 
     */  
    public static String getOSname(){  
        
    	if (isLinux()) {  
            return FTPClientConfig.SYST_UNIX;  
        }else if (isMacOS()) {  
            return FTPClientConfig.SYST_MACOS_PETER;  
        }else if (isOpenVMS()) {  
            return FTPClientConfig.SYST_VMS;
        }else if (isOS2()) {  
        	return FTPClientConfig.SYST_OS2;  
        }else {  
        	return FTPClientConfig.SYST_NT;  
        }
    }  
    /** 
     * @param args 
     */  
    public static void main(String[] args) {  
        System.out.println(OsUtil.getOSname());  
    } 
}
