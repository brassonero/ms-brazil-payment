CREATE TABLE chatbot.brl_form_submission ( 
    id SERIAL PRIMARY KEY,
    business_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    website VARCHAR(255) CHECK (website ~ '^https?://[a-zA-Z0-9.-]+(?:\.[a-zA-Z]{2,})+(?::\d+)?(?:/.*)?$'),
    corporate_email VARCHAR(255) NOT NULL
        CHECK (corporate_email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    description TEXT,
    facebook_manager_no VARCHAR(50),
    phone VARCHAR(50) NOT NULL CHECK (phone ~ '^\+?[1-9]\d{1,14}$'),
    address TEXT NOT NULL,
    vertical VARCHAR(100) NOT NULL,
    logo_url VARCHAR(255) CHECK (logo_url ~ '^https?://.*'),
    email_confirmed BOOLEAN DEFAULT FALSE,
    confirmation_token VARCHAR(255),
    token_expiry TIMESTAMP WITH TIME ZONE,
    confirmation_sent_at TIMESTAMP WITH TIME ZONE,
    confirmed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    company_id INTEGER,
    person_id INTEGER,
    role_id INTEGER,
    CONSTRAINT unique_business_email UNIQUE (corporate_email),
    CONSTRAINT unique_confirmation_token UNIQUE (confirmation_token)
);

CREATE TABLE chatbot.brl_plan_catalog (
    id SERIAL PRIMARY KEY,
    plan_name VARCHAR(50) NOT NULL,
    -- Base fees
    setup_fee DECIMAL(10,2) NOT NULL CHECK (setup_fee >= 0),

    -- Monthly pricing
    monthly_total_fee DECIMAL(10,2) NOT NULL CHECK (monthly_total_fee >= 0),
    monthly_fee DECIMAL(10,2) NOT NULL CHECK (monthly_fee >= 0),
    monthly_setup_per_agent DECIMAL(10,2) NOT NULL CHECK (monthly_setup_per_agent >= 0),
    monthly_extra_agent_fee DECIMAL(10,2) NOT NULL CHECK (monthly_extra_agent_fee >= 0),

    -- Annual pricing
    annual_total_fee DECIMAL(10,2) NOT NULL CHECK (annual_total_fee >= 0),
    annual_fee DECIMAL(10,2) NOT NULL CHECK (annual_fee >= 0),
    annual_setup_per_agent DECIMAL(10,2) NOT NULL CHECK (annual_setup_per_agent >= 0),
    annual_extra_agent_fee DECIMAL(10,2) NOT NULL CHECK (annual_extra_agent_fee >= 0),

    -- Common fields
    included_agents INTEGER NOT NULL CHECK (included_agents > 0),
    wizard_bot_enabled BOOLEAN NOT NULL DEFAULT true,
    support_hours INTEGER,

    -- Monthly extras
    monthly_mobile_app_fee DECIMAL(10,2) CHECK (monthly_mobile_app_fee >= 0),
    monthly_bot_conversation_history_fee DECIMAL(10,2) CHECK (monthly_bot_conversation_history_fee >= 0),
    monthly_web_chat_fee DECIMAL(10,2) CHECK (monthly_web_chat_fee >= 0),
    monthly_apple_messages_fee DECIMAL(10,2) CHECK (monthly_apple_messages_fee >= 0),
    monthly_auto_conversation_assignment_fee DECIMAL(10,2) CHECK (monthly_auto_conversation_assignment_fee >= 0),
    monthly_whatsapp_api_fee DECIMAL(10,2) CHECK (monthly_whatsapp_api_fee >= 0),
    monthly_historical_backup_fee DECIMAL(10,2) CHECK (monthly_historical_backup_fee >= 0),

    -- Annual extras
    annual_mobile_app_fee DECIMAL(10,2) CHECK (annual_mobile_app_fee >= 0),
    annual_bot_conversation_history_fee DECIMAL(10,2) CHECK (annual_bot_conversation_history_fee >= 0),
    annual_web_chat_fee DECIMAL(10,2) CHECK (annual_web_chat_fee >= 0),
    annual_apple_messages_fee DECIMAL(10,2) CHECK (annual_apple_messages_fee >= 0),
    annual_auto_conversation_assignment_fee DECIMAL(10,2) CHECK (annual_auto_conversation_assignment_fee >= 0),
    annual_whatsapp_api_fee DECIMAL(10,2) CHECK (annual_whatsapp_api_fee >= 0),
    annual_historical_backup_fee DECIMAL(10,2) CHECK (annual_historical_backup_fee >= 0),

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_plan_name UNIQUE (plan_name)
);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';


CREATE TRIGGER update_plan_catalog_updated_at
    BEFORE UPDATE ON chatbot.brl_plan_catalog
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

INSERT INTO chatbot.brl_plan_catalog (
    plan_name,
    setup_fee,
    -- Monthly values
    monthly_total_fee,
    monthly_fee,
    monthly_setup_per_agent,
    monthly_extra_agent_fee,
    -- Annual values
    annual_total_fee,
    annual_fee,
    annual_setup_per_agent,
    annual_extra_agent_fee,
    -- Common fields
    included_agents,
    wizard_bot_enabled,
    support_hours,
    -- Monthly extras
    monthly_mobile_app_fee,
    monthly_bot_conversation_history_fee,
    monthly_web_chat_fee,
    monthly_apple_messages_fee,
    monthly_auto_conversation_assignment_fee,
    monthly_whatsapp_api_fee,
    monthly_historical_backup_fee,
    -- Annual extras
    annual_mobile_app_fee,
    annual_bot_conversation_history_fee,
    annual_web_chat_fee,
    annual_apple_messages_fee,
    annual_auto_conversation_assignment_fee,
    annual_whatsapp_api_fee,
    annual_historical_backup_fee
) VALUES
-- Growth Plan
(
    'Growth',
    1999.00,
    -- Monthly
    168.28,
    168.28,
    179.80,
    32.28,
    -- Annual
    129.83,
    129.83,
    149.83,
    24.83,
    -- Common
    5,
    true,
    NULL,
    -- Monthly extras
    130.00,
    130.00,
    130.00,
    130.00,
    130.00,
    130.00,
    130.00,
    -- Annual extras
    100.00,
    100.00,
    100.00,
    100.00,
    100.00,
    100.00,
    100.00
),
-- Business Plan
(
    'Business',
    1999.00,
    -- Monthly
    259.78,
    259.78,
    179.80,
    32.28,
    -- Annual
    199.83,
    199.83,
    149.83,
    24.83,
    -- Common
    10,
    true,
    5,
    -- Monthly extras
    117.00,
    117.00,
    117.00,
    117.00,
    117.00,
    117.00,
    117.00,
    -- Annual extras
    100.00,
    100.00,
    100.00,
    100.00,
    100.00,
    100.00,
    100.00
),
-- Enterprise Plan
(
    'Enterprise',
    1999.00,
    -- Monthly
    346.67,
    346.67,
    179.80,
    32.28,
    -- Annual
    266.67,
    266.67,
    149.83,
    24.83,
    -- Common
    15,
    true,
    8,
    -- Monthly extras
    104.00,
    104.00,
    104.00,
    104.00,
    104.00,
    NULL,
    104.00,
    -- Annual extras
    80.00,
    80.00,
    80.00,
    80.00,
    80.00,
    NULL,
    80.00
);

CREATE TABLE chatbot.brl_wa_catalog (
    id SERIAL PRIMARY KEY,
    template_type VARCHAR(50) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL CHECK (cost >= 0),
    is_free BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT cost_free_check CHECK (
        (is_free = true AND cost = 0) OR 
        (is_free = false AND cost > 0)
    )
);

INSERT INTO chatbot.brl_wa_catalog (template_type, cost, is_free) VALUES
('Plantilla Marketing', 0.99, false),
('Plantilla Utilidad', 0.64, false),
('Plantilla Autenticación', 0.58, false),
('Plantilla Servicio', 0.00, true);

CREATE TABLE chatbot.brl_package_catalog (
    id SERIAL PRIMARY KEY,
    package_name VARCHAR(20) NOT NULL,
    conversations INTEGER NOT NULL CHECK (conversations > 0),
    cost DECIMAL(10,2) NOT NULL CHECK (cost >= 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_package_name UNIQUE (package_name)
);

INSERT INTO chatbot.brl_package_catalog (package_name, conversations, cost) VALUES
('Paquete 01', 5000, 4950.00),
('Paquete 02', 10000, 9800.00),
('Paquete 03', 30000, 29100.00),
('Paquete 04', 50000, 48000.00),
('Paquete 05', 100000, 95000.00),
('Paquete 06', 200000, 188000.00);


CREATE TABLE chatbot.brl_products (
    id BIGSERIAL PRIMARY KEY,
    stripe_product_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_stripe_product_id UNIQUE (stripe_product_id),
    CONSTRAINT unique_product_name UNIQUE (name)
);

CREATE TABLE chatbot.brl_prices (
    id BIGSERIAL PRIMARY KEY,
    stripe_price_id VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    stripe_product_id VARCHAR(255) NOT NULL,
    unit_amount DOUBLE PRECISION NOT NULL,  -- Changed from BIGINT to DOUBLE PRECISION
    currency VARCHAR(3) NOT NULL,
    interval_type VARCHAR(10) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_stripe_price_id UNIQUE (stripe_price_id),
    CONSTRAINT fk_price_product FOREIGN KEY (product_id)
        REFERENCES chatbot.brl_products(id),
    CONSTRAINT fk_price_stripe_product FOREIGN KEY (stripe_product_id)
        REFERENCES chatbot.brl_products(stripe_product_id)
);

CREATE TABLE chatbot.brl_customers (
    id VARCHAR(255) PRIMARY KEY,
    document VARCHAR(20) NOT NULL,
    document_type VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    person_id INTEGER
);

CREATE TABLE chatbot.brl_payments (
    id VARCHAR(255) PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL REFERENCES chatbot.brl_customers(id),
    payment_method_id VARCHAR(255) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE chatbot.brl_subscriptions (
    id VARCHAR(255) PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL REFERENCES chatbot.brl_customers(id),
    status VARCHAR(50) NOT NULL,
    price_id VARCHAR(255) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_method_id VARCHAR(255) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE chatbot.brl_invoice_info (
    id SERIAL PRIMARY KEY,
    fiscal_regime VARCHAR(50) NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    id_type VARCHAR(50) NOT NULL,
    id_number VARCHAR(50) NOT NULL CHECK (id_number ~ '^\d+$'),
    billing_email VARCHAR(255) NOT NULL CHECK (billing_email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    phone VARCHAR(50) CHECK (phone ~ '^\+?[1-9]\d{1,14}$'),
    street VARCHAR(255) NOT NULL,
    state VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) CHECK (postal_code ~ '^\d+$'),
    tax_id VARCHAR(50) UNIQUE,
    cfdi_usage VARCHAR(50),
    neighborhood VARCHAR(100),
    person_id INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_billing_email UNIQUE (billing_email)
);