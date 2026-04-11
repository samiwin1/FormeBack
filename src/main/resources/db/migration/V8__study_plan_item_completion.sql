ALTER TABLE study_plan_item
    ADD COLUMN completed     BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN completed_at  DATETIME     NULL;
