package com.sample.operator.app.svc.fileBiz;

import com.sample.operator.app.common.exception.OperException;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@NoArgsConstructor
public class ChatLogSvc {

    String path = "C:\\tempLog";

    public byte[] getTodayChat()
    {
        Path p = getTodayLogDir();
        if(!p.toFile().exists())
        {
            System.out.println("없는 파일입니다 "+ p);
            throw new OperException("없는 파일입니다");
        }
        else
        {
            try
            {
                return Files.readAllBytes(p);
            }
            catch(Exception e)
            {
                System.out.println("오류 발생 " + p);
                throw new OperException("파일 호출 오류 발생");
            }
        }
    }


    public void writeLog(String msg)
    {
        try(BufferedWriter bw = Files.newBufferedWriter(getTodayLogDir(), StandardOpenOption.CREATE, StandardOpenOption.APPEND))
        {
            String logLine = String.format("[%s] %s%n", getTimeNow(), msg);
            bw.write(logLine);
        } catch (Exception e) {
            System.out.println("로그 추가 실패");
        }
    }

    private Path getTodayLogDir()
    {
        File dir = new File(path);

        if(!dir.exists()){
            System.out.println("유효한 경로가 아닙니다");
        }
        return Paths.get(path).resolve(getTodayLogName());
    }


    private String getTimeNow()
    {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }


    private String getTodayLogName()
    {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return "chatLog_"+date+".log";
    }
}
