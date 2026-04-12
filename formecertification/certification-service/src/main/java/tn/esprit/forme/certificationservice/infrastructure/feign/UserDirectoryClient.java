package tn.esprit.forme.certificationservice.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import tn.esprit.forme.certificationservice.infrastructure.feign.config.FeignAuthForwardingConfig;
import tn.esprit.forme.certificationservice.infrastructure.feign.dto.UserDto;

import java.util.List;

@FeignClient(
        name = "user-service",
        contextId = "userDirectoryClient",
        configuration = FeignAuthForwardingConfig.class
)
public interface UserDirectoryClient {

    @GetMapping("/admin/list")
    List<UserDto> listAdmins();

    @GetMapping("/super-admin/list")
    List<UserDto> listSuperAdmins();
}
