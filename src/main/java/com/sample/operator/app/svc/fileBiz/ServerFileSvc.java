package com.sample.operator.app.svc.fileBiz;

import com.sample.operator.app.common.exception.OperException;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@NoArgsConstructor
public class ServerFileSvc {

    String path = "C:/temp";

    public byte[] getFile(String fileName) {
        String getFilePath = path + File.separator + fileName;
        File file = new File(getFilePath);
        
        if(!file.exists())
        {
            System.out.println("없는 파일");
            throw new OperException("없는 파일");
        }
        else 
        {
            try
            {
                Path path =Path.of(getFilePath);
                return Files.readAllBytes(path);
            }catch (Exception e)
            {
                System.out.println("파일 리드 중 오류");
                throw new OperException(e.getMessage());
            }
        }
    }


    public String saveFile(String fileName, byte[] data) 
    {
        File dir = new File(path);
        
        if(!dir.exists())
        {
            System.out.println("경로를 확인하세요");
            return null;
        }
        
        try{
            boolean isNoneExtFile = fileName.lastIndexOf(".") != -1; // 확장자 체크
            String fileNameOnly = isNoneExtFile ? fileName : fileName.substring(0, fileName.lastIndexOf("."));
            String fileExt = isNoneExtFile ? "" : "."+fileName.substring(fileName.lastIndexOf(".")+1);
            
            String saveFilePath = path + File.separator + fileName;
            File saveFile = new File(saveFilePath); // 아직 저장되지 않고 객체만 생성

            // 동일한 파일이 있을 경우 파일(1).확장자 형식으로 생성
            if(saveFile.isFile())
            {
                boolean isExistFile = true;
                int idx = 0;

                while(isExistFile)
                {
                    idx++;
                    String newFileName = fileName + "("+ idx + ")" + fileExt;
                    String newFilePath = path + File.separator + newFileName;
                    isExistFile = new File(newFilePath).isFile();

                    if(!isExistFile)
                    {
                        fileName = newFileName;
                        saveFilePath = newFilePath;
                        saveFile = new File(saveFilePath);
                    }
                }
            }

            FileOutputStream fos = new FileOutputStream(saveFile);
            fos.write(data);
            fos.close();

            System.out.println("저장 결과");
            return fileName;

        } catch (Exception e) {
            System.out.println("파일 저장 실패");
            return null;
        }
    }
    
    public void renameFiles(String filePath, HashMap<String, String> fileNames)
    {
        System.out.println("파일명 변경 시작");
        try{
            for(Map.Entry<String, String> entry : fileNames.entrySet())
            {
                String oldPath = filePath + entry.getKey();
                String newPath = path + entry.getValue();

                Path oldP = Paths.get(oldPath);
                Path newP = Paths.get(newPath);

                System.out.println("파일명 변경 " + oldPath + "->" + newPath);
                
                Files.move(oldP, newP, StandardCopyOption.REPLACE_EXISTING);

            }
        }catch(Exception e)
        {
            System.out.println("파일명 변경 실패");
        }
        System.out.println("파일명 변경 완료");
    }


    public boolean delDir(File targetFolder)
    {
        if(!targetFolder.exists())
        {
            System.out.println("경로가 없음");
            return false;
        }
        else
        {
            File[] files = targetFolder.listFiles();

            for(File f : files)
            {
                if(f.isDirectory())
                {
                    System.out.println("디렉토리 내부 파일 삭제합니다");
                    delDir(f);
                }
                else
                {
                    boolean rst = f.delete();
                    String msg = rst ? "파일 삭제 성공" : "파일 삭제 실패";
                    System.out.println(msg);
                }
            }
            return targetFolder.delete();
        }
    }


    public boolean mkdir(String path)
    {
        try{
            Files.createDirectory(Paths.get(path));
            System.out.println("디렉토리 생성 성공" + path);
            return true;
        }
        catch (FileAlreadyExistsException e)
        {
            System.out.println("이미 있는 경로");
        }
        catch (NoSuchFileException e)
        {
            System.out.println("경로 없음");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("경로 생성 실패");
        }
        return false;
    }

    public void addFileToZip(ZipOutputStream zos, byte[] fileByteArr, String entryName)
    {
        try{
            ZipEntry ze = new ZipEntry(entryName);
            zos.putNextEntry(ze);
            zos.write(fileByteArr);
            zos.closeEntry();
        }catch (Exception e){
            e.printStackTrace();
            throw new OperException(e.getMessage());
        }
    }
}
