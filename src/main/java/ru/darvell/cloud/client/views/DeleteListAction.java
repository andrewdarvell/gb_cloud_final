package ru.darvell.cloud.client.views;

@FunctionalInterface
public interface DeleteListAction {
    void delete(String fileName);
}
