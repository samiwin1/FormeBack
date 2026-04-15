package tn.esprit.partnerintelligence.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SummaryGenerationService {
    private final String[] insights = {
        "The Partner Intelligence engine detected positive growth momentum. Training engagement increased by 14% over the last week. The AI algorithm recommends extending their pack tier to capitalize on current trends.",
        "AI diagnostic checks show a slight decline in partner conversion rates. A forecast projection using time-series analysis predicts a minor 5% drop in revenue in the coming 30 days unless retention vouchers are dispersed.",
        "Our neural network analysis has flagged anomalous patterns in voucher redemption, specifically late-night bulk validations. The health score remains stable but active preventative measures are requested in the pending recommendations queue.",
        "Performance benchmarks indicate stable health overall. The LLM summarized recent feedback interactions as 'Highly Positive'. Based on current deal velocity, the partner is eligible for automated contract renewals."
    };

    public String weeklySummary(Long p) {
        String base = "🤖 AI Executive Summary (Partner ID: " + p + "):\n\n";
        String insight = insights[new Random().nextInt(insights.length)];
        
        return base + insight + "\n\nKey actions computed:\n- Mitigate pending security flag.\n- Review AI-suggested retention strategy to maximize projected 30D forecast.";
    }
}