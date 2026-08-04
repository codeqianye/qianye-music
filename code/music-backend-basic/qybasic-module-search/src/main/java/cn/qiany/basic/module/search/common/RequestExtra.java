package cn.qiany.basic.module.search.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class RequestExtra implements Serializable {
    /**
     * 来自 搜索过程产生的数据
     */
    private String flowId; // 推荐流程id

}
