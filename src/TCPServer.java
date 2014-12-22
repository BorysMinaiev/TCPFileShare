import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.codec.digest.DigestUtils;

public class TCPServer implements Runnable {
	private ServerSocket socket;
	static final int LIST = 0x1;
	static final int GET = 0x2;
	static final int PUT = 0x3;
	static final int RET_LIST = 0x4;
	static final int RET_GET = 0x5;
	static final int NO_SUCH_FILE = 0x1;
	final String dirName = Main.DIRECTORY;

	private class Connection implements Runnable {

		Socket socket;

		Connection(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@accept@@@");
			InputStream in = null;
			try {
				in =
						socket.getInputStream();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			OutputStream out = null;
			try {
				out = socket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				int cmd = in.read();
				switch (cmd) {
				case LIST: {
					System.err.println("list");
					System.err.println(socket.getInetAddress());
					out.write(Utils.writeByte(RET_LIST));
					File dir = new File(dirName);
					File[] filesList = dir.listFiles();
					int cnt = 0;
					for (File file : filesList) {
						if (file.isFile()) {
							cnt++;
						}
					}
					out.write(Utils.writeInt(cnt));
					for (File file : filesList) {
						if (file.isFile()) {
							InputStream is = new FileInputStream(file);
							out.write(DigestUtils.md5(is));
							out.write(Utils.writeString(file.getName()));
						}
					}
					break;
				}
				case GET: {
					System.err.println("get");
					String fileName = Utils.readString(in);
//					out.write(Utils.writeByte(RET_GET));
					final String dirName = Main.DIRECTORY;
					File dir = new File(dirName);
					File[] filesList = dir.listFiles();
					boolean found = false;
					System.err.println(socket.getInetAddress());
					System.err.println(fileName);
					for (File file : filesList) {
						if (file.isFile()) {
							if (file.getName().equals(fileName)) {
								out.write(Utils.writeByte(RET_GET));
								long size = file.length();
								out.write(Utils.writeLong(size));
								InputStream is = new FileInputStream(file);
								out.write(DigestUtils.md5(is));
								is.close();
								is = new FileInputStream(file);
								while (true) {
									int x = is.read();
									if (x == -1) {
										break;
									}
									out.write(x);
								}
								found = true;
								is.close();
							}
						}
					}
					if (!found) {
						out.write(Utils.writeByte(NO_SUCH_FILE));
					}
					break;
				}
				case PUT: {
					System.err.println("put");
					String fileName = Utils.readString(in);
					out.write(Utils.writeByte(RET_LIST));
					final String dirName = Main.DIRECTORY;
					File dir = new File(dirName);
					File[] filesList = dir.listFiles();
					boolean found = false;
					for (File file : filesList) {
						if (file.isFile()) {
							if (file.getName().equals(fileName)) {
								found = true;
								break;
							}
						}
					}
					System.err.println("try to put file = " + fileName);
//					if (found) {
//						System.err.println("file already exist");
//					} else {
						long size = Utils.readLong(in);
						FileOutputStream o = new FileOutputStream(new File(dirName+"/"+fileName));
						while (true) {
							int x = in.read();
							if (x == -1) {
								break;
							}
							o.write(x);
//							System.err.println((char)x);
						}
						o.close();
						System.err.println("DONE");
//					}
					break;
				}
				default:
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void run() {
		System.err.println("!!!!!!!!!");
		try {
			socket = new ServerSocket(Main.PORT);
			while (true) {
				Socket s = socket.accept();
				System.err.println("accept");
				new Thread(new Connection(s)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
