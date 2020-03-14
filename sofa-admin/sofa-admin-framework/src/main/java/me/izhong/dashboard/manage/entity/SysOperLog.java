package me.izhong.dashboard.manage.entity;

import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.izhong.common.annotation.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_op_log")
@Data
public class SysOperLog extends TimedBasedEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 日志主键
     */
    @PrimaryId
    @Excel(name = "操作序号", cellType = Excel.ColumnType.NUMERIC)
    @Search
    @AutoId
    @Indexed(unique = true)
    private Long operId;

    /**
     * 操作模块
     */
    @Excel(name = "操作模块")
    @Search
    private String title;

    /**
     * 业务类型
     */
    @Excel(name = "业务类型", readConverterExp = "0=其它,1=新增,2=修改,3=删除,4=授权,5=导出,6=导入,7=强退,8=生成代码,9=清空数据")
    @NotNull
    @Search
    private Integer businessType;

    /**
     * 业务类型数组
     */
    @Search(columnName ="businessType", op = Search.Op.IN)
    private Integer[] businessTypes;

    /**
     * 请求方法
     */
    @Excel(name = "请求方法")
    private String method;

    /** 请求方式 */
    @Excel(name = "请求方式")
    private String requestMethod;

    /**
     * 操作人类别
     */
    @Excel(name = "操作类别", readConverterExp = "0=其它,1=后台用户,2=手机端用户")
    @Search
    private Integer operatorType;

    /**
     * 操作人员
     */
    @Excel(name = "操作人员")
    @Search
    private String operName;

    /**
     * 部门名称
     */
    @Excel(name = "部门名称")
    @Search
    private String deptName;

    /**
     * 请求url
     */
    @Excel(name = "请求地址")
    private String operUrl;

    /**
     * 操作地址
     */
    @Excel(name = "操作地址")
    private String operIp;

    /**
     * 操作地点
     */
    @Excel(name = "操作地点")
    private String operLocation;

    /**
     * 请求参数
     */
    @Excel(name = "请求参数")
    private String operParam;

    /** 返回参数 */
    @Excel(name = "返回参数")
    private String jsonResult;

    /**
     * 状态0正常 1异常
     */
    @Search
    @Excel(name = "状态", readConverterExp = "0=正常,1=异常")
    private String status;

    /**
     * 错误消息
     */
    @Excel(name = "错误消息")
    private String errorMsg;

    /**
     * 操作时间
     */
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @CreateTimeAdvise
    private Date operTime;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("operId", getOperId())
                .append("title", getTitle())
                .append("businessType", getBusinessType())
                .append("businessTypes", getBusinessTypes())
                .append("method", getMethod())
                .append("operatorType", getOperatorType())
                .append("operName", getOperName())
                .append("deptName", getDeptName())
                .append("operUrl", getOperUrl())
                .append("operIp", getOperIp())
                .append("operLocation", getOperLocation())
                .append("operParam", getOperParam())
                .append("status", getStatus())
                .append("errorMsg", getErrorMsg())
                .append("operTime", getOperTime())
                .toString();
    }
}
