package win.pangniu.learn.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import win.pangniu.learn.mapper.FileMd5Mapper;
import win.pangniu.learn.mapper.FileUploadStatusMapper;
import win.pangniu.learn.param.MultipartFileParam;
import win.pangniu.learn.pojo.FileMd5;
import win.pangniu.learn.pojo.FileUploadStatus;
import win.pangniu.learn.service.StorageService;
import win.pangniu.learn.utils.Constants;
import win.pangniu.learn.utils.FileMD5Util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by 超文 on 2017/5/2.
 */
@Service
public class StorageServiceImpl implements StorageService {

    private final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);
    // 保存文件的根目录
    private Path rootPath;

    @Autowired
    private FileMd5Mapper fileMd5Mapper;

    @Autowired
    private FileUploadStatusMapper fileUploadStatusMapper;

    // 这个必须与前端设定的值一致
    @Value("${breakpoint.upload.chunkSize}")
    private long CHUNK_SIZE;

    @Value("${breakpoint.upload.dir}")
    private String finalDirPath;

    @Autowired
    public StorageServiceImpl(@Value("${breakpoint.upload.dir}") String location) {
        this.rootPath = Paths.get(location);
    }

    @Override
    public void deleteAll() {
        logger.info("开发初始化清理数据，start");
        FileSystemUtils.deleteRecursively(rootPath.toFile());
        QueryWrapper wrapperFile = new QueryWrapper();
        wrapperFile.gt("id", 0);
        fileMd5Mapper.delete(wrapperFile);
        QueryWrapper wrapperStatus = new QueryWrapper();
        wrapperStatus.gt("id", 0);
        fileUploadStatusMapper.delete(wrapperStatus);
        logger.info("开发初始化清理数据，end");
    }

    @Override
    public void init() {
        try {
            Files.createDirectory(rootPath);
        } catch (FileAlreadyExistsException e) {
            logger.error("文件夹已经存在了，不用再创建。");
        } catch (IOException e) {
            logger.error("初始化root文件夹失败。", e);
        }
    }

    @Override
    public void uploadFileRandomAccessFile(MultipartFileParam param) throws IOException {
        String fileName = param.getName();
        String tempDirPath = finalDirPath + param.getMd5();
        String tempFileName = fileName + "_tmp";
        File tmpDir = new File(tempDirPath);
        File tmpFile = new File(tempDirPath, tempFileName);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        RandomAccessFile accessTmpFile = new RandomAccessFile(tmpFile, "rw");
        long offset = CHUNK_SIZE * param.getChunk();
        // 定位到该分片的偏移量
        accessTmpFile.seek(offset);
        // 写入该分片数据
        accessTmpFile.write(param.getFile().getBytes());
        // 释放
        accessTmpFile.close();

        boolean isOk = checkAndSetUploadProgress(param, tempDirPath);
        if (isOk) {
            boolean flag = renameFile(tmpFile, fileName);
            System.out.println("upload complete !!" + flag + " name=" + fileName);
        }
    }

    @Override
    public void uploadFileByMappedByteBuffer(MultipartFileParam param) throws IOException {
        String fileName = param.getName();
        String uploadDirPath = finalDirPath + param.getMd5();
        String tempFileName = fileName + "_tmp";
        File tmpDir = new File(uploadDirPath);
        File tmpFile = new File(uploadDirPath, tempFileName);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        RandomAccessFile tempRaf = new RandomAccessFile(tmpFile, "rw");
        FileChannel fileChannel = tempRaf.getChannel();

        // 写入该分片数据
        long offset = CHUNK_SIZE * param.getChunk();
        byte[] fileData = param.getFile().getBytes();
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, offset, fileData.length);
        mappedByteBuffer.put(fileData);
        // 释放
        FileMD5Util.freedMappedByteBuffer(mappedByteBuffer);
        fileChannel.close();

        boolean isOk = checkAndSetUploadProgress(param, uploadDirPath);
        if (isOk) {
            boolean flag = renameFile(tmpFile, fileName);
            System.out.println("upload complete !!" + flag + " name=" + fileName);
        }
    }

    /**
     * 检查并修改文件上传进度
     *
     * @param param
     * @param uploadDirPath
     * @return
     * @throws IOException
     */
    private boolean checkAndSetUploadProgress(MultipartFileParam param, String uploadDirPath) throws IOException {
        String fileName = param.getName();
        File confFile = new File(uploadDirPath, fileName + ".conf");
        RandomAccessFile accessConfFile = new RandomAccessFile(confFile, "rw");
        // 把该分段标记为 true 表示完成
        System.out.println("set part " + param.getChunk() + " complete");
        accessConfFile.setLength(param.getChunks());
        accessConfFile.seek(param.getChunk());
        accessConfFile.write(Byte.MAX_VALUE);

        // completeList 检查是否全部完成,如果数组里是否全部都是(全部分片都成功上传)
        byte[] completeList = FileUtils.readFileToByteArray(confFile);
        byte isComplete = Byte.MAX_VALUE;
        for (int i = 0; i < completeList.length && isComplete == Byte.MAX_VALUE; i++) {
            // 与运算, 如果有部分没有完成则 isComplete 不是 Byte.MAX_VALUE
            isComplete = (byte) (isComplete & completeList[i]);
            System.out.println("check part " + i + " complete?:" + completeList[i]);
        }

        accessConfFile.close();
        if (isComplete == Byte.MAX_VALUE) {
            FileUploadStatus status = new FileUploadStatus();
            status.setFileMd5(param.getMd5());
            status.setUploadStatus("true");
            fileUploadStatusMapper.insert(status);
            FileMd5 fileMd5 = new FileMd5();
            fileMd5.setFileMd5(Constants.FILE_MD5_KEY + param.getMd5());
            fileMd5.setFilePath(uploadDirPath + "/" + fileName);
            fileMd5Mapper.insert(fileMd5);
            return true;
        } else {
            if (fileUploadStatusMapper.selectFileMd5(param.getMd5()) <= 0) {
                FileUploadStatus status = new FileUploadStatus();
                status.setFileMd5(param.getMd5());
                status.setUploadStatus("false");
                fileUploadStatusMapper.insert(status);
            }

            if (fileMd5Mapper.selectFileMd5(param.getMd5()) >= 1) {
                FileMd5 fileMd5 = new FileMd5();
                fileMd5.setFileMd5(Constants.FILE_MD5_KEY + param.getMd5());
                fileMd5.setFilePath(uploadDirPath + "/" + fileName + ".conf");
                fileMd5Mapper.insert(fileMd5);
            }
            return false;
        }
    }

    /**
     * 文件重命名
     *
     * @param toBeRenamed   将要修改名字的文件
     * @param toFileNewName 新的名字
     * @return
     */
    public boolean renameFile(File toBeRenamed, String toFileNewName) {
        // 检查要重命名的文件是否存在，是否是文件
        if (!toBeRenamed.exists() || toBeRenamed.isDirectory()) {
            logger.info("File does not exist: " + toBeRenamed.getName());
            return false;
        }
        String p = toBeRenamed.getParent();
        File newFile = new File(p + File.separatorChar + toFileNewName);
        // 修改文件名
        return toBeRenamed.renameTo(newFile);
    }

}
