CREATE TABLE learner_streak (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id          BIGINT  NOT NULL UNIQUE,
  current_streak   INT     NOT NULL DEFAULT 0,
  best_streak      INT     NOT NULL DEFAULT 0,
  last_active_date DATE,
  updated_at       DATETIME NOT NULL,
  INDEX idx_streak_user (user_id)
);

CREATE TABLE reminder_log (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id       BIGINT       NOT NULL,
  sent_date     DATE         NOT NULL,
  reminder_type VARCHAR(20)  NOT NULL,
  created_at    DATETIME     NOT NULL,
  CONSTRAINT uq_reminder_user_date UNIQUE (user_id, sent_date, reminder_type),
  INDEX idx_reminder_user (user_id)
);
