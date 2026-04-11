-- Backtick column names: `current_role` is invalid without quotes on MariaDB/MySQL (CURRENT / CURRENT_ROLE parsing).
CREATE TABLE learner_portfolio (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    `current_role` VARCHAR(100),
    target_role VARCHAR(100),
    experience_level VARCHAR(32) NOT NULL,
    bio TEXT,
    weekly_study_hours INT,
    preferred_language VARCHAR(10),
    learning_style VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_learner_portfolio_user (user_id)
);

CREATE TABLE learner_skill (
    id BIGINT NOT NULL AUTO_INCREMENT,
    portfolio_id BIGINT NOT NULL,
    skill_name VARCHAR(200) NOT NULL,
    skill_level VARCHAR(32) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_learner_skill_portfolio (portfolio_id),
    CONSTRAINT fk_learner_skill_portfolio FOREIGN KEY (portfolio_id) REFERENCES learner_portfolio (id) ON DELETE CASCADE
);

CREATE TABLE mentor_session (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    channel VARCHAR(32) NOT NULL,
    formation_id BIGINT,
    model_used VARCHAR(100),
    period_start DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_mentor_session_user_channel_period (user_id, channel, period_start)
);

CREATE TABLE mentor_advice (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    summary TEXT,
    structured_json LONGTEXT,
    raw_text LONGTEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_mentor_advice_session FOREIGN KEY (session_id) REFERENCES mentor_session (id) ON DELETE CASCADE
);

CREATE TABLE study_plan (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    source_session_id BIGINT,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_until DATE,
    PRIMARY KEY (id),
    KEY idx_study_plan_user (user_id),
    CONSTRAINT fk_study_plan_session FOREIGN KEY (source_session_id) REFERENCES mentor_session (id) ON DELETE SET NULL
);

CREATE TABLE study_plan_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    study_plan_id BIGINT NOT NULL,
    sort_order INT NOT NULL,
    item_type VARCHAR(32) NOT NULL,
    title VARCHAR(500) NOT NULL,
    duration_minutes INT,
    resource_ref VARCHAR(500),
    PRIMARY KEY (id),
    KEY idx_study_plan_item_plan (study_plan_id),
    CONSTRAINT fk_study_plan_item_plan FOREIGN KEY (study_plan_id) REFERENCES study_plan (id) ON DELETE CASCADE
);
