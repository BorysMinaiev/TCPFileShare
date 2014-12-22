import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Utils {
	public static byte[] writeByte(int x) {
		return new byte[] { (byte) x };
	}

	public static byte[] writeInt(int x) {
		byte[] res = new byte[4];
		for (int i = 0; i < 4; i++) {
			res[3 - i] = (byte) (x & ((1 << 8) - 1));
			x >>= 8;
		}
		return res;
	}

	public static byte[] writeLong(long x) {
		byte[] res = new byte[8];
		for (int i = 0; i < 8; i++) {
			res[7 - i] = (byte) (x & ((1 << 8) - 1));
			x >>= 8;
		}
		return res;
	}

	public static byte[] writeString(String s) {
		byte[] res = new byte[s.length() + 1];
		for (int i = 0; i + 1 < res.length; i++) {
			res[i] = (byte) s.charAt(i);
		}
		return res;
	}

	public static String readString(InputStream is) throws IOException {
		ArrayList<Character> ans = new ArrayList<>();
		while (true) {
			int c = is.read();
			if (c == -1 || c == 0) {
				break;
			}
			ans.add((char) c);
		}
		char[] res = new char[ans.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = ans.get(i);
		}
		return new String(res);
	}

	public static long readLong(InputStream is) throws IOException {
		long result = 0;
		for (int i = 0; i < 8; i++) {
			int x = is.read();
			if (x == -1) {
				throw new IOException("unexpected end of file");
			}
			result = (result << 8) | x;
		}
		return result;
	}

	public static int readInt(InputStream is) throws IOException {
		int result = 0;
		for (int i = 0; i < 4; i++) {
			int x = is.read();
			if (x == -1) {
				throw new IOException("unexpected end of file");
			}
			result = (result << 8) | x;
		}
		return result;
	}

	public static byte[] readMD5(InputStream is) throws IOException {
		byte[] result = new byte[16];
		for (int i = 0; i < result.length; i++) {
			int x = is.read();
			if (x == -1) {
				throw new IOException("unexpected end of file");
			}
			result[i] = (byte) x;
		}
		return result;
	}
}
