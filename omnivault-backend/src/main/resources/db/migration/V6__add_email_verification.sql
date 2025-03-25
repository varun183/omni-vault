ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT true;

-- Set existing users as verified and enabled
UPDATE users SET email_verified = true, enabled = true;

CREATE TABLE verification_tokens (
                                     id UUID PRIMARY KEY,
                                     token VARCHAR(255) NOT NULL,
                                     otp_code VARCHAR(6),
                                     user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                     expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
                                     created_at TIMESTAMP WITH TIME ZONE,
                                     updated_at TIMESTAMP WITH TIME ZONE,
                                     version BIGINT DEFAULT 0
);

CREATE INDEX idx_verification_token ON verification_tokens(token);
CREATE INDEX idx_verification_otp ON verification_tokens(otp_code);