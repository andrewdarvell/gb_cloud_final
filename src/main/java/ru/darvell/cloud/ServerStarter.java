package ru.darvell.cloud;

import ru.darvell.cloud.server.Server;

public class ServerStarter {
    public static void main(String[] args) {
        new Server().run();
    }
}
