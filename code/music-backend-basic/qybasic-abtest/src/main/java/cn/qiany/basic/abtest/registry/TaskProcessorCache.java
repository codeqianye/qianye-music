package cn.qiany.basic.abtest.registry;

import cn.qiany.basic.abtest.context.RecommendJobFlowContext;
import cn.qiany.basic.abtest.exception.AbFlowException;
import cn.qiany.basic.abtest.mapping.FuncMappingPropertiesUtils;
import cn.qiany.basic.abtest.processor.AbstractGeneralProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 根据 fmap 映射文件建立 Processor Bean 只读缓存。
 *
 * <p>该类保留现网“fmap → 类名 → Processor”的查找过程，但不会通过
 * newInstance 创建对象。类名只用于定位类型，最终实例必须从 Spring 容器取得，
 * 因此 Processor 可以继续使用构造器注入。</p>
 */
public class TaskProcessorCache<
        C extends RecommendJobFlowContext<R>, R> {

    // 启动时完成全部映射，运行期只读
    private final Map<String, AbstractGeneralProcessor<C, R>> processors;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public TaskProcessorCache(
            FuncMappingPropertiesUtils mappingProperties,
            ApplicationContext applicationContext) {
        Map<String, AbstractGeneralProcessor<C, R>> registry =
                new LinkedHashMap<>();
        for (String fmap : mappingProperties.names()) {
            String className = mappingProperties.getRequired(fmap);
            try {
                // 只根据类名定位类型，不通过反射创建实例
                Class<?> rawClass = ClassUtils.forName(
                        className, applicationContext.getClassLoader());
                if (!AbstractGeneralProcessor.class.isAssignableFrom(rawClass)) {
                    throw new AbFlowException(
                            "映射类不是Processor: " + className);
                }
                Class<? extends AbstractGeneralProcessor> processorClass =
                        rawClass.asSubclass(AbstractGeneralProcessor.class);
                AbstractGeneralProcessor<C, R> processor =
                        (AbstractGeneralProcessor<C, R>)
                                applicationContext.getBean(processorClass);
                registry.put(fmap, processor);
            } catch (ClassNotFoundException ex) {
                throw new AbFlowException(
                        "Processor类不存在: " + className, ex);
            } catch (BeansException ex) {
                throw new AbFlowException(
                        "Processor Bean不存在或不唯一: " + className, ex);
            }
        }
        this.processors = Collections.unmodifiableMap(registry);
    }

    /**
     * 获取 fmap 对应的 Processor。
     *
     * <p>RecommendJobFlowParser 解析节点时调用该方法。这里直接使用完整 fmap
     * 作为 Map key，不截取 msc_10 的数字后缀。</p>
     *
     * @param fmap 节点配置中的 Processor 映射标识
     * @return Spring 管理的无状态 Processor Bean
     */
    public AbstractGeneralProcessor<C, R> getData(String fmap) {
        AbstractGeneralProcessor<C, R> processor =
                processors.get(StringUtils.trim(fmap));
        if (processor == null) {
            throw new AbFlowException("Processor未注册: " + fmap);
        }
        return processor;
    }
}