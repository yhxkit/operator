package com.sample.operator.app.common.connection;


import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

@Component
public class TransmissionControlProtocol {

    public StringBuffer socketConn(String domain, int port, int timeout, String packet)
    {
        StringBuffer buffer = new StringBuffer();
        
        try(Socket socket = new Socket())
        {
            InetSocketAddress socketAddress = new InetSocketAddress(domain, port);
            socket.connect(socketAddress, timeout);
            socket.setSoTimeout(timeout);
            socket.setTcpNoDelay(true);
            
            //입력 스트림
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // 출력 스트림
            OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
            
            writer.write(packet);
            writer.flush();
            String line;
            while ((line = reader.readLine()) != null) 
            {
                buffer.append(line);
                // 엔진에서 소켓 종료하지 않을 경우 break 로 길이체크하여 끊어야 함
            }
        }
        catch (SocketTimeoutException e)
        {
            System.out.println("Socket timed out");
        }
        catch (Exception e)
        {
            System.out.println("통신 오류");
        }
         return buffer;
    }
}
