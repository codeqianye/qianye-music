package cn.qiany.basic.module.search.flow.config;

import cn.qiany.basic.abtest.config.AbTestExecutorProperties;
import cn.qiany.basic.abtest.delegate.RecommendJobLaunchDelegate;
import cn.qiany.basic.abtest.executor.AsyncTaskManager;
import cn.qiany.basic.abtest.executor.CompositeItemProcessor;
import cn.qiany.basic.abtest.mapping.FuncMappingPropertiesUtils;
import cn.qiany.basic.abtest.model.RecommendResult;
import cn.qiany.basic.abtest.parser.JsonReaderContext;
import cn.qiany.basic.abtest.parser.RecommendJobFlowParser;
import cn.qiany.basic.abtest.registry.TaskProcessorCache;
import cn.qiany.basic.abtest.source.JobFlowSource;
import cn.qiany.basic.module.search.common.AbstractGeneralSearchRequest;
import cn.qiany.basic.module.search.controller.admin.song.vo.es.SongSearchItem;
import cn.qiany.basic.module.search.flow.context.GeneralRecFlowContext;
import cn.qiany.basic.module.search.flow.model.SongProcessorResult;
import cn.qiany.basic.module.search.flow.reader.GeneralRecFlowReader;
import cn.qiany.basic.module.search.flow.writer.RecommendDefaultWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 组装单曲业务使用的完整 ABTest 泛型执行链。
 */
@Configuration(proxyBeanMethods = false)
public class SongAbTestConfiguration {

    @Bean
    public FuncMappingPropertiesUtils funcMappingPropertiesUtils() {
        return new FuncMappingPropertiesUtils();
    }

    @Bean
    public JsonReaderContext jsonReaderContext() {
        return new JsonReaderContext();
    }

    @Bean
    public TaskProcessorCache<GeneralRecFlowContext, SongProcessorResult>
            songTaskProcessorCache(
            FuncMappingPropertiesUtils mappingProperties,
            ApplicationContext applicationContext) {
        return new TaskProcessorCache<>(
                mappingProperties, applicationContext);
    }

    @Bean
    public RecommendJobFlowParser<GeneralRecFlowContext, SongProcessorResult>
            songJobFlowParser(
            TaskProcessorCache<GeneralRecFlowContext, SongProcessorResult> cache,
            AbTestExecutorProperties properties) {
        return new RecommendJobFlowParser<>(
                cache, properties.getStageTimeoutMs());
    }

    @Bean
    public AsyncTaskManager<GeneralRecFlowContext, SongProcessorResult>
            songAsyncTaskManager(
            @Qualifier("abTestExecutor")
            ThreadPoolTaskExecutor executor) {
        return new AsyncTaskManager<>(executor);
    }

    @Bean
    public CompositeItemProcessor<GeneralRecFlowContext, SongProcessorResult>
            songCompositeItemProcessor(
            AsyncTaskManager<GeneralRecFlowContext, SongProcessorResult> manager,
            AbTestExecutorProperties properties) {
        return new CompositeItemProcessor<>(
                manager, properties.getStageTimeoutMs());
    }

    @Bean
    public RecommendJobLaunchDelegate<
            AbstractGeneralSearchRequest,
            GeneralRecFlowContext,
            SongProcessorResult,
            RecommendResult<SongSearchItem, Void>> songJobLaunchDelegate(
            JobFlowSource flowSource,
            RecommendJobFlowParser<GeneralRecFlowContext, SongProcessorResult> parser,
            GeneralRecFlowReader reader,
            CompositeItemProcessor<GeneralRecFlowContext, SongProcessorResult> processor,
            RecommendDefaultWriter writer,
            SongSearchFlowProperties flowProperties) {
        // 启动阶段解析默认场景，配置错误直接阻止应用启动
        parser.parseJobFlow(
                flowProperties.getDefaultAppId(),
                flowSource.getRequired(flowProperties.getDefaultAppId()));
        return new RecommendJobLaunchDelegate<>(
                flowSource, parser, reader, processor, writer);
    }
}