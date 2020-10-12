package win.pangniu.learn.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * @Author renjiaxin
 * @Date 2020/10/12
 * @Description
 */
public class FileMd5 {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String fileMd5;
    private String filePath;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public FileMd5() {
    }

    public FileMd5(Integer id, String fileMd5, String filePath) {
        this.id = id;
        this.fileMd5 = fileMd5;
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "FileMd5{" +
                "id=" + id +
                ", fileMd5='" + fileMd5 + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
