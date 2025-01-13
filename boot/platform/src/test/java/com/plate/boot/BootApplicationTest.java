package com.plate.boot;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
class BootApplicationTest {

    @Test
    void contextLoads() throws MalformedURLException {
        URI uri = UriComponentsBuilder.fromUriString("https://tcszxsj.oss-cn-chengdu.aliyuncs.com/" +
                "template/social/610222000000000001/cppcc/socialPaper.docx").build().toUri();
        Resource resource = new UrlResource(uri);
        String filename = resource.getFilename();
        System.out.println(filename);
        URI parent = uri.resolve("..");
        System.out.println(parent);
        URI fileUri = UriComponentsBuilder.fromUri(parent).path(filename).build().toUri();
        System.out.println(fileUri);
    }
}