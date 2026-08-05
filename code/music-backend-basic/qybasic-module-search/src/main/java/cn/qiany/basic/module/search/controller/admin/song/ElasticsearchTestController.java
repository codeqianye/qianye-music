package cn.qiany.basic.module.search.controller.admin.song;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping("/test/es")
public class ElasticsearchTestController {

    @Resource
    private RestHighLevelClient client;

    @GetMapping("/ping")
    public boolean ping() throws IOException {
        return client.ping(RequestOptions.DEFAULT);
    }
}