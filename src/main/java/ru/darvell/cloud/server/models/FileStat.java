package ru.darvell.cloud.server.models;

import lombok.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString
@EqualsAndHashCode
public class FileStat {

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private int id;
    private int userId;
    private String fileName;
    private String filePath;
    private Date lastUpdate;

    public void setLastUpdate(Date date) {
        this.lastUpdate = date;
    }

    public void setLastUpdate(String s) {
        try {
            this.lastUpdate = simpleDateFormat.parse(s);
        } catch (ParseException e) {
            this.lastUpdate = new Date(System.currentTimeMillis());
        }
    }

    public String getLastUpdateInStr() {
        return simpleDateFormat.format(this.lastUpdate);
    }


}
