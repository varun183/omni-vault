
-- Add storage location columns to contents table
ALTER TABLE contents
    ADD COLUMN storage_location VARCHAR(10) DEFAULT 'LOCAL' NOT NULL,
    ADD COLUMN thumbnail_storage_location VARCHAR(10) DEFAULT 'LOCAL';

-- Update existing records to use LOCAL storage
UPDATE contents SET
    storage_location = 'LOCAL',
    thumbnail_storage_location = CASE
        WHEN thumbnail_path IS NOT NULL THEN 'LOCAL'
        ELSE NULL
    END;