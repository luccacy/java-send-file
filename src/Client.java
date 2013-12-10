import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;


public class Client {
	public static void main(String[] args){
		
		try {
			int i = 0;
			for(i=0; i<1; i++){
				String filename = new String("file" + i + ".img");
				SendThread th = new SendThread(filename);
				Thread thread = new Thread(th);
				thread.start();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}

class SendThread implements Runnable{
	private String filename;
	
	SendThread(String filename){
		this.filename = filename;
	}
	
	public void run(){
		sendBigFile(this.filename);
	}
	
	private void sendBigFile(String filename){
		String urlPath = "http://10.12.13.11:7878/1/2/3/4";
		try{
			URL url = new URL(urlPath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(5 * 1000);
			conn.setDoOutput(true); // 发送POST请求， 必须设置允许输出
			conn.setUseCaches(false);
			conn.setChunkedStreamingMode(10 *10 * 1024);
			
			FileInputStream fs = new FileInputStream("e:\\ubuntu-12.04-alternate-i386.iso");
			
			//FileInputStream fs = new FileInputStream("e:\\test.zip");
			FileChannel fc = null;
			
			fc = fs.getChannel();
			
			conn.setRequestProperty("Content-Type", "application/octet-stream");
			conn.setRequestProperty("Transfer-Encoding", "chunked");
			conn.setRequestProperty("x-image-meta-size", ""+fc.size());
			conn.setRequestProperty("X-Auth-Token",
					"48bf0956adbd46b6b3867aed56fc2cd4");
			conn.setRequestProperty("x-image-meta-name", filename);

			
			DataOutputStream outStream = new DataOutputStream(
					conn.getOutputStream());
//			
//			
//			
//			MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
//			byteBuffer.allocateDirect(10 * 1024 * 1024);
//			while (byteBuffer.hasRemaining()) {
//				byte[] tempBuffer = new byte[10 * 1024 * 1024];
//				if (byteBuffer.remaining() < tempBuffer.length) {
//					byte[] tempArr = new byte[byteBuffer.remaining()];
//					byteBuffer.get(tempArr);
//					outStream.write(tempBuffer);
//				}
//				else {
//					byteBuffer.get(tempBuffer);
//					outStream.write(tempBuffer);
//				}
//				outStream.flush();
//			}


			
			InputStream inStream;
			File file = new File("e:\\test.zip");
			inStream = new FileInputStream(file);

			System.out.println("begin");
			byte[] buffer =new byte[10 *1024];
			int rlen = 0;
			int i=0;
			
			int nlen = (int) file.length();
			while(nlen > 0){
				if(nlen >= 10*1024){			
					rlen = inStream.read(buffer, 0, 10*1024);
					
					outStream.write(buffer);
					nlen = nlen - rlen;
				}else{
					buffer =new byte[nlen];
					rlen = inStream.read(buffer, 0, nlen);
					outStream.write(buffer);
					nlen = nlen - rlen;
				}
			}
			System.out.println("send end");
			outStream.flush();
			outStream.close();
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode" + responseCode);
			if (responseCode != 200) {
				throw new RuntimeException("请求url失败");
			}
			
			conn.disconnect();
			
			System.out.println("end");
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

