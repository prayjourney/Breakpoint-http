package win.pangniu.learn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import win.pangniu.learn.pojo.FileUploadStatus;

/**
 * @Author renjiaxin
 * @Date 2020/10/12
 * @Description
 */
@Repository
public interface FileUploadStatusMapper extends BaseMapper<FileUploadStatus> {
    int selectFileMd5(String md5);

    FileUploadStatus selectFileMd5Obj(String md5);
}
