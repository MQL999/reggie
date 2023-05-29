package com.minqiliang.controller;

import com.minqiliang.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.basePath}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        // 获取文件原名
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        // 截取文件后缀
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 拼接文件名
        String fileName = UUID.randomUUID().toString() + substring;

        // 如果路径不存在，就创建
        File f = new File(basePath);
        if(!f.exists()){
            f.mkdirs();
        }

        // 转存文件到指定路径
        try{
            file.transferTo(new File(basePath + fileName));
        }catch (Exception e){
            e.printStackTrace();
        }

        // 返回文件名
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws FileNotFoundException {
        try{
            // 创建输入输出流
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            ServletOutputStream outputStream = response.getOutputStream();

            // 设置响应类型
            response.setContentType("image/jpeg");

            // 读取文件并写出
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            // 关闭资源
            fileInputStream.close();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
