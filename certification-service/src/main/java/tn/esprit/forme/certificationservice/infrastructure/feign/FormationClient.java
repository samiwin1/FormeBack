package tn.esprit.forme.certificationservice.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import tn.esprit.forme.certificationservice.infrastructure.feign.dto.ExamDto;
import tn.esprit.forme.certificationservice.infrastructure.feign.dto.ExamHistoryItemDto;
import tn.esprit.forme.certificationservice.infrastructure.feign.dto.FormationDto;
import tn.esprit.forme.certificationservice.infrastructure.feign.dto.ResultExamenDto;

import java.util.List;

@FeignClient(name = "formation-service", dismiss404 = true)
public interface FormationClient {

    @GetMapping("/api/formations/{id}")
    FormationDto getFormationById(@PathVariable("id") Long id);

    @GetMapping("/api/examens/history")
    List<ExamHistoryItemDto> getExamHistory(@RequestParam("userId") Long userId,
                                            @RequestParam("formationId") Long formationId);

    @GetMapping("/api/examens/formation/{formationId}")
    ExamDto getExamByFormationId(@PathVariable("formationId") Long formationId);

    @GetMapping("/api/result-examens/examen/{examenId}")
    List<ResultExamenDto> getResultExamensByExamenId(@PathVariable("examenId") Long examenId);
}
