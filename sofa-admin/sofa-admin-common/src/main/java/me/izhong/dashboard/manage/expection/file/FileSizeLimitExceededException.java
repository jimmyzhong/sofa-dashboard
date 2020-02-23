package me.izhong.dashboard.manage.expection.file;

import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.web.bean.ResponseContainer;

public class FileSizeLimitExceededException extends BusinessException {

    public FileSizeLimitExceededException(String message) {
        super(ResponseContainer.FAIL_CODE, message);
    }

    public FileSizeLimitExceededException(long size) {
        super(ResponseContainer.FAIL_CODE, "文件大小不能超过" + size);
    }

}
