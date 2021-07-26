package ru.darvell.cloud.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.darvell.cloud.client.views.FileListItem;
import ru.darvell.cloud.common.AbstractCommand;
import ru.darvell.cloud.common.CommandType;
import ru.darvell.cloud.common.commands.*;
import ru.darvell.cloud.common.utils.FileTransmitter;
import ru.darvell.cloud.server.database.repositories.UserRepositoryImpl;
import ru.darvell.cloud.server.database.repositories.UsersRepository;
import ru.darvell.cloud.server.models.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    private boolean authorized = false;
    private Path rootPath;
    private User user;

    private int catalogDeep = 0;
    private final FileTransmitter fileTransmitter;


    public MessageHandler(String rootPathUri) {
        log.info("Start new message handler");
        this.rootPath = Paths.get(rootPathUri);
        fileTransmitter = new FileTransmitter(rootPath, this::deleteFile, 8 * 1024);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand command) {
        log.debug("received: {}", command);
        if (!authorized) {
            processAuthOrRegister(ctx, command);
        } else {
            doUserActions(ctx, command);
        }
    }

    private void processAuthOrRegister(ChannelHandlerContext ctx, AbstractCommand command) {
        if (command.getType() == CommandType.AUTH_MESSAGE) {
            AuthCommand authCommand = (AuthCommand) command;
            if (authUser(authCommand)) {
                authorized = true;
                log.info("trying auth user");
                ctx.writeAndFlush(new SuccessAuthMessage());
            } else {
                sendErrorMessage("Invalid credentials", ctx);
            }
        } else if (command.getType() == CommandType.REGISTER_MESSAGE) {
            RegisterCommand registerCommand = (RegisterCommand) command;
            if (doRegister(registerCommand, ctx)) {
                authorized = true;
                log.info("trying register user");
                ctx.writeAndFlush(new SuccessAuthMessage());
            }
        } else {
            sendErrorMessage("Auth first!", ctx);
        }
    }

    private boolean authUser(AuthCommand authCommand) {
        UsersRepository usersRepository = new UserRepositoryImpl();
        Optional<User> userOptional = usersRepository.authUser(authCommand.getLogin(), authCommand.getPassword());
        if (userOptional.isPresent()) {
            user = userOptional.get();
            boolean rootPathPrepareResult = prepareRootPath();
            if (!rootPathPrepareResult) {
                return false;
            }
            fileTransmitter.prepareServerFileCheckers(user, rootPath);
            return true;
        } else {
            return false;
        }
    }

    private boolean doRegister(RegisterCommand registerCommand, ChannelHandlerContext ctx) {
        UsersRepository usersRepository = new UserRepositoryImpl();
        Optional<User> userOptional = usersRepository.getuserByLogin(registerCommand.getLogin());
        if (userOptional.isPresent()) {
            sendErrorMessage("Login already exist", ctx);
            return false;
        } else {
            user = usersRepository.addUser(User.builder()
                    .login(registerCommand.getLogin())
                    .password(registerCommand.getPassword())
                    .workDirName(registerCommand.getLogin())
                    .build()
            );
            boolean rootPathPrepareResult = prepareRootPath();
            if (!rootPathPrepareResult) {
                return false;
            }
            fileTransmitter.prepareServerFileCheckers(user, rootPath);
            return true;
        }
    }

    private boolean prepareRootPath() {
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
                processFileMessage((FileMessage) command, ctx);
                break;
            case FILE_REQUEST:
                doSendFile(ctx, (RequestFileMessage) command);
                break;
            case DELETE_REQUEST:
                doDelete((DeleteCommand) command, ctx);
                break;
            case REGISTER_MESSAGE:


        }
    }


    private void doDelete(DeleteCommand command, ChannelHandlerContext ctx) {
        Path path = rootPath.resolve(command.getFileName());
        deleteFile(path);
        sendListMessage(ctx);
    }


    private void sendListMessage(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new ListMessage(rootPath.toString(), getRootPathChilds()));
    }

    private List<FileListItem> getRootPathChilds() {
        if (Files.isDirectory(rootPath)) {
            try (Stream<Path> paths = Files.list(rootPath)) {
                return paths.map(i -> new FileListItem(i.getFileName().toString(), Files.isDirectory(i)))
                        .collect(Collectors.toList());
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

    private void processFileMessage(FileMessage fileMessage, ChannelHandlerContext ctx) {

        fileTransmitter.receiveFileOnServer(fileMessage, () -> sendListMessage(ctx));

    }

    private void deleteFile(Path path) {
        try {
            if (Files.isDirectory(path)) {
                deleteFolder(path);
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            log.error("Error while delete existing file", e);
        }
    }

    private void deleteFolder(Path path) {
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void doSendFile(ChannelHandlerContext ctx, RequestFileMessage command) {
        if (command.getName() != null && !command.getName().isEmpty()) {
            Path pathNew = rootPath.resolve(command.getName());
            if (!Files.isDirectory(pathNew) && Files.exists(pathNew)) {
                fileTransmitter.sendFile(pathNew, ctx::writeAndFlush
                        , progress -> {
                        }
                        , () -> {
                        }
                );
            }
        }
    }
}
