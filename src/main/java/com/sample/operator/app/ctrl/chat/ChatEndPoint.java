package com.sample.operator.app.ctrl.chat;

import com.google.gson.JsonObject;
import com.sample.operator.app.dto.chat.ChatUserInfo;
import com.sample.operator.app.svc.fileBiz.ChatLogSvc;
import com.sample.operator.config.webSocket.NamedChatProp;
import com.sample.operator.config.webSocket.SpringContext;
import com.sample.operator.config.webSocket.WebSocketConfig;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ServerEndpoint(value = "chatty", configurator = WebSocketConfig.class)
public class ChatEndPoint {

    private ChatLogSvc chatLogSvc;

    // 접속자 세션
    private static final Set<Session> clientSessions = Collections.synchronizedSet(new HashSet<>());

    // 접속자 정보 셋
    private static Set<ChatUserInfo> userInfos = Collections.synchronizedSet(new HashSet<>());


    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        System.out.println("오픈!" + session.getId());

        if( isDuplicatedIp(session) || clientSessions.contains(session))
        {
            System.out.println("중복 입장 "+ isDuplicatedIp(session) + clientSessions.contains(session));
            clientSessions.remove(session);
        }
        
        clientSessions.add(session);
        
        try
        {
            JsonObject jo = new JsonObject();
            jo.addProperty(NamedChatProp.allClientInfoStr, getAllClientStr());
            jo.addProperty(NamedChatProp.alertMsg, getSessionClientStr(session) + " 입장");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("입장 메시지 생성 중 오류 발생");
        }

    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("클로즈 / " + closeReason.getReasonPhrase());
        clientSessions.remove(session);
        
        try{
            JsonObject jo = new JsonObject();
            jo.addProperty(NamedChatProp.allClientInfoStr, getAllClientStr());
            jo.addProperty(NamedChatProp.alertMsg, getSessionClientStr(session) + " 퇴장");

            exportToAll(jo.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("퇴장 메시지 생성 오류");
        }
    }

    @OnMessage
    public void onTextMsg(String msg, Session session) {
        String time = getTimeInfo();
        System.out.println("메시지 수신 " + time + "by client ip " + getSessionClientStr(session));

        try{
            JsonObject jo = new JsonObject();
            jo.addProperty(NamedChatProp.timeInfo, time);
            jo.addProperty(NamedChatProp.clientInfoStr, getSessionClientStr(session));

            if(msg.startsWith(NamedChatProp.downloadableDelimiter))
            {
                jo.addProperty(NamedChatProp.downloadable, msg.replace(NamedChatProp.downloadableDelimiter, ""));
            }
            else
            {
                jo.addProperty(NamedChatProp.plainMsg, msg);
            }
            exportToAll(jo.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("메시지 생성 중 오류 발생");
        }
    }

    @OnMessage
    public void onBinaryMsg(byte[] data, Session session) {
        String time = getTimeInfo();
        System.out.println("메시지 파일 수신 " + time + "by client ip " + getSessionClientStr(session));

        if(data != null && data.length > 0)
        {
            System.out.println("수신 파일 크키 " + data.length + " bytes");
            exportToAll(getSessionClientStr(session) + "의 바이너리 메시지를 수신할 수 없습니다. 정상적인 경로로 파일을 업로드해주세요.");
        }
    }

    // 중복 IP 체크
    private boolean isDuplicatedIp(Session session) {
        return getAllClientStr().contains(getSessionClientStr(session));
    }


    // 전체 세션 참가자 정보 문자열
    private String getAllClientStr() {
        return userInfos.stream().map(ChatUserInfo::getIp).collect(Collectors.joining(","));
    }


    // 해당 세션 참가자 정보 문자열
    private String getSessionClientStr(Session session) {
        return convertSessionToUserInfo(session);
    }


    // 세션 정보를 유저정보로 변환 후 Set에서 관리 / 유저의 IP 정보를 반환
    private String convertSessionToUserInfo(Session session) {
        ChatUserInfo user = new ChatUserInfo(session);

        if (userInfos == null) {
            userInfos = Collections.synchronizedSet(new HashSet<>());
        }

        userInfos.add(user);

        return user.getIp();
    }


    // 현재 시간 정보
    private String getTimeInfo()
    {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    // 전체 방에 메시지 송출
    private void exportToAll(String msg) {
        for (Session client : clientSessions) {
            if (client.isOpen()) {
                try {
                    client.getBasicRemote().sendText(msg);
                } catch (Exception e) {
                    System.out.println("송출 실패! >> " + client.getId());
                }
            }
        }


        if (chatLogSvc == null) {
            chatLogSvc = SpringContext.getBean(ChatLogSvc.class);
        }

        // 메시지 로깅
        chatLogSvc.writeLog(msg);
    }
}
