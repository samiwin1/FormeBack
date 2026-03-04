package tn.esprit.forme.certificationservice.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.esprit.forme.certificationservice.infrastructure.feign.config.FeignAuthForwardingConfig;
import tn.esprit.forme.certificationservice.infrastructure.feign.dto.UserDto;
import tn.esprit.forme.certificationservice.infrastructure.feign.dto.UserMeDto;

@FeignClient(name = "user-service", path = "/users", configuration = FeignAuthForwardingConfig.class)
public interface UserClient {

    @GetMapping("/me")
    UserMeDto getMe();

    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}
