package com.sample.operator.config.webSocket;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.security.Principal;

@Configuration
public class WebSocketConfig extends ServerEndpointConfig.Configurator
{

    // 메시지 전파
    @Bean
    public ServerEndpointExporter serverEndpointExporter()
    {
        return new ServerEndpointExporter();
    }

    // session 정보 추가
    @Bean
    public WebSocketFilter webSocketFilter()
    {
        return new WebSocketFilter();
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        HttpServletRequest req = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        String host = req.getRemoteHost();
        String addr = req.getRemoteAddr();

        Principal principal = req.getUserPrincipal();
        String username = principal == null ? NamedChatProp.anonym : principal.getName();

        sec.getUserProperties().put(NamedChatProp.userName, username);
        sec.getUserProperties().put(NamedChatProp.host, host);
        sec.getUserProperties().put(NamedChatProp.ip, addr);

        super.modifyHandshake(sec, request, response);
    }
}
