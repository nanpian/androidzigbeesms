package com.rtk.bdtest.util;

import java.io.UnsupportedEncodingException;

import java.security.SecureRandom;

import javax.crypto.Cipher;

import javax.crypto.SecretKey;

import javax.crypto.SecretKeyFactory;

import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DesCrypt

{

	public static void main(String[] args)

	throws UnsupportedEncodingException

	{

		// 待加密内容

		String str = "hellomys";

		// 密码，长度要是8的倍数

		String password = "hellomys";
		
		DesCrypt XX = new DesCrypt();

		byte[] result = XX.desCrypto(str.getBytes("UTF-8"), password.getBytes());

		System.out.println("加密后内容" + new String(result));

		// 直接将如上内容解密

		try

		{

			byte[] decryResult = XX.decrypt(result, password);

			System.out.println("加密前内容为：" + new String(decryResult));

		}

		catch (Exception e1)

		{

			e1.printStackTrace();

		}

	}
	
	/**
	 * 
	 * <对字符串进行Des加密，将字符串转化为字节数组解密>
	 */

	public byte[] desCrypto(byte[] datasource, byte[] password)

	{

		try

		{

			SecureRandom random = new SecureRandom();

			DESKeySpec desKey = new DESKeySpec(password);

			// 创建一个密匙工厂，然后用它把DESKeySpec转换成

			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

			SecretKey securekey = keyFactory.generateSecret(desKey);

			// Cipher对象实际完成加密操作

			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

			// 用密匙初始化Cipher对象

			cipher.init(Cipher.ENCRYPT_MODE, securekey);

			// 现在，获取数据并加密

			// 正式执行加密操作

			return cipher.doFinal(datasource);

		}

		catch (Throwable e)

		{

			e.printStackTrace();

		}

		return null;

	}

	/**
	 * 
	 * <对字符串进行Des加密，将字符串转化为字节数组解密>
	 */

	public static byte[] desCrypto(byte[] datasource, String password)

	{

		try

		{

			SecureRandom random = new SecureRandom();

			DESKeySpec desKey = new DESKeySpec(password.getBytes());

			// 创建一个密匙工厂，然后用它把DESKeySpec转换成

			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

			SecretKey securekey = keyFactory.generateSecret(desKey);

			// Cipher对象实际完成加密操作

			Cipher cipher = Cipher.getInstance("DES");

			// 用密匙初始化Cipher对象

			//cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
			//按照http://blog.sina.com.cn/s/blog_6d51d25e0100lp28.html
			cipher.init(Cipher.ENCRYPT_MODE, securekey);
			// 现在，获取数据并加密

			// 正式执行加密操作

			return cipher.doFinal(datasource);

		}

		catch (Throwable e)

		{

			e.printStackTrace();

		}

		return null;

	}

	/**
	 * 
	 * <将加密的密文字节数组转化为明文字节数组>
	 */

	public static byte[] decrypt(byte[] src, String password)

	throws Exception

	{

		// DES算法要求有一个可信任的随机数源

		SecureRandom random = new SecureRandom();

		// 创建一个DESKeySpec对象

		DESKeySpec desKey = new DESKeySpec(password.getBytes("UTF-8"));

		// 创建一个密匙工厂

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

		// 将DESKeySpec对象转换成SecretKey对象

		SecretKey securekey = keyFactory.generateSecret(desKey);

		// Cipher对象实际完成解密操作

		Cipher cipher = Cipher.getInstance("DES");

		// 用密匙初始化Cipher对象

	//	cipher.init(Cipher.DECRYPT_MODE, securekey, random);
		byte[] iv = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00}; 
		cipher.init(Cipher.DECRYPT_MODE, securekey);
		// 真正开始解密操作

		return cipher.doFinal(src);

	}
}
