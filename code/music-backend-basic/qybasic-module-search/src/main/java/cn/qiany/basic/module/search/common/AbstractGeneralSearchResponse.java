package cn.qiany.basic.module.search.common;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AbstractGeneralSearchResponse implements Serializable {


    /**
     * 搜索流程记录
     */
    protected String fl;
    /**
     * 是否有搜索数据，1-有，0-无
     */
    protected Integer f;
    /**
     * 搜索时间
     */
    protected String u;
    /**
     * 搜索流水号
     */
    protected String seq;
    /**
     * 搜索数据
     */
    protected Object c;
    /**
     * 搜索总数
     */
    protected Integer total;

    /**
     * 是否搜索数量不足，0-本次搜索数量满足count，1-本次搜索数量不足
     */
    protected Integer nodata = 0;

    protected ResponseExtra extra;


    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ResponseExtra {
        private Map<String, Object> correct;
        private String info;
        private String instanceId;

    }

    /**
     * 创建响应结果
     *
     * @param request 接口参数及流程附加参数
     * @param c       搜索结果
     */
    public void build(AbstractGeneralSearchRequest request, Object c) {
        if (null == c) {
            c = Collections.emptyList();
        }
        int f = 1;
        if (c instanceof List) {
            f = CollectionUtils.isEmpty((List) c) ? 0 : 1;
        }
        //分页
        c = page((List) c,request.getPageNo(),request.getPageSize());

        String flowId = request.getExtra().getFlowId();
        String traceSeq = request.getTraceSeq();

        setF(f);
        setSeq(StringUtils.isBlank(flowId) ? traceSeq : traceSeq + "@" + flowId);
        setU(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        setC(c);

        if (c instanceof List && ((List<?>) c).size() < request.getPageSize()) {
            setNodata(1);
        }

        buildExtraResponse(request);
    }

    /**
     * 构建 ES 已分页响应，不再执行内存分页。
     * @param request 搜索参数
     * @param rows ES 当前页数据
     * @param total ES 总命中数
     */
    public void buildPaged(AbstractGeneralSearchRequest request,
                           List<?> rows,
                           long total) {
        List<?> safeRows = rows == null ? Collections.emptyList() : rows;
        if (total > Integer.MAX_VALUE) {
            throw new IllegalStateException("搜索结果总数超过响应字段上限: " + total);
        }

        this.setC(safeRows);
        this.setTotal((int) total);
        this.setF(safeRows.isEmpty() ? 0 : 1);

        // 使用 long 计算当前页结束位置，避免整数溢出
        long currentEnd = (long) request.getPageNo() * request.getPageSize();
        this.setNodata(currentEnd >= total ? 1 : 0);

        String flowId = request.getExtra().getFlowId();
        String traceSeq = request.getTraceSeq();
        this.setSeq(StringUtils.isBlank(flowId) ? traceSeq : traceSeq + "@" + flowId);
        this.setU(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        buildExtraResponse(request);
    }

    /**
     * 设置其他响应结果
     *
     * @param request 接口参数
     */
    public void buildExtraResponse(AbstractGeneralSearchRequest request) {

    }

    /**
     * 手动分页
     * @param list
     * @param pageNo
     * @param pageSize
     * @return
     * @param <T>
     */
    public static <T> List<T> page(List<T> list, int pageNo, int pageSize) {
        if (list == null || list.isEmpty() || pageNo <= 0 || pageSize <= 0) {
            return Collections.emptyList();
        }
        int fromIndex = (pageNo - 1) * pageSize;
        if (fromIndex >= list.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + pageSize, list.size());
        return list.subList(fromIndex, toIndex);
    }




}
