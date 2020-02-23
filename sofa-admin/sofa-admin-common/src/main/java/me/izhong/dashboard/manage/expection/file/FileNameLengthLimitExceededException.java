package me.izhong.dashboard.manage.expection.file;

import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.web.bean.ResponseContainer;

public class FileNameLengthLimitExceededException extends BusinessException {

    public FileNameLengthLimitExceededException(String message) {
        super(ResponseContainer.FAIL_CODE, message);
    }

    public FileNameLengthLimitExceededException(int size) {
        super(ResponseContainer.FAIL_CODE, "文件长度不能超过" + size);
    }

}
