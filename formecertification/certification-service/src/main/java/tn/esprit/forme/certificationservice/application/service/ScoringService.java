package tn.esprit.forme.certificationservice.application.service;

import org.springframework.stereotype.Service;

@Service

public class ScoringService {

    public double computeFinalScore(double writtenScore, double oralScore, double weightWritten, double weightOral) {
        return writtenScore * weightWritten + oralScore * weightOral;
    }
}
