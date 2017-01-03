package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * netty阻塞io
 *
 */
public class NettyOioServer {
	public void server(int port) throws Exception{
		final ByteBuf buf=Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n",CharsetUtil.UTF_8));
		//时间循环组
		EventLoopGroup group=new NioEventLoopGroup();
		try{
			//用来引导服务器配置
			ServerBootstrap b=new ServerBootstrap();
			//使用oio阻塞模式
			b.group(group).channel(OioServerSocketChannel.class).localAddress(new InetSocketAddress(port))
			//使用指定channelInitializer初始化handlers
			.childHandler(new ChannelInitializer<Channel>(){
				@Override
				protected void initChannel(Channel ch) throws Exception {
					//添加一个入站handler到ChannelPipeline
					ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
						@Override
						public void channelActive(ChannelHandlerContext ctx){
							//连接后,写消息到客户端,写完后关闭连接
							ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);
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
