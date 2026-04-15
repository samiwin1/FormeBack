package tn.esprit.events_service.service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.events_service.dto.DepositResponse;
import tn.esprit.events_service.dto.ViewerRole;

import java.util.List;
import java.util.Optional;

public interface EventDepositService {

    /** Current user's submission for this event, empty if none. */
    Optional<DepositResponse> getMyDeposit(Long eventId, Long userId);

    DepositResponse submitDeposit(Long eventId, Long userId, MultipartFile zipFile, MultipartFile readmeFile);

    void scoreDeposit(Long eventId, Long depositId, int score, ViewerRole role);

    /** ZIP binary for admin or event sponsor download. */
    byte[] getZipForDownload(Long eventId, Long depositId, ViewerRole role, Long userId);

    List<DepositResponse> listDepositsForAdmin(Long eventId, ViewerRole role);
}
