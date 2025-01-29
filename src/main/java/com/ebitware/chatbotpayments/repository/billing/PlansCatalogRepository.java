package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.model.PlansDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

import static com.ebitware.chatbotpayments.constants.SqlConstants.SELECT_ALL_PLANS;

@Repository
public class PlansCatalogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PlansCatalogRepository(@Qualifier("billingDataSource") DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    private final RowMapper<PlansDTO> planRowMapper = (rs, rowNum) ->
            PlansDTO.builder()
                    .id(rs.getLong("id"))
                    .planName(rs.getString("plan_name"))
                    .setupFee(rs.getDouble("setup_fee"))
                    // Monthly pricing
                    .monthlyTotalFee(rs.getDouble("monthly_total_fee"))
                    .monthlyFee(rs.getDouble("monthly_fee"))
                    .monthlySetupPerAgent(rs.getDouble("monthly_setup_per_agent"))
                    .monthlyExtraAgentFee(rs.getDouble("monthly_extra_agent_fee"))
                    // Annual pricing
                    .annualTotalFee(rs.getDouble("annual_total_fee"))
                    .annualFee(rs.getDouble("annual_fee"))
                    .annualSetupPerAgent(rs.getDouble("annual_setup_per_agent"))
                    .annualExtraAgentFee(rs.getDouble("annual_extra_agent_fee"))
                    // Common fields
                    .includedAgents(rs.getInt("included_agents"))
                    .wizardBotEnabled(rs.getBoolean("wizard_bot_enabled"))
                    .supportHours(rs.getInt("support_hours"))
                    // Monthly extras
                    .monthlyMobileAppFee(rs.getDouble("monthly_mobile_app_fee"))
                    .monthlyBotConversationHistoryFee(rs.getDouble("monthly_bot_conversation_history_fee"))
                    .monthlyWebChatFee(rs.getDouble("monthly_web_chat_fee"))
                    .monthlyAppleMessagesFee(rs.getDouble("monthly_apple_messages_fee"))
                    .monthlyAutoConversationAssignmentFee(rs.getDouble("monthly_auto_conversation_assignment_fee"))
                    .monthlyWhatsappApiFee(rs.getDouble("monthly_whatsapp_api_fee"))
                    .monthlyHistoricalBackupFee(rs.getDouble("monthly_historical_backup_fee"))
                    // Annual extras
                    .annualMobileAppFee(rs.getDouble("annual_mobile_app_fee"))
                    .annualBotConversationHistoryFee(rs.getDouble("annual_bot_conversation_history_fee"))
                    .annualWebChatFee(rs.getDouble("annual_web_chat_fee"))
                    .annualAppleMessagesFee(rs.getDouble("annual_apple_messages_fee"))
                    .annualAutoConversationAssignmentFee(rs.getDouble("annual_auto_conversation_assignment_fee"))
                    .annualWhatsappApiFee(rs.getDouble("annual_whatsapp_api_fee"))
                    .annualHistoricalBackupFee(rs.getDouble("annual_historical_backup_fee"))
                    .build();

    public List<PlansDTO> findAllPlans() {
        return jdbcTemplate.query(SELECT_ALL_PLANS, planRowMapper);
    }
}
