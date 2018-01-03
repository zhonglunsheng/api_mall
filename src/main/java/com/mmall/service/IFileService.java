package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by zhonglunsheng on 2017/12/28.
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
