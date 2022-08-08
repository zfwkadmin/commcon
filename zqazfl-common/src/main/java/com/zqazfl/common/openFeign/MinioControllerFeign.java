package com.zqazfl.common.openFeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
@FeignClient("fj-service")
public interface MinioControllerFeign {
    @PostMapping("/fj/minio/updateType")
    String updateType(@RequestParam Map<String, Object> map);
    @PostMapping("/fj/minio/delete")
    String delete(@RequestParam Map<String, Object> map);

}
