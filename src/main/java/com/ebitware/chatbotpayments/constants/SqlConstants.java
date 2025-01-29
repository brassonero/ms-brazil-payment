package com.ebitware.chatbotpayments.constants;

public class SqlConstants {

    private SqlConstants() {
    }

    public static final String INSERT_FORM_SUBMISSION =
            "INSERT INTO chatbot.brl_form_submission (business_name, display_name, corporate_email, website, description, " +
                    "facebook_manager_no, phone, address, vertical, logo_url) " +
                    "VALUES (:businessName, :displayName, :corporateEmail, :website, :description, " +
                    ":facebookManagerNo, :phone, :address, :vertical, :logoUrl)";

    public static final String SELECT_ALL_PLANS =
            "SELECT id, plan_name, setup_fee, " +
                    "monthly_total_fee, monthly_fee, monthly_setup_per_agent, monthly_extra_agent_fee, " +
                    "annual_total_fee, annual_fee, annual_setup_per_agent, annual_extra_agent_fee, " +
                    "included_agents, wizard_bot_enabled, support_hours, " +
                    "monthly_mobile_app_fee, monthly_bot_conversation_history_fee, monthly_web_chat_fee, " +
                    "monthly_apple_messages_fee, monthly_auto_conversation_assignment_fee, monthly_whatsapp_api_fee, " +
                    "monthly_historical_backup_fee, annual_mobile_app_fee, annual_bot_conversation_history_fee, " +
                    "annual_web_chat_fee, annual_apple_messages_fee, annual_auto_conversation_assignment_fee, " +
                    "annual_whatsapp_api_fee, annual_historical_backup_fee " +
                    "FROM chatbot.brl_plan_catalog ORDER BY monthly_total_fee";

    public static final String SELECT_ALL_WA_PRICES =
            "SELECT template_type, cost, is_free " +
                    "FROM chatbot.brl_wa_catalog ORDER BY cost DESC";

    public static final String SELECT_ALL_PACKAGES =
            "SELECT package_name, conversations, cost " +
                    "FROM chatbot.brl_package_catalog ORDER BY conversations";

    public static final String INSERT_COMPANY = """
        INSERT INTO chatbot.company (
            name, key_name, mode, agents, multiple_agents, supervisors,
            multiple_supervisors, contacts, type_workgroups, workgroups,
            workgroups_active, active, created_at, updated_at
        ) VALUES (
            :name, :keyName, :mode, CAST(:agents AS INTEGER), :multipleAgents, CAST(:supervisors AS INTEGER),
            :multipleSupervisors, CAST(:contacts AS INTEGER), :typeWorkgroups, CAST(:workgroups AS INTEGER),
            :workgroupsActive, :active, NOW(), NOW()
        ) RETURNING id
        """;

    public static final String INSERT_PERSON = """
        INSERT INTO chatbot.person (
            first_name, last_name, second_last_name,
            username, password, email, active, first_login,
            role_id, company_id, is_super,
            created_at, updated_at
        ) VALUES (
            :firstName, :lastName, :secondLastName,
            :username, :password, :email, true, true,
            1, :companyId, true,
            NOW(), NOW()
        )
        """;

    public static final String INSERT_COMPANY_ACCESS = """
        INSERT INTO chatbot.company_sec_access (
            company_id, sec_access_id, active,
            from_system, created_at, updated_at
        ) VALUES (
            :companyId, :accessId, true,
            true, NOW(), NOW()
        )
        """;

    public static final String INSERT_WORKGROUP = """
        INSERT INTO chatbot.workgroup (
            name, description, company_id, active,
            from_system, created_at, updated_at,
            persons, is_default
        ) VALUES (
            'General', 'Default', :companyId, true,
            :fromSystem, NOW(), NOW(),
            :persons, :isDefault
        )
        """;

    public static final String INSERT_BOT = """
        INSERT INTO chatbot.bot_host (
            name, host, port, company_id, ivr,
            message, status, active, bot_channel,
            created_at, updated_at
        ) VALUES (
            :name, :host, :port, :companyId, :ivr,
            :message, :status, true, :botChannel,
            NOW(), NOW()
        )
        """;

    public static final String CHECK_EMAIL_EXISTS =
            "SELECT COUNT(*) FROM chatbot.person WHERE LOWER(email) = LOWER(:email) AND deleted_at IS NULL";

    public static final String CHECK_EMAIL_EXISTS_DEPRECATED =
            "SELECT COUNT(*) FROM chatbot.brl_form_submission WHERE corporate_email = :email";

    public static final String COUNT_WORKGROUPS =
            "SELECT COUNT(*) FROM chatbot.workgroup WHERE company_id = :companyId AND deleted_at IS NULL";

    public static final String GET_COMPANY_DETAILS =
            "SELECT type_workgroups, workgroups FROM chatbot.company WHERE id = :companyId";

    public static final String CHECK_COMPANY_EXISTS =
            "SELECT COUNT(*) FROM chatbot.company WHERE LOWER(name) = LOWER(:name) AND deleted_at IS NULL";

    public static final String FIND_USERNAMES_LIKE = """
            SELECT username 
            FROM chatbot.person 
            WHERE LOWER(username) LIKE LOWER(:baseUsername) || '%'
            AND deleted_at IS NULL
            ORDER BY username DESC
            """;

    public static final String SELECT_COMPANY_BY_ID =
            "SELECT id, active, created_at, updated_at FROM chatbot.company WHERE id = :companyId";

    public static final String SELECT_PERSON_BY_COMPANY_ID =
            "SELECT id, active, created_at, updated_at FROM chatbot.person WHERE company_id = :companyId";
}