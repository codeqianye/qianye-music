package cn.qiany.basic.abtest.executor;

import cn.qiany.basic.abtest.context.RecommendJobFlowContext;
import cn.qiany.basic.abtest.exception.AbFlowException;
import cn.qiany.basic.abtest.model.TaskDefinition;
import cn.qiany.basic.abtest.processor.AbstractGeneralProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 通过可观察的事件顺序验证阶段语义，不依赖 Spring 容器。
 */
class CompositeItemProcessorTest {

    private final ThreadPoolTaskExecutor executor = createExecutor();

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    void shouldRunSameOrderBeforeNextOrder() {
        List<String> events = new CopyOnWriteArrayList<>();
        CompositeItemProcessor<TestContext, String> processor = processor();
        TestContext context = new TestContext();

        processor.processWithCustomParams(context, Arrays.asList(
                task("a", 10, false, events),
                task("b", 10, false, events),
                task("c", 20, false, events)));

        // a、b 的先后不稳定；但 c 必须在它们都结束后才开始。
        assertEquals(3, events.size());
        assertEquals("c", events.get(2));
        assertEquals(Arrays.asList("a", "b", "c"), context.merged);
    }

    @Test
    void shouldStopWhenRequiredNodeFails() {
        List<String> events = new CopyOnWriteArrayList<>();
        CompositeItemProcessor<TestContext, String> processor = processor();

        assertThrows(AbFlowException.class, () -> processor.processWithCustomParams(
                new TestContext(), Arrays.asList(
                        failingTask("required-fail", 10, events),
                        task("next-stage", 20, false, events))));

        assertEquals(Collections.singletonList("required-fail"), events);
    }

    private CompositeItemProcessor<TestContext, String> processor() {
        return new CompositeItemProcessor<>(
                new AsyncTaskManager<>(executor), 1_000L);
    }

    private ThreadPoolTaskExecutor createExecutor() {
        ThreadPoolTaskExecutor value = new ThreadPoolTaskExecutor();
        value.setCorePoolSize(2);
        value.setMaxPoolSize(2);
        value.setQueueCapacity(10);
        value.initialize();
        return value;
    }

    private TaskDefinition<TestContext, String> task(
            String name, int order, boolean required, List<String> events) {
        return TaskDefinition.<TestContext, String>builder()
                .scene("test").type(name).fmap(name).execOrder(order).toggle(true)
                .required(required).timeoutMs(500L)
                .params(Collections.emptyMap())
                .processor(new AbstractGeneralProcessor<TestContext, String>() {
                    @Override
                    public String processWithCustomParams(TestContext context, Map<String, Object> params) {
                        events.add(name);
                        return name;
                    }
                }).build();
    }

    private TaskDefinition<TestContext, String> failingTask(
            String name, int order, List<String> events) {
        return TaskDefinition.<TestContext, String>builder()
                .scene("test").type(name).fmap(name).execOrder(order).toggle(true)
                .required(true).timeoutMs(500L)
                .params(Collections.emptyMap())
                .processor(new AbstractGeneralProcessor<TestContext, String>() {
                    @Override
                    public String processWithCustomParams(TestContext context, Map<String, Object> params) {
                        events.add(name);
                        throw new IllegalStateException("模拟必选节点失败");
                    }
                }).build();
    }

    private static class TestContext extends RecommendJobFlowContext<String> {
        private final List<String> merged = new CopyOnWriteArrayList<>();

        private TestContext() {
            super("test");
        }

        @Override
        public void mergeSuccess(String result) {
            merged.add(result);
        }
    }
}