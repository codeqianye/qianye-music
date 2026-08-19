package cn.qiany.basic.module.search.flow.processor;

import cn.qiany.basic.abtest.exception.AbFlowException;
import cn.qiany.basic.abtest.processor.AbstractGeneralProcessor;
import cn.qiany.basic.module.search.flow.context.GeneralRecFlowContext;
import cn.qiany.basic.module.search.flow.model.SongProcessorResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 执行第三期最小文本标准化规则。
 */
@Component
public class GeneraTextChainProcessor extends AbstractGeneralProcessor<
        GeneralRecFlowContext, SongProcessorResult> {

    /**
     * 只接受第三期约定的 trim 链，输出供后续召回节点使用的关键词。
     */
    @Override
    public SongProcessorResult processWithCustomParams(
            GeneralRecFlowContext context,
            Map<String, Object> params) {
        Object chain = params.get("chain");
        // 第三期不做动态 DSL 解释，避免配置可以执行未实现的文本算子。
        if (!(chain instanceof List)
                || !((List<?>) chain).contains("cleanText:root:trim")) {
            throw new AbFlowException(
                    "第三期TextRule仅支持cleanText:root:trim");
        }
        String keyword = StringUtils.trim(context.getRequest().getText());
        if (StringUtils.isBlank(keyword)) {
            throw new AbFlowException("搜索关键词不能为空");
        }
        return SongProcessorResult.builder()
                .normalizedKeyword(keyword)
                .build();
    }
}