package ru.darvell.cloud.common.utils;

import java.nio.file.Path;

@FunctionalInterface
public interface FileDeleter {
    void delete(Path path);
}
