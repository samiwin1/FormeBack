package tn.esprit.events_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.events_service.dto.DepositResponse;
import tn.esprit.events_service.dto.ViewerRole;
import tn.esprit.events_service.service.EventDepositService;
import tn.esprit.events_service.util.RequestHeaderUtil;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventDepositController {

    private final EventDepositService eventDepositService;

    @PostMapping(value = "/{eventId}/deposits", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DepositResponse submit(
            @PathVariable Long eventId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestPart("zipFile") MultipartFile zipFile,
            @RequestPart("readmeFile") MultipartFile readmeFile
    ) {
        return eventDepositService.submitDeposit(eventId, userId, zipFile, readmeFile);
    }

    /** Participant: current submission if any (404 when not joined or no submission yet). */
    @GetMapping("/{eventId}/deposits/me")
    public ResponseEntity<DepositResponse> myDeposit(
            @PathVariable Long eventId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return eventDepositService.getMyDeposit(eventId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{eventId}/deposits/{depositId}/score")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void score(
            @PathVariable Long eventId,
            @PathVariable Long depositId,
            @RequestParam int score,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader
    ) {
        ViewerRole role = RequestHeaderUtil.parseViewerRole(roleHeader);
        eventDepositService.scoreDeposit(eventId, depositId, score, role);
    }

    @GetMapping("/{eventId}/deposits")
    public List<DepositResponse> listForAdmin(
            @PathVariable Long eventId,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader
    ) {
        ViewerRole role = RequestHeaderUtil.parseViewerRole(roleHeader);
        return eventDepositService.listDepositsForAdmin(eventId, role);
    }

    @GetMapping("/{eventId}/deposits/{depositId}/zip")
    public ResponseEntity<byte[]> downloadZip(
            @PathVariable Long eventId,
            @PathVariable Long depositId,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        ViewerRole role = RequestHeaderUtil.parseViewerRole(roleHeader);
        byte[] data = eventDepositService.getZipForDownload(eventId, depositId, role, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "submission-" + depositId + ".zip");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
