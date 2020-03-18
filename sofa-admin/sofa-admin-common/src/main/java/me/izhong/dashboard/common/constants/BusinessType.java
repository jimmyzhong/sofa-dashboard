package me.izhong.dashboard.common.constants;

import lombok.Getter;

public enum BusinessType {


    /**
     * 其它
     */
    OTHER(0,"其他"),

    /**
     * 新增
     */
    ADD(1,"新增"),

    /**
     * 修改
     */
    UPDATE(2,"修改"),

    /**
     * 删除
     */
    DELETE(3,"删除"),

    /**
     * 授权
     */
    GRANT(4,"授权"),

    /**
     * 导出
     */
    EXPORT(5,"导出"),

    /**
     * 导入
     */
    IMPORT(6,"导入"),

    /**
     * 强退
     */
    FORCE(7,"强退"),

    /**
     * 生成代码
     */
    GENCODE(8,"生成代码"),

    /**
     * 清空
     */
    CLEAN(9,"清空"),

    /**
     * 执行
     */
    OPERATE(10,"执行");

    @Getter
    private Integer index;

    @Getter
    private String title;

    BusinessType(Integer index, String title){
        this.index = index;
        this.title = title;
    }
}
