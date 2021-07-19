package ru.darvell.cloud.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.server.database.creator.DBInitializer;
import ru.darvell.cloud.server.exceptions.ServerException;


@Slf4j
public class Server {
    private final String ROOT_STORAGE_PATH = "storage";

    public void run() {
        try {
            DBInitializer dbInitializer = new DBInitializer();
            dbInitializer.doInitial();
        } catch (ServerException e) {
            log.error("Server error while check/create DB", e);
            System.exit(-1);
        }

        try {
            EventLoopGroup auth = new NioEventLoopGroup(1);
            EventLoopGroup worker = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            channel.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new MessageHandler(ROOT_STORAGE_PATH)
                            );
                        }
                    });
            ChannelFuture future = bootstrap.bind(8189).sync();
            log.debug("Server started...");
            future.channel().closeFuture().sync();
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        } catch (InterruptedException e) {
            log.error("Something wrong with server", e);
            Thread.currentThread().interrupt();
        }
    }

}
