package ru.darvell.cloud.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.server.database.DBConnection;

import java.sql.Connection;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand command) throws Exception {
        Connection connection = DBConnection.getSqliteDBConnection();
        log.debug("received: {}", command);
//        switch (command.getType()) {
//            case FILE_MESSAGE:
//                FileMessage message = (FileMessage) command;
//                try (FileOutputStream fos = new FileOutputStream("server_dir/" + message.getName())) {
//                    fos.write(message.getData());
//                }
//                break;
//        }
    }
}
