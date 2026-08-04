package cn.qiany.basic.module.search.common;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AbstractGeneralSearchRequest implements Serializable {
    /**
     * 用户标识
     * <p>
     * 优先手机号码，其次安卓或苹果客户端标识imei、idfa
     */
    protected String user_id = "";
    /**
     * 请求场景，同channel，对接engine改为app_id
     */
    protected String app_id;
    /**
     * 是否子频道id，1-是，0-否
     */
    protected Integer is_sub = 0;

    /**
     *  搜索内容
     */
    protected String text;

    /**
     * 搜索流水号
     */
    protected String traceSeq;

    protected Integer pageSize;

    protected Integer pageNo;

    protected Integer is_correct = 1;

    /**
     * 附加参数(通过接口参数计算/查询等方式得到的信息)
     */
    @JSONField(serialize = false, deserialize = false)
    protected RequestExtra extra = new RequestExtra();

    /**
     * 是否返回推荐内容详情 名称、简介、图片地址、播放地址等
     */
    protected Short show_details = 1;

    /**
     * 获取 策略app_id
     *
     * @return 策略频道id
     * @since v1.33.0 策略ID自定义
     */
    public String generateStrategyAppId() {
        return app_id;
    }

    public int getPageNo() {
        return pageNo == null ? 1 : pageNo;
    }
    public int getPageSize() {
        return pageSize == null ? 20 : pageSize;
    }



}
