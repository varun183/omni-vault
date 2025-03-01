--  version columns to all entity tables for optimistic locking

-- Add version column to users table
ALTER TABLE users ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Add version column to refresh_tokens table
ALTER TABLE refresh_tokens ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Add version column to folders table
ALTER TABLE folders ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Add version column to tags table
ALTER TABLE tags ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Add version column to contents table
ALTER TABLE contents ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Add version column to text_contents table
ALTER TABLE text_contents ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Add version column to link_contents table
ALTER TABLE link_contents ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Add indexes for frequently used join conditions to improve query performance
CREATE INDEX idx_contents_user_folder ON contents(user_id, folder_id);
CREATE INDEX idx_contents_type_user ON contents(content_type, user_id);
CREATE INDEX idx_content_tags_tag_id ON content_tags(tag_id);

-- Add created_at index for efficient sorting of recent content
CREATE INDEX idx_contents_user_created ON contents(user_id, created_at DESC);

-- Add a constraint to ensure content is not orphaned
ALTER TABLE contents ADD CONSTRAINT chk_content_type CHECK (
    (content_type = 'TEXT' AND EXISTS (SELECT 1 FROM text_contents WHERE content_id = id)) OR
    (content_type = 'LINK' AND EXISTS (SELECT 1 FROM link_contents WHERE content_id = id)) OR
    (content_type IN ('IMAGE', 'VIDEO', 'DOCUMENT', 'OTHER') AND storage_path IS NOT NULL)
    );