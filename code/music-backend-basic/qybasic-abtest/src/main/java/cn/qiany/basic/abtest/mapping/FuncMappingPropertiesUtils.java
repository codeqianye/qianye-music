package cn.qiany.basic.abtest.mapping;

import cn.qiany.basic.abtest.exception.AbFlowException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * 读取 fmap 与 Processor 类名映射。
 *
 * <p>职责与现网同名工具一致，默认读取 classpath 下的
 * ab_funcmapping/rec_funcmaps.properties。该类只解析映射关系，
 * 不加载类，也不创建 Processor 对象。</p>
 */
public class FuncMappingPropertiesUtils {

    // 保留现网资源目录和文件名，便于直接对照映射项
    public static final String DEFAULT_MAPPING_RESOURCE =
            "ab_funcmapping/rec_funcmaps.properties";

    // 构造完成后不再修改，只允许通过查询方法读取
    private final Properties mappings;

    public FuncMappingPropertiesUtils() {
        this(DEFAULT_MAPPING_RESOURCE);
    }

    public FuncMappingPropertiesUtils(String resourcePath) {
        if (StringUtils.isBlank(resourcePath)) {
            throw new AbFlowException("Processor映射文件路径不能为空");
        }
        ClassPathResource resource = new ClassPathResource(resourcePath);
        Properties loaded = new Properties();
        try (InputStream inputStream = resource.getInputStream();
             InputStreamReader reader = new InputStreamReader(
                     inputStream, StandardCharsets.UTF_8)) {
            loaded.load(reader);
        } catch (IOException ex) {
            throw new AbFlowException(
                    "Processor映射文件读取失败: " + resourcePath, ex);
        }
        if (loaded.isEmpty()) {
            throw new AbFlowException(
                    "Processor映射文件不能为空: " + resourcePath);
        }
        this.mappings = loaded;
    }

    /**
     * 获取 fmap 对应的 Processor 类名。
     *
     * @param fmap 节点配置中的映射标识
     * @return Processor 全限定类名
     */
    public String getRequired(String fmap) {
        String className = StringUtils.trim(
                mappings.getProperty(StringUtils.trim(fmap)));
        if (StringUtils.isBlank(className)) {
            throw new AbFlowException("Processor映射不存在: " + fmap);
        }
        return className;
    }

    /**
     * 获取全部 fmap，用于启动时建立 Processor 缓存。
     *
     * @return 按名称排序的只读 fmap 集合
     */
    public Set<String> names() {
        return Collections.unmodifiableSet(
                new TreeSet<>(mappings.stringPropertyNames()));
    }
}