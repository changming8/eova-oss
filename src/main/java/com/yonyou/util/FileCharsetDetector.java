package com.yonyou.util;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;


public class FileCharsetDetector {
    private boolean found = false;
    private String encoding = null;

    public static void main(String[] argv) throws Exception {
        File file1 = new File("C:\\test1.txt");
        
        System.out.println("文件编码:" + new FileCharsetDetector().guessFileEncoding(file1));
        convert("D:\\stuff\\src\\main\\java\\com\\mikan\\stuff\\test.txt",
				"GBK", "UTF-8", new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith("txt");
					}
				});
    }
    
    /**
     * 
     * @param fileName
     * @param toCharset
     * @param expendName
     * @throws FileNotFoundException
     * @throws IOException
     * @throws Exception
     */
    public void convert( String fileName, String toCharset, final String expendName ) throws FileNotFoundException,IOException,Exception{
    	
    	File fil = new File(fileName);
    	String fromCharset = guessFileEncoding(fil, 2);
    	if (!"UTF-8".equals(fromCharset))
			convert( fil, fromCharset, toCharset, new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(expendName);
				}
			});
    }
 
   /**
	* 
    * @param file
    * @param toCharset
    * @param expendName
    * @throws FileNotFoundException
    * @throws IOException
    * @throws Exception
    */
   public void convertByFile( File fil, String toCharset, final String expendName ) throws FileNotFoundException,IOException,Exception{
   	
	   String fromCharset = guessFileEncoding(fil, 2);
	   if (!"UTF-8".equals(fromCharset))
			convert( fil, fromCharset, toCharset, new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(expendName);
				}
			});
   }
    
	/**
	 * 把指定文件或目录转换成指定的编码
	 * @param file 要转换的文件或目录
	 * @param fromCharsetName 源文件的编码
	 * @param toCharsetName 要转换的编码
	 * @throws Exception
	 */
	public static void convert(File file, String fromCharsetName,
			String toCharsetName) throws Exception {
		convert(file, fromCharsetName, toCharsetName, null);
	}
 
	/**
	 * 把指定文件或目录转换成指定的编码
	 * @param file 要转换的文件或目录
	 * @param fromCharsetName 源文件的编码
	 * @param toCharsetName 要转换的编码
	 * @param filter 文件名过滤器
	 * @throws Exception
	 */
	public static void convert(String fileName, String fromCharsetName,
			String toCharsetName, FilenameFilter filter) throws Exception {
		convert(new File(fileName), fromCharsetName, toCharsetName, filter);
	}
 
	/**
	 * 把指定文件或目录转换成指定的编码
	 * @param file 要转换的文件或目录
	 * @param fromCharsetName 源文件的编码
	 * @param toCharsetName 要转换的编码
	 * @param filter 文件名过滤器
	 * @throws Exception
	 */
	public static void convert(File file, String fromCharsetName,
			String toCharsetName, FilenameFilter filter) throws Exception {
		if (file.isDirectory()) {
			File[] fileList = null;
			if (filter == null) {
				fileList = file.listFiles();
			} else {
				fileList = file.listFiles(filter);
			}
			for (File f : fileList) {
				convert(f, fromCharsetName, toCharsetName, filter);
			}
		} else {
			if (filter == null
					|| filter.accept(file.getParentFile(), file.getName())) {
				String fileContent = getFileContentFromCharset(file,
						fromCharsetName);
				saveFile2Charset(file, toCharsetName, fileContent);
			}
		}
	}
 
	/**
	 * 以指定编码方式读取文件，返回文件内容
	 * @param file 要转换的文件
	 * @param fromCharsetName 源文件的编码
	 * @return 文件内容
	 * @throws Exception
	 */
	private static String getFileContentFromCharset(File file,
			String fromCharsetName) throws Exception {
		if (!Charset.isSupported(fromCharsetName)) {
			throw new UnsupportedCharsetException(fromCharsetName);
		}
		InputStream inputStream = new FileInputStream(file);
		InputStreamReader reader = new InputStreamReader(inputStream,
				fromCharsetName);
		char[] chs = new char[(int) file.length()];
		reader.read(chs);
		String str = new String(chs).trim();
		reader.close();
		return str;
	}
 
	/**
	 * 以指定编码方式写文本文件，覆盖原文件
	 * @param file 要写入的文件
	 * @param toCharsetName 要转换的编码
	 * @param content 文件内容
	 * @throws Exception
	 */
	private static void saveFile2Charset(File file, String toCharsetName,
			String content) throws Exception {
		if (!Charset.isSupported(toCharsetName)) {
			throw new UnsupportedCharsetException(toCharsetName);
		}
		OutputStream outputStream = new FileOutputStream(file);
		OutputStreamWriter outWrite = new OutputStreamWriter(outputStream,
				toCharsetName);
		outWrite.write(content);
		outWrite.close();
	}

    /**
     * 传入一个文件(File)对象，检查文件编码
     * @param file
     *            File对象实例
     * @return 文件编码，若无，则返回null
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String guessFileEncoding(File file) throws FileNotFoundException, IOException {
        return guessFileEncoding(file, new nsDetector());
    }

    /**
     * 获取文件的编码
     * @param file
     *            File对象实例
     * @param languageHint
     *            语言提示区域代码 @see #nsPSMDetector ,取值如下：
     *             1 : Japanese
     *             2 : Chinese
     *             3 : Simplified Chinese
     *             4 : Traditional Chinese
     *             5 : Korean
     *             6 : Dont know(default)
     * 
     * @return 文件编码，eg：UTF-8,GBK,GB2312形式(不确定的时候，返回可能的字符编码序列)；若无，则返回null
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String guessFileEncoding(File file, int languageHint) throws FileNotFoundException, IOException {
        return guessFileEncoding(file, new nsDetector(languageHint));
    }

    /**
     * 获取文件的编码
     * 
     * @param file
     * @param det
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String guessFileEncoding(File file, nsDetector det) throws FileNotFoundException, IOException {
        // Set an observer...
        // The Notify() will be called when a matching charset is found.
        det.Init(new nsICharsetDetectionObserver() {
            public void Notify(String charset) {
                encoding = charset;
                found = true;
            }
        });

        BufferedInputStream imp = new BufferedInputStream(new FileInputStream(file));
        byte[] buf = new byte[1024];
        int len;
        boolean done = false;
        boolean isAscii = false;

        while ((len = imp.read(buf, 0, buf.length)) != -1) {
            // Check if the stream is only ascii.
            isAscii = det.isAscii(buf, len);
            if (isAscii) {
                break;
            }
            // DoIt if non-ascii and not done yet.
            done = det.DoIt(buf, len, false);
            if (done) {
                break;
            }
        }
        imp.close();
        det.DataEnd();

        if (isAscii) {
            encoding = "ASCII";
            found = true;
        }

        if (!found) {
            String[] prob = det.getProbableCharsets();
            //这里将可能的字符集组合起来返回
            for (int i = 0; i < prob.length; i++) {
                if (i == 0) {
                    encoding = prob[i];
                } else {
                    encoding += "," + prob[i];
                }
            }

            if (prob.length > 0) {
                // 在没有发现情况下,也可以只取第一个可能的编码,这里返回的是一个可能的序列
                return encoding;
            } else {
                return null;
            }
        }
        return encoding;
    }
}