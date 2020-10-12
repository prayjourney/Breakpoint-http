package win.pangniu.learn.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * @Author renjiaxin
 * @Date 2020/10/12
 * @Description
 */
public class FileUploadStatus {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String fileMd5;
    private String uploadStatus;

    public FileUploadStatus() {
    }

    public FileUploadStatus(Integer id, String fileMd5, String uploadStatus) {
        this.id = id;
        this.fileMd5 = fileMd5;
        this.uploadStatus = uploadStatus;
    }

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

    public String getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    @Override
    public String toString() {
        return "FileUploadStatus{" +
                "id=" + id +
                ", fileMd5='" + fileMd5 + '\'' +
                ", uploadStatus='" + uploadStatus + '\'' +
                '}';
    }
}
