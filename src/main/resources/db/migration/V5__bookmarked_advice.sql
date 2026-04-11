CREATE TABLE bookmarked_advice (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT       NOT NULL,
  session_id BIGINT       NOT NULL,
  note       VARCHAR(500),
  created_at DATETIME     NOT NULL,
  CONSTRAINT uq_bookmark UNIQUE (user_id, session_id),
  INDEX idx_bookmark_user (user_id)
);
