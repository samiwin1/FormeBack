-- Add assignment-level scheduled time override
-- This allows individual assignments to have different scheduled times
-- without affecting other learners in the same session

ALTER TABLE oral_exam_assignments 
ADD COLUMN scheduled_at_override DATETIME NULL
COMMENT 'Assignment-specific scheduled time (overrides session scheduledAt if present)';

-- Create index for queries filtering by scheduled time
CREATE INDEX idx_oral_exam_assignments_scheduled_override 
ON oral_exam_assignments(scheduled_at_override);
