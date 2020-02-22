package me.izhong.shop.cache;

import lombok.Getter;
import lombok.Setter;
import me.izhong.shop.entity.User;

@Getter
@Setter
public class SessionInfo extends User {

    private String timestamp;
    private String lasttimestamp;
    private String data;
}
