package mx.com.ebitware.stripe.payment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlansCatalogResponse {
    private String planName;
    private boolean omniChannelPlatform;
    private double onboardingFee;
    private double monthlyFee;
    private int whatsappConversations;
    private boolean fbMessengerEnabled;
    private boolean igMessengerEnabled;
    private boolean webChatEnabled;
    private int includedAgents;
    private double extraAgentFee;
    private boolean massTemplatesEnabled;
    private boolean wizardBotEnabled;
    private boolean apiIntegrationEnabled;
    private boolean supportEnabled;
}
