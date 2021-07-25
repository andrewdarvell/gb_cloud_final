package ru.darvell.cloud.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;
import ru.darvell.cloud.common.commands.*;
import ru.darvell.cloud.common.utils.FileTransmitter;
import ru.darvell.cloud.server.database.repositories.UserRepositoryImpl;
import ru.darvell.cloud.server.database.repositories.UsersRepository;
import ru.darvell.cloud.server.models.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    private boolean authorized = false;
    private Path rootPath;

    private int catalogDeep = 0;
    private FileTransmitter fileTransmitter = new FileTransmitter(this::deleteFile, 8 * 1024);


    public MessageHandler(String rootPathUri) {
        log.info("Start new message handler");
        this.rootPath = Paths.get(rootPathUri);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand command) {
        log.debug("received: {}", command);
        if (!authorized) {
            processAuth(ctx, command);
        } else {
            doUserActions(ctx, command);
        }
    }

    private void processAuth(ChannelHandlerContext ctx, AbstractCommand command) {
        if (command.getType() == CommandType.AUTH_MESSAGE) {
            AuthCommand authCommand = (AuthCommand) command;
            if (authUser(authCommand)) {
                authorized = true;
                log.info("trying auth user");
                ctx.writeAndFlush(new SuccessAuthMessage());
            } else {
                sendErrorMessage("Invalid credentials", ctx);
            }
        } else {
            sendErrorMessage("Auth first!", ctx);
        }
    }

    private boolean authUser(AuthCommand authCommand) {
        UsersRepository usersRepository = new UserRepositoryImpl();
        Optional<User> userOptional = usersRepository.authUser(authCommand.getLogin(), authCommand.getPassword());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            this.rootPath = this.rootPath.resolve(user.getWorkDirName());
            if (!Files.exists(rootPath)) {
                try {
                    Files.createDirectory(rootPath);
                } catch (Exception e) {
                    log.error("error while create user root dir", e);
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void sendErrorMessage(String message, ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new ErrorMessage(message));
    }

    private void doUserActions(ChannelHandlerContext ctx, AbstractCommand command) {
        CommandType commandType = command.getType();
        switch (commandType) {
            case LIST_REQUEST:
                sendListMessage(ctx);
                break;
            case CD_REQUEST:
                doCdCommand(((CdCommand) command).getDir());
                sendListMessage(ctx);
                break;
            case UP_REQUEST:
                doUpCommand();
                sendListMessage(ctx);
                break;
            case FILE_MESSAGE:
                processFileMessage((FileMessage) command);
                sendListMessage(ctx);
                break;
            case FILE_REQUEST:
                doSendFile(ctx, (RequestFileMessage) command);
                break;

        }
    }


    private void sendListMessage(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new ListMessage(rootPath.toString(), getRootPathChilds()));
    }

    private List<String> getRootPathChilds() {
        if (Files.isDirectory(rootPath)) {
            try (Stream<Path> paths = Files.list(rootPath)) {
                return paths.map(path -> path.getFileName().toString()).collect(Collectors.toList());
            } catch (IOException e) {
                log.error("Cannot get root childs", e);
            }
        }
        return Collections.emptyList();
    }

    private void doCdCommand(String dir) {
        log.debug("dir for cd: {}", dir);
        Path pathNew = rootPath.resolve(dir);
        if (Files.isDirectory(pathNew)) {
            rootPath = pathNew;
            increaseDeep();
        }
    }

    private void doUpCommand() {
        if (catalogDeep > 0) {
            Path pathNew = rootPath.getParent();
            if (pathNew != null) {
                rootPath = pathNew;
                decreaseDeep();
            }
        }
    }

    private void increaseDeep() {
        catalogDeep += 1;
    }

    private void decreaseDeep() {
        catalogDeep -= 1;
    }

    private void processFileMessage(FileMessage fileMessage) {

        fileTransmitter.receiveFile(fileMessage, rootPath, progress -> {});

    }

    private void deleteFile(Path path) {
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                log.error("Error while delete existing file", e);
            }
        }
    }

    private void doSendFile(ChannelHandlerContext ctx, RequestFileMessage command) {
        if (command.getName() != null && !command.getName().isEmpty()) {
            Path pathNew = rootPath.resolve(command.getName());
            if (!Files.isDirectory(pathNew) && Files.exists(pathNew)) {
                fileTransmitter.sendFile(pathNew, ctx::writeAndFlush
                        , progress -> {
                        }
                );
            }
        }
    }
}
