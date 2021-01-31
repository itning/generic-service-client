package top.itning.generic.service.jar.handle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileCopyUtils;
import top.itning.generic.service.common.jar.MethodInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author itning
 * @since 2020/12/31 16:02
 */
class JarHandlerTest {

    @Test
    void handle() throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(new File("C:\\ProjectData\\CompanyProjects\\generic-service-client\\test-jar\\target\\test-classes\\top\\itning\\test\\jar\\handle\\Test.class"));
        MockMultipartFile mockMultipartFile = new MockMultipartFile("a.zip", "a.zip", MediaType.ALL_VALUE, bytes);
        List<MethodInfo> handle = new JarHandler().handle(mockMultipartFile, "top.itning.test.jar.handle.Test", "test");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(handle));
    }
}