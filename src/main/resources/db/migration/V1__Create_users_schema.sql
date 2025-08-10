-- ObserveTask User Service Database Migration V1
-- Creates the complete user management schema in observetask_users

-- Create the users table
CREATE TABLE IF NOT EXISTS observetask_users.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255), -- nullable for SSO users
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL' CHECK (auth_provider IN ('LOCAL', 'AUTH0', 'SAML', 'GOOGLE', 'MICROSOFT')),
    external_id VARCHAR(255), -- SSO provider user ID
    email_verified BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(auth_provider, external_id) -- Ensure unique external IDs per provider
);

-- Create the user_roles table
CREATE TABLE IF NOT EXISTS observetask_users.user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES observetask_users.users(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('SUPER_ADMIN', 'ORG_ADMIN', 'TEAM_ADMIN', 'TEAM_MEMBER')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, organization_id)
);

-- Create the jwt_refresh_tokens table
CREATE TABLE IF NOT EXISTS observetask_users.jwt_refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES observetask_users.users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    device_info VARCHAR(500),
    ip_address VARCHAR(45)
);

-- Create the jwt_blacklist table for revoked tokens
CREATE TABLE IF NOT EXISTS observetask_users.jwt_blacklist (
    token_jti VARCHAR(255) PRIMARY KEY,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create the invitations table
CREATE TABLE IF NOT EXISTS observetask_users.invitations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    organization_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('SUPER_ADMIN', 'ORG_ADMIN', 'TEAM_ADMIN', 'TEAM_MEMBER')),
    token VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'REVOKED')),
    invited_by UUID NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP WITH TIME ZONE,
    first_name VARCHAR(100),
    last_name VARCHAR(100)
);

-- Create indexes for performance optimization

-- Users table indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON observetask_users.users(email);
CREATE INDEX IF NOT EXISTS idx_users_active ON observetask_users.users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON observetask_users.users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_name ON observetask_users.users(first_name, last_name);
CREATE INDEX IF NOT EXISTS idx_users_auth_provider ON observetask_users.users(auth_provider);
CREATE INDEX IF NOT EXISTS idx_users_external_id ON observetask_users.users(external_id);
CREATE INDEX IF NOT EXISTS idx_users_email_verified ON observetask_users.users(email_verified);

-- User roles table indexes
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON observetask_users.user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_org_id ON observetask_users.user_roles(organization_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON observetask_users.user_roles(role);
CREATE INDEX IF NOT EXISTS idx_user_roles_org_role ON observetask_users.user_roles(organization_id, role);

-- Refresh tokens table indexes
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON observetask_users.jwt_refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON observetask_users.jwt_refresh_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash ON observetask_users.jwt_refresh_tokens(token_hash);

-- JWT blacklist table indexes
CREATE INDEX IF NOT EXISTS idx_jwt_blacklist_expires_at ON observetask_users.jwt_blacklist(expires_at);

-- Invitations table indexes
CREATE INDEX IF NOT EXISTS idx_invitations_email ON observetask_users.invitations(email);
CREATE INDEX IF NOT EXISTS idx_invitations_org_id ON observetask_users.invitations(organization_id);
CREATE INDEX IF NOT EXISTS idx_invitations_token ON observetask_users.invitations(token);
CREATE INDEX IF NOT EXISTS idx_invitations_status ON observetask_users.invitations(status);
CREATE INDEX IF NOT EXISTS idx_invitations_expires_at ON observetask_users.invitations(expires_at);
CREATE INDEX IF NOT EXISTS idx_invitations_invited_by ON observetask_users.invitations(invited_by);

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION observetask_users.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON observetask_users.users 
    FOR EACH ROW 
    EXECUTE FUNCTION observetask_users.update_updated_at_column();

-- Insert sample super admin user for development (password: admin123)
-- Password hash for 'admin123' using bcrypt rounds=12
INSERT INTO observetask_users.users (
    id, 
    email, 
    password_hash, 
    first_name, 
    last_name, 
    auth_provider, 
    external_id, 
    email_verified, 
    is_active
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'admin@observetask.demo',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj6hsXg9BSzS', -- bcrypt hash of 'admin123'
    'System',
    'Administrator',
    'LOCAL',
    NULL,
    true,
    true
) ON CONFLICT (email) DO NOTHING;

-- Insert sample SSO user for testing
INSERT INTO observetask_users.users (
    id,
    email,
    password_hash,
    first_name,
    last_name,
    auth_provider,
    external_id,
    email_verified,
    is_active
) VALUES (
    '22222222-2222-2222-2222-222222222222',
    'sso.user@observetask.demo',
    NULL, -- No password for SSO user
    'SSO',
    'User',
    'AUTH0',
    'auth0|123456789',
    true,
    true
) ON CONFLICT (email) DO NOTHING;

-- Insert sample organization and roles for demo
-- Note: Organization ID is a placeholder - will be replaced when Organization Service is implemented
INSERT INTO observetask_users.user_roles (
    user_id,
    organization_id,
    role
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    '99999999-9999-9999-9999-999999999999', -- Demo organization ID
    'SUPER_ADMIN'
) ON CONFLICT (user_id, organization_id) DO NOTHING;

INSERT INTO observetask_users.user_roles (
    user_id,
    organization_id,
    role
) VALUES (
    '22222222-2222-2222-2222-222222222222',
    '99999999-9999-9999-9999-999999999999', -- Demo organization ID
    'ORG_ADMIN'
) ON CONFLICT (user_id, organization_id) DO NOTHING;