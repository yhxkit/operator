package com.sample.operator.app.dto.chat;

import com.sample.operator.config.webSocket.NamedChatProp;
import jakarta.websocket.Session;
import lombok.Data;

@Data
public class ChatUserInfo
{
    String name;
    String ip;
    String host;

    public ChatUserInfo(Session session)
    {
        ip = session.getUserProperties().getOrDefault(NamedChatProp.ip, NamedChatProp.unknown).toString();
        host = session.getUserProperties().getOrDefault(NamedChatProp.host, NamedChatProp.unknown).toString();
        name = session.getUserProperties().getOrDefault(NamedChatProp.userName, NamedChatProp.anonym).toString();
    }
}
