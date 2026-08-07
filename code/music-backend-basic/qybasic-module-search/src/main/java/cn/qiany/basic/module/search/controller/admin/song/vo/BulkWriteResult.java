package cn.qiany.basic.module.search.controller.admin.song.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BulkWriteResult {
    private int successCount;
    private int failureCount;
    private List<String> failureIds = new ArrayList<>();
    private String failureMessage;

    public boolean hasFailure() {
        return failureCount > 0;
    }

    public static BulkWriteResult empty() {
        return new BulkWriteResult();
    }
}