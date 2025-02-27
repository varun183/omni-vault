-- Function definition
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create content type as a varchar instead of enum for better compatibility
CREATE TABLE contents (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          title VARCHAR(255) NOT NULL,
                          description TEXT,
                          content_type VARCHAR(50) NOT NULL CHECK (content_type IN ('TEXT', 'LINK', 'IMAGE', 'VIDEO', 'DOCUMENT', 'OTHER')),
                          folder_id UUID REFERENCES folders(id) ON DELETE SET NULL,
                          user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          size_bytes BIGINT,
                          mime_type VARCHAR(100),
                          storage_path VARCHAR(255),
                          original_filename VARCHAR(255),
                          thumbnail_path VARCHAR(255),
                          is_favorite BOOLEAN DEFAULT FALSE,
                          view_count INTEGER DEFAULT 0,
                          metadata JSONB,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create text content table for storing actual text content
CREATE TABLE text_contents (
                               content_id UUID PRIMARY KEY REFERENCES contents(id) ON DELETE CASCADE,
                               text_content TEXT NOT NULL
);

-- Create link content table for storing link-specific data
CREATE TABLE link_contents (
                               content_id UUID PRIMARY KEY REFERENCES contents(id) ON DELETE CASCADE,
                               url TEXT NOT NULL,
                               preview_image_path VARCHAR(255)
);

-- Create content_tags junction table
CREATE TABLE content_tags (
                              content_id UUID NOT NULL REFERENCES contents(id) ON DELETE CASCADE,
                              tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (content_id, tag_id)
);

-- Create search index using GIN for full-text search
CREATE INDEX idx_contents_title_description ON contents USING GIN (to_tsvector('english', title || ' ' || COALESCE(description, '')));
CREATE INDEX idx_text_contents ON text_contents USING GIN (to_tsvector('english', text_content));

-- Create other indexes
CREATE INDEX idx_contents_user_id ON contents(user_id);
CREATE INDEX idx_contents_folder_id ON contents(folder_id);
CREATE INDEX idx_contents_content_type ON contents(content_type);
CREATE INDEX idx_contents_created_at ON contents(created_at);
CREATE INDEX idx_contents_is_favorite ON contents(is_favorite);

-- Create trigger for updating the updated_at column
CREATE TRIGGER update_contents_updated_at
    BEFORE UPDATE ON contents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();