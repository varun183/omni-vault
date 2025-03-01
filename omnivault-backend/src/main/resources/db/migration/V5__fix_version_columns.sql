
-- version columns
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE IF EXISTS refresh_tokens ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE IF EXISTS folders ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE IF EXISTS tags ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE IF EXISTS contents ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE IF EXISTS text_contents ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE IF EXISTS link_contents ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

--  indexes for frequently used join conditions to improve query performance
CREATE INDEX IF NOT EXISTS idx_contents_user_folder ON contents(user_id, folder_id);
CREATE INDEX IF NOT EXISTS idx_contents_type_user ON contents(content_type, user_id);
CREATE INDEX IF NOT EXISTS idx_content_tags_tag_id ON content_tags(tag_id);

--  created_at index for efficient sorting of recent content
CREATE INDEX IF NOT EXISTS idx_contents_user_created ON contents(user_id, created_at DESC);

-- Instead of the complex check constraint, a trigger function for validation
CREATE OR REPLACE FUNCTION validate_content_type()
RETURNS TRIGGER AS $$
BEGIN
    -- For TEXT content type, verify an entry exists in text_contents
    IF NEW.content_type = 'TEXT' THEN
        IF NOT EXISTS (SELECT 1 FROM text_contents WHERE content_id = NEW.id) THEN
            RAISE NOTICE 'Creating TEXT content requires an entry in text_contents table';
END IF;
END IF;

    -- For LINK content type, verify an entry exists in link_contents
    IF NEW.content_type = 'LINK' THEN
        IF NOT EXISTS (SELECT 1 FROM link_contents WHERE content_id = NEW.id) THEN
            RAISE NOTICE 'Creating LINK content requires an entry in link_contents table';
END IF;
END IF;

    -- For file-based content types, verify storage_path is not null
    IF NEW.content_type IN ('IMAGE', 'VIDEO', 'DOCUMENT', 'OTHER') AND NEW.storage_path IS NULL THEN
        RAISE NOTICE 'Creating file-based content requires storage_path to be set';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create the trigger (only if it doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'content_type_validation_trigger'
    ) THEN
CREATE TRIGGER content_type_validation_trigger
    AFTER INSERT OR UPDATE ON contents
                        FOR EACH ROW
                        EXECUTE FUNCTION validate_content_type();
END IF;
END $$;