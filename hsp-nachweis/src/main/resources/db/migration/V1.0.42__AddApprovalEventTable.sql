-- Migration to add approval_event table for tracking approval workflow history
-- Each action (request, assign, approve, reject, withdraw) creates an event with actor, timestamp, and comment

-- Create approval_event table
CREATE TABLE IF NOT EXISTS approval_event
(
    id         VARCHAR(255) PRIMARY KEY,
    event_type VARCHAR(50)  NOT NULL,
    actor      VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    comment    VARCHAR(4096)
);

-- Create indexes for efficient lookups
CREATE INDEX IF NOT EXISTS idx_approval_event_created_at ON approval_event (created_at);
CREATE INDEX IF NOT EXISTS idx_approval_event_type ON approval_event (event_type);

-- Create join table for approval_request <-> approval_event relationship
-- This follows the same pattern as publikation_approval
CREATE TABLE IF NOT EXISTS approval_request_event
(
    approval_request_id VARCHAR(255) NOT NULL,
    approval_event_id   VARCHAR(255) NOT NULL,
    PRIMARY KEY (approval_request_id, approval_event_id)
);

-- Add foreign key constraints for join table
-- CASCADE on approval_request to clean up join table when request is deleted
-- CASCADE on approval_event to clean up join table when event is deleted
ALTER TABLE IF EXISTS approval_request_event
    ADD CONSTRAINT fk_approval_request_event_request
        FOREIGN KEY (approval_request_id) REFERENCES approval_request (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_approval_request_event_event
        FOREIGN KEY (approval_event_id) REFERENCES approval_event (id) ON DELETE CASCADE;

-- Create indexes for efficient lookups on join table
CREATE INDEX IF NOT EXISTS idx_approval_request_event_request ON approval_request_event (approval_request_id);
CREATE INDEX IF NOT EXISTS idx_approval_request_event_event ON approval_request_event (approval_event_id);

-- Drop comment column from approval_request (comments are now in events)
ALTER TABLE IF EXISTS approval_request
    DROP COLUMN IF EXISTS comment;
