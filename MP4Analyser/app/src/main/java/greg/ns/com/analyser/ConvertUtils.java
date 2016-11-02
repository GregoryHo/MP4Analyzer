package greg.ns.com.analyser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Gregory on 2016/9/29.
 */
public class ConvertUtils {

	protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();

	/**
	 * Convert input stream to byte array
	 *
	 * @param in
	 * @return
	 */
	public static byte[] inputStream2Bytes(InputStream in) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[4 * 1024];

		int read;
		try {
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			out.flush();
			out.close();

			return out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Gets 1 byte value. (HEX)
	 *
	 * @param aByte
	 * @return
	 */
	public static String byte2HEX(byte aByte) {
		char[] chars = new char[2];
		chars[0] = hexArray[(aByte & 0xF0) >> 4];
		chars[1] = hexArray[(aByte & 0x0F)];
		return new String(chars);
	}

	/**
	 * Gets 4 bytes value. (DEC)
	 *
	 * @param bytes  data
	 * @param offset start index
	 * @return
	 */
	public static int bytes2Int(byte[] bytes, int offset) {
		if (offset + 4 > bytes.length) {
			return -1;
		}

		int size = 0;
		size += (bytes[offset] & 0xFF) << 24;
		size += (bytes[offset + 1] & 0xFF) << 16;
		size += (bytes[offset + 2] & 0xFF) << 8;
		size += bytes[offset + 3] & 0xFF;

		return size;
	}

	/**
	 * Gets ASCII characters from 4 bytes data
	 *
	 * @param bytes
	 * @param offset
	 * @return
	 */
	public static String bytes2ASCII(byte[] bytes, int offset) {
		if (offset + 4 > bytes.length) {
			return "error";
		}

		char c1 = (char) (bytes[offset] & 0xFF);
		char c2 = (char) (bytes[offset + 1] & 0xFF);
		char c3 = (char) (bytes[offset + 2] & 0xFF);
		char c4 = (char) (bytes[offset + 3] & 0xFF);

		return "" + c1 + c2 + c3 + c4;
	}

	public static byte[] hex2Byte(String hexString) {
		byte[] bytes = new byte[hexString.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);

		}

		return bytes;
	}
}
