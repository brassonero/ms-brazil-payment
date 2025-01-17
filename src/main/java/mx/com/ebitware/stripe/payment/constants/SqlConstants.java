package mx.com.ebitware.stripe.payment.constants;

public class SqlConstants {

    private SqlConstants() {
    }

    public static final String INSERT_FORM_SUBMISSION =
            "INSERT INTO chatbot.brl_form_submission (business_name, display_name, corporate_email, website, description, " +
                    "facebook_manager_no, phone, address, vertical, logo_url) " +
                    "VALUES (:businessName, :displayName, :corporateEmail, :website, :description, " +
                    ":facebookManagerNo, :phone, :address, :vertical, :logoUrl)";

    public static final String SELECT_ALL_PLANS =
            "SELECT plan_name, omnichannel_platform, onboarding_fee, monthly_fee, " +
                    "whatsapp_conversations, fb_messenger_enabled, ig_messenger_enabled, " +
                    "web_chat_enabled, included_agents, extra_agent_fee, mass_templates_enabled, " +
                    "wizard_bot_enabled, api_integration_enabled, support_enabled " +
                    "FROM chatbot.brl_plan_catalog ORDER BY monthly_fee";

    public static final String SELECT_ALL_WA_PRICES =
            "SELECT template_type, cost, is_free " +
                    "FROM chatbot.brl_wa_catalog ORDER BY cost DESC";

    public static final String SELECT_ALL_PACKAGES =
            "SELECT package_name, conversations, cost " +
                    "FROM chatbot.brl_package_catalog ORDER BY conversations";

    public static final String CHECK_EMAIL_EXISTS =
            "SELECT COUNT(*) FROM chatbot.brl_form_submission WHERE corporate_email = :email";
}
