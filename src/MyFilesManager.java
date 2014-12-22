import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class MyFilesManager implements Runnable {

	private static Logger log = Logger.getLogger(Main.class.getName());
	private static final long SLEEP_TIME = 10000;
	private final String ip;
	
	public MyFilesManager(String ip) {
		this.ip = ip;
	}

	@Override
	public void run() {
		while (true) {
			final String dirName = Main.DIRECTORY;
			File dir = new File(dirName);
			File[] filesList = dir.listFiles();
			int cnt = 0;
			long timestamp = 0;
			for (File file : filesList) {
			    if (file.isFile()) {
			    	timestamp = Math.max(timestamp, file.lastModified());
			    	cnt++;
			    }
			}	
			//System.err.println(timestamp);
			//System.err.println("real date = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp)));
			try {
				UdpAnnouncer.announce(ip, cnt, timestamp, Main.NAME);
			} catch (IOException e1) {
				log.severe("IOException:(");
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				log.severe("interrupted exception");
				System.exit(0);
			}
		}
	}

}
