package com.yonyou.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class FileUtil {

	/**
     * 指定文件去掉bom头
     * @param fileName
     * @throws java.io.IOException
     */
    public static void trimBom(String fileName) throws IOException {
 
        FileInputStream fin = new FileInputStream(fileName);
		// 开始写临时文件
        InputStream in = getInputStream(fin);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte b[] = new byte[4096];
 
        int len = 0;
        while (in.available() > 0) {
            len = in.read(b, 0, 4096);
            bos.write(b, 0, len);
        }
 
        in.close();
        fin.close();
        bos.close();
 
		//临时文件写完，开始将临时文件写回本文件。
        FileOutputStream out = new FileOutputStream(fileName);
        out.write(bos.toByteArray());
        out.close();
    }
    
    /**
     * 读取流中前面的字符，看是否有bom，如果有bom，将bom头丢弃
     *
     * @param in
     * @return
     * @throws java.io.IOException
     */
    private static InputStream getInputStream(InputStream in) throws IOException {
 
        PushbackInputStream testin = new PushbackInputStream(in);
        int ch = testin.read();
        if (ch != 0xEF) {
            testin.unread(ch);
        } else if ((ch = testin.read()) != 0xBB) {
            testin.unread(ch);
            testin.unread(0xef);
        } else if ((ch = testin.read()) != 0xBF) {
            throw new IOException("错误的UTF-8格式文件");
        } else {
		// 不需要做，这里是bom头被读完了
		// System.out.println("still exist bom");
        }
        return testin;
 
    }
    
    /**
	 * 文本文件中修正换行符
	 * @param path
	 * @throws IOException
	 */
	public static void fixSymbol(String path) throws IOException{
		
		//原有的内容
		String windows = "\r\n";
		String moc = "\r";
		//要替换的内容
		String replaceStr = "\n";     
		// 读  
		File file = new File(path);   
		FileReader in = new FileReader(file);  
		BufferedReader bufIn = new BufferedReader(in);  
		// 内存流, 作为临时流  
		CharArrayWriter  tempStream = new CharArrayWriter();  
		// 替换  
		String line = null;
		int i = 0;
		while ( (line = bufIn.readLine()) != null) {  
		    // 替换每行中, 符合条件的字符串  
		    line = line.replaceAll(windows, replaceStr).replaceAll(moc, replaceStr);  
		    // 将该行写入内存  
		    tempStream.write(line);  
		    i++;
		}  
		// 关闭 输入流  
		bufIn.close();  
		// 将内存中的流 写入 文件  
		FileWriter out = new FileWriter(file);  
		tempStream.writeTo(out);  
		out.close();  
		System.out.println("====path:"+path+"替换"+i+"次换行符");
		
	}

	
    
    public static void main(String[] args) {
    	String path = "D:/source/M003-COSTTYPEANDCOORAUTH-FMP-20190415-001-I.TXT";
    	try {
//			trimBom(path);
//			System.out.println(System.getProperty("line.separator").equals("\r\n"));
    		fixSymbol(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
}
