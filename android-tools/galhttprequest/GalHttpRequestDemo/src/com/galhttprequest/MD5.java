package com.galhttprequest;

/**   
 * @Title: MD5.java    
 * @Package com.galeapp.push.xmpp.util   
 * @Description: 为String生成MD5码
 * @author 林秋明   
 * @date 2012-2-14 上午11:51:48   
 * @version V1.0   
 */
import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5 {
	public static String encodeMD5String(String str) {
		return encode(str, "MD5");
	}

	private static String encode(String str, String method) {
		MessageDigest md = null;
		String dstr = null;
		try {
			md = MessageDigest.getInstance(method);
			md.update(str.getBytes());
			dstr = new BigInteger(1, md.digest()).toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dstr;
	}
}
