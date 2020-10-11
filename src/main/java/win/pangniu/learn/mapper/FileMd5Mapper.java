package win.pangniu.learn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import win.pangniu.learn.pojo.FileMd5;

/**
 * @Author renjiaxin
 * @Date 2020/10/12
 * @Description
 */
@Repository
public interface FileMd5Mapper extends BaseMapper<FileMd5> {
    int selectFileMd5(String md5);

    String selectFilePath(String md5);
}
