package com.ebitware.chatbotpayments.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlansDTO {
    private Long id;
    private String planName;
    private double setupFee;

    // Monthly pricing
    private double monthlyTotalFee;
    private double monthlyFee;
    private double monthlySetupPerAgent;
    private double monthlyExtraAgentFee;

    // Annual pricing
    private double annualTotalFee;
    private double annualFee;
    private double annualSetupPerAgent;
    private double annualExtraAgentFee;

    // Common fields
    private int includedAgents;
    private boolean wizardBotEnabled;
    private Integer supportHours;

    // Monthly extras
    private Double monthlyMobileAppFee;
    private Double monthlyBotConversationHistoryFee;
    private Double monthlyWebChatFee;
    private Double monthlyAppleMessagesFee;
    private Double monthlyAutoConversationAssignmentFee;
    private Double monthlyWhatsappApiFee;
    private Double monthlyHistoricalBackupFee;

    // Annual extras
    private Double annualMobileAppFee;
    private Double annualBotConversationHistoryFee;
    private Double annualWebChatFee;
    private Double annualAppleMessagesFee;
    private Double annualAutoConversationAssignmentFee;
    private Double annualWhatsappApiFee;
    private Double annualHistoricalBackupFee;
}
