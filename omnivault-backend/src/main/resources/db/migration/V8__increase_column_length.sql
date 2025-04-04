-- Increase the length of string columns that might contain long paths
ALTER TABLE contents
ALTER COLUMN storage_path TYPE TEXT,
    ALTER COLUMN original_filename TYPE TEXT,
    ALTER COLUMN thumbnail_path TYPE TEXT;