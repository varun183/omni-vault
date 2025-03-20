CREATE TABLE user_oauth_connections (
                                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                        user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                        provider VARCHAR(50) NOT NULL,
                                        provider_user_id VARCHAR(255) NOT NULL,
                                        email VARCHAR(255),
                                        display_name VARCHAR(255),
                                        access_token TEXT,
                                        refresh_token TEXT,
                                        token_expires_at TIMESTAMP WITH TIME ZONE,
                                        scopes VARCHAR(1000),
                                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                        version BIGINT DEFAULT 0 NOT NULL,
                                        UNIQUE (provider, provider_user_id)
);