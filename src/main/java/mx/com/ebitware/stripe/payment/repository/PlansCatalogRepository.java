package mx.com.ebitware.stripe.payment.repository;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.PlansCatalogResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static mx.com.ebitware.stripe.payment.constants.SqlConstants.SELECT_ALL_PLANS;

@Repository
@RequiredArgsConstructor
public class PlansCatalogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<PlansCatalogResponse> planRowMapper = (rs, rowNum) ->
            PlansCatalogResponse.builder()
                    .planName(rs.getString("plan_name"))
                    .omniChannelPlatform(rs.getBoolean("omnichannel_platform"))
                    .onboardingFee(rs.getDouble("onboarding_fee"))
                    .monthlyFee(rs.getDouble("monthly_fee"))
                    .whatsappConversations(rs.getInt("whatsapp_conversations"))
                    .fbMessengerEnabled(rs.getBoolean("fb_messenger_enabled"))
                    .igMessengerEnabled(rs.getBoolean("ig_messenger_enabled"))
                    .webChatEnabled(rs.getBoolean("web_chat_enabled"))
                    .includedAgents(rs.getInt("included_agents"))
                    .extraAgentFee(rs.getDouble("extra_agent_fee"))
                    .massTemplatesEnabled(rs.getBoolean("mass_templates_enabled"))
                    .wizardBotEnabled(rs.getBoolean("wizard_bot_enabled"))
                    .apiIntegrationEnabled(rs.getBoolean("api_integration_enabled"))
                    .supportEnabled(rs.getBoolean("support_enabled"))
                    .build();

    public List<PlansCatalogResponse> findAllPlans() {
        return jdbcTemplate.query(SELECT_ALL_PLANS, planRowMapper);
    }
}
