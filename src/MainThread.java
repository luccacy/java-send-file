
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

public class MainThread {
	private final static BlockingQueue queue = new ArrayBlockingQueue(1);
	
	public static void main(String[] args) {
        /**
         * 创建管道输出流
         */
        PipedOutputStream pos = new PipedOutputStream();

        /**
         * 创建管道输入流
         */
        PipedInputStream pis = new PipedInputStream();
        try {
            /**
             * 将管道输入流与输出流连接 此过程也可通过重载的构造函数来实现
             */
            pis.connect(pos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         * 创建生产者线程
         */
        Producer p = new Producer(pos, queue);
        /**
         * 创建消费者线程
         */
        Consumer1 c1 = new Consumer1(pis, queue);
      
        /**
         * 启动线程
         */
        p.start();
        c1.start();

    }
}

class Producer extends Thread {
    private PipedOutputStream pos;
    private BlockingQueue queue;

    public Producer(PipedOutputStream pos, BlockingQueue queue) {
        this.pos = pos;
        this.queue = queue;
    }

    public void run() {
        int i = 0;
        try {
            while(true)
            {
            this.sleep(1000);
            pos.write(i);
            queue.put("hello");
            i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * 消费者线程(与一个管道输入流相关联)
 * 
 */
class Consumer1 extends Thread {
    private PipedInputStream pis;
    private BlockingQueue queue;

    public Consumer1(PipedInputStream pis, BlockingQueue queue) {
        this.pis = pis;
        this.queue = queue;
    }

    public void run() {
        try {
            while(true)
            {
            	
            System.out.println("consumer1:"+pis.read());
            
				System.out.println("consumer1:"+queue.take());
			
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}


