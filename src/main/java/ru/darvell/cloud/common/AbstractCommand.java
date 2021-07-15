package ru.darvell.cloud.common;

import java.io.Serializable;

public abstract class AbstractCommand implements Serializable {
    public abstract CommandType getType();
}
