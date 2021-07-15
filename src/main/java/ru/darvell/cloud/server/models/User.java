package ru.darvell.cloud.server.models;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@Data
public class User {

    private int id;
    private String login;
    private String password;
    private String workDirName;

}
