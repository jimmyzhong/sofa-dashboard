package me.izhong.dashboard.manage.expection.file;

import me.izhong.common.constant.ErrCode;
import me.izhong.common.exception.BusinessException;

public class FileNameLengthLimitExceededException extends BusinessException {

    public FileNameLengthLimitExceededException(String message) {
        super(ErrCode.FAIL_CODE, message);
    }

    public FileNameLengthLimitExceededException(int size) {
        super(ErrCode.FAIL_CODE, "文件长度不能超过" + size);
    }

}
