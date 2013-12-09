import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

public class MainThread {
	private final static BlockingQueue queue_send = new ArrayBlockingQueue(1);
	private final static BlockingQueue queue_recv = new ArrayBlockingQueue(1);

	public static void main(String[] args) {

		PipedOutputStream pos_send = new PipedOutputStream();
		PipedInputStream pis_send = new PipedInputStream();

		PipedOutputStream pos_recv = new PipedOutputStream();
		PipedInputStream pis_recv = new PipedInputStream();
		try {
			pis_send.connect(pos_send);
			pis_recv.connect(pos_recv);
		} catch (IOException e) {
			e.printStackTrace();
		}

		SendThread sendThread = new SendThread(pos_recv, queue_recv, pis_send,
				queue_send);
		RecvThread recvThread = new RecvThread(pis_recv, queue_recv);

		sendThread.start();
		recvThread.start();
		while (true) {
			try {

				pos_send.write(1);
				queue_send.put("send");
				Thread.sleep(100000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}

class SendThread extends Thread {
	private PipedOutputStream pos_recv;
	private BlockingQueue queue_recv;
	private PipedInputStream pis_send;
	private BlockingQueue queue_send;

	public SendThread(PipedOutputStream pos_recv, BlockingQueue queue_recv,
			PipedInputStream pis_send, BlockingQueue queue_send) {
		this.pos_recv = pos_recv;
		this.queue_recv = queue_recv;
		this.pis_send = pis_send;
		this.queue_send = queue_send;
	}

	public void run() {
		int i = 0;
		try {
			while (true) {

				this.pis_send.read();
				System.out.println("queue_send:" + this.queue_send.take());
				System.out.println("send file");
				sendFile("e:\\cirros.img");
				System.out.println("send over");
				pos_recv.write(1);
				queue_recv.put("recv");
				sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendFile(String filePath) {
		String urlPath = "http://10.12.13.11:7878/1/2/3/4";
		File file = new File(filePath);

		try {
			URL url = new URL(urlPath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(5 * 1000);
			conn.setDoOutput(true); // 发送POST请求， 必须设置允许输出
			conn.setUseCaches(false);

			conn.setRequestProperty("Content-Type", "application/octet-stream");
			conn.setRequestProperty("Transfer-Encoding", "chunked");
			conn.setRequestProperty("x-image-meta-size", "" + file.length());
			conn.setRequestProperty("x-image-meta-name", file.getName());

			DataOutputStream outStream = new DataOutputStream(
					conn.getOutputStream());

			InputStream inStream = new FileInputStream(file);

			byte[] buffer = new byte[10 * 1024];
			int rlen = 0;

			int nlen = (int) file.length();
			while (nlen > 0) {
				if (nlen >= 10 * 1024) {
					rlen = inStream.read(buffer, 0, 10 * 1024);
					outStream.write(buffer);
					nlen = nlen - rlen;
				} else {
					buffer = new byte[nlen];
					rlen = inStream.read(buffer, 0, nlen);
					outStream.write(buffer);
					nlen = nlen - rlen;
				}
			}

			outStream.flush();
			outStream.close();
			inStream.close();
			int responseCode = conn.getResponseCode();

			if (responseCode != 200) {
				throw new RuntimeException("请求url失败");
			}

			conn.disconnect();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class RecvThread extends Thread {
	private PipedInputStream pis_recv;
	private BlockingQueue queue_recv;
	private HttpURLConnection conn;

	public RecvThread(PipedInputStream pis_recv, BlockingQueue queue_recv) {
		this.pis_recv = pis_recv;
		this.queue_recv = queue_recv;
	}

	public void run() {
		try {
			while (true) {
				System.out.println("pis_recv:" + pis_recv.read());
				System.out.println("queue_recv:" + queue_recv.take());
				System.out.println("get status");
				initConn();
				while(true){
					if(getStatus().equals("ready")){
						break;
					}else{
						Thread.sleep(1000);
					}
				}
				closeConn();
				initConn();
				System.out.println("get status over");
				System.out.println("recv file");
				recvFile("e:\\test.img");
				System.out.println("recv over");
				closeConn();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initConn() {
		String urlPath = "http://10.12.13.11:7878/1/2/3/4";
		URL url;
		try {
			url = new URL(urlPath);

			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(5 * 1000);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void closeConn() {
		conn.disconnect();
	}

	private String getStatus() {

		StringBuilder str = new StringBuilder();

		try {
			conn.setRequestMethod("PUT");

			/****/
			InputStream inStream = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inStream));
			str.append(reader.readLine());
			System.out.println(str.toString());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str.toString();
	}

	private void recvFile(String filePath) {
		File file = new File(filePath);

		try {
			conn.setRequestMethod("GET");

			InputStream inStream = conn.getInputStream();
			OutputStream outStream = new FileOutputStream(file);

			int len = 0;
			byte[] data = new byte[1024];
			while ((len = inStream.read(data, 0, 1024)) != -1) {
				outStream.write(data, 0, len);
			}

			outStream.flush();
			outStream.close();
			inStream.close();
			int responseCode = conn.getResponseCode();

			if (responseCode != 200) {
				throw new RuntimeException("请求url失败");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
