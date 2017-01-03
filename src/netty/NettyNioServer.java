package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * netty非阻塞io
 *
 */
public class NettyNioServer {

	public void server(int port) throws Exception{
		final ByteBuf buf=Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n",CharsetUtil.UTF_8));
		//时间循环组
		EventLoopGroup group=new NioEventLoopGroup();
		try{
			//用来引导服务器配置
			ServerBootstrap b=new ServerBootstrap();
			//使用nio异步模式
			b.group(group).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(port))
			//使用指定channelInitializer初始化handlers
			.childHandler(new ChannelInitializer<SocketChannel>(){
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					//添加一个入站handler到ChannelPipeline
					ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
						@Override
						public void channelActive(ChannelHandlerContext ctx){
							//连接后,写消息到客户端,写完后关闭连接
							ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);//duplicate创建共享此缓冲区内容的新的字节缓冲区
						}
					});
					
				}
				
			});
			//绑定服务器接收连接
			ChannelFuture f=b.bind().sync();
			f.channel().closeFuture().sync();
			
		}catch(Exception e){
			group.shutdownGracefully();
		}
	}

}
