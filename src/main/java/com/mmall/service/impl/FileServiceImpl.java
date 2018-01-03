package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by zhonglunsheng on 2017/12/28.
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String upload(MultipartFile file, String path){
        //获取上传文件名
        String fileName = file.getOriginalFilename();
        //得到后缀名
        String fileExtensionName = fileName.substring(fileName.indexOf(".")+1);
        //生成新的文件名
        String uploadName = UUID.randomUUID().toString()+"."+fileExtensionName;

        logger.info("开始上传文件,文件名:{},上传路径:{},新文件名:{}",fileName,path,uploadName);

        File fileDir = new File(path);
        if (!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }

        File targetFile = new File(path,uploadName);
        try {
            file.transferTo(targetFile);

            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
        } catch (IOException e) {
            logger.error("上传文件异常");
            return null;
        }
        return targetFile.getName();
    }
}
