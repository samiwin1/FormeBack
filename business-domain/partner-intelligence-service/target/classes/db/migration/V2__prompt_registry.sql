CREATE TABLE IF NOT EXISTS prompt_template_version (id BIGINT PRIMARY KEY AUTO_INCREMENT, prompt_key VARCHAR(255), version_tag VARCHAR(100), template_text TEXT, active BOOLEAN, created_at TIMESTAMP);
