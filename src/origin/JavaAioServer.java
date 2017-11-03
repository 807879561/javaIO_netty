package origin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.*;

/**
 * @author shiyizhen
 */
public class JavaAioServer {
    public static void main(String[] args) throws IOException {
        int port=7000;
        ExecutorService taskExecutor= Executors.newCachedThreadPool(Executors.defaultThreadFactory());
        try(AsynchronousServerSocketChannel asynchronousServerSocketChannel=AsynchronousServerSocketChannel.open()){
            if(asynchronousServerSocketChannel.isOpen()){
                asynchronousServerSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF,4*1024);
                asynchronousServerSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR,true);
                asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
                System.out.println("wait connect...");
                while(true){
                    Future<AsynchronousSocketChannel> asynchronousSocketChannelFuture=asynchronousServerSocketChannel.accept();
                    try{
                        final AsynchronousSocketChannel asynchronousSocketChannel=asynchronousSocketChannelFuture.get();
                        Callable<String> worker= () -> {
                            String host=asynchronousSocketChannel.getRemoteAddress().toString();
                            System.out.println("connect from:"+host);
                            final ByteBuffer buffer=ByteBuffer.allocateDirect(1024);
                            while(asynchronousSocketChannel.read(buffer).get()!=-1){
                                buffer.flip();
                                asynchronousSocketChannel.write(buffer).get();
                                if(buffer.hasRemaining()){
                                    buffer.compact();
                                }else{
                                    buffer.clear();
                                }
                            }
                            asynchronousSocketChannel.close();
                            System.out.println(host+"was served");
                            return host;
                        };
                        taskExecutor.submit(worker);
                    }catch(InterruptedException | ExecutionException ex){
                        System.err.println("ex");
                        System.err.println("\n server is shutting down");
                    }
                    //执行器不再接受新线程
                    //并完成队列中所有线程
                    taskExecutor.shutdown();

                    //等待所有线程完成
                    while(!taskExecutor.isTerminated()){

                    }
                    break;
                }
            }else{
                System.out.println("server socket can not be opened");
            }
        }catch(IOException ex){
            System.err.println(ex);
        }
    }
}
