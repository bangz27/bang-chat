-- ============================================================
-- BANG CHAT: Production PostgreSQL Schema & Supabase Configuration
-- ============================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. PROFILES TABLE
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    bio TEXT,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL,
    last_seen_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()),
    is_online BOOLEAN DEFAULT false NOT NULL
);

-- 2. CONVERSATIONS TABLE
CREATE TABLE IF NOT EXISTS public.conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type TEXT NOT NULL CHECK (type IN ('direct', 'group')),
    title TEXT,
    avatar_url TEXT,
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL,
    last_message_id UUID
);

-- 3. CONVERSATION MEMBERS TABLE
CREATE TABLE IF NOT EXISTS public.conversation_members (
    conversation_id UUID REFERENCES public.conversations(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    role TEXT NOT NULL DEFAULT 'member' CHECK (role IN ('member', 'admin', 'owner')),
    joined_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL,
    last_read_message_id UUID,
    muted BOOLEAN DEFAULT false NOT NULL,
    pinned BOOLEAN DEFAULT false NOT NULL,
    PRIMARY KEY (conversation_id, user_id)
);

-- 4. MESSAGES TABLE
CREATE TABLE IF NOT EXISTS public.messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    type TEXT NOT NULL DEFAULT 'text' CHECK (type IN ('text', 'image', 'file', 'audio', 'system')),
    content TEXT NOT NULL,
    reply_to_message_id UUID REFERENCES public.messages(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL,
    edited_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);

-- 5. MESSAGE ATTACHMENTS TABLE
CREATE TABLE IF NOT EXISTS public.message_attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message_id UUID REFERENCES public.messages(id) ON DELETE CASCADE,
    storage_path TEXT NOT NULL,
    file_name TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    file_size BIGINT DEFAULT 0,
    width INTEGER,
    height INTEGER,
    duration INTEGER,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL
);

-- 6. MESSAGE REACTIONS TABLE
CREATE TABLE IF NOT EXISTS public.message_reactions (
    message_id UUID REFERENCES public.messages(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    reaction TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL,
    PRIMARY KEY (message_id, user_id, reaction)
);

-- 7. MESSAGE READS TABLE
CREATE TABLE IF NOT EXISTS public.message_reads (
    message_id UUID REFERENCES public.messages(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    read_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL,
    PRIMARY KEY (message_id, user_id)
);

-- 8. BLOCKED USERS TABLE
CREATE TABLE IF NOT EXISTS public.blocked_users (
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    blocked_user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL,
    PRIMARY KEY (user_id, blocked_user_id)
);

-- 9. USER DEVICES (PUSH NOTIFICATIONS)
CREATE TABLE IF NOT EXISTS public.user_devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    platform TEXT NOT NULL CHECK (platform IN ('web', 'android', 'ios')),
    push_token TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc', NOW()) NOT NULL
);

-- ============================================================
-- INDEXES FOR MAXIMUM REALTIME PERFORMANCE
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_profiles_username ON public.profiles(username);
CREATE INDEX IF NOT EXISTS idx_profiles_display_name ON public.profiles(display_name);
CREATE INDEX IF NOT EXISTS idx_conversation_members_user_id ON public.conversation_members(user_id);
CREATE INDEX IF NOT EXISTS idx_conversation_members_conversation_id ON public.conversation_members(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON public.messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON public.messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON public.messages(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_message_reactions_message_id ON public.message_reactions(message_id);
CREATE INDEX IF NOT EXISTS idx_message_reads_message_id ON public.message_reads(message_id);
CREATE INDEX IF NOT EXISTS idx_blocked_users_user_id ON public.blocked_users(user_id);

-- ============================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversation_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.message_attachments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.message_reactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.message_reads ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.blocked_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_devices ENABLE ROW LEVEL SECURITY;

-- Profiles Policies
CREATE POLICY "Public profiles are viewable by authenticated users" 
ON public.profiles FOR SELECT TO authenticated USING (true);

CREATE POLICY "Users can insert their own profile" 
ON public.profiles FOR INSERT TO authenticated WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can update their own profile" 
ON public.profiles FOR UPDATE TO authenticated USING (auth.uid() = id);

-- Conversations Policies: Member-only access
CREATE POLICY "Users can view conversations they are member of" 
ON public.conversations FOR SELECT TO authenticated USING (
    EXISTS (
        SELECT 1 FROM public.conversation_members 
        WHERE conversation_members.conversation_id = conversations.id 
        AND conversation_members.user_id = auth.uid()
    )
);

CREATE POLICY "Authenticated users can create conversations" 
ON public.conversations FOR INSERT TO authenticated WITH CHECK (auth.uid() = created_by);

-- Conversation Members Policies
CREATE POLICY "Members can view membership of their conversations" 
ON public.conversation_members FOR SELECT TO authenticated USING (
    EXISTS (
        SELECT 1 FROM public.conversation_members cm
        WHERE cm.conversation_id = conversation_members.conversation_id 
        AND cm.user_id = auth.uid()
    )
);

CREATE POLICY "Users can join or be added to conversations" 
ON public.conversation_members FOR INSERT TO authenticated WITH CHECK (
    user_id = auth.uid() OR 
    EXISTS (
        SELECT 1 FROM public.conversation_members cm 
        WHERE cm.conversation_id = conversation_members.conversation_id 
        AND cm.user_id = auth.uid() 
        AND cm.role IN ('admin', 'owner')
    )
);

CREATE POLICY "Users can update their own membership state (pin, mute, last_read)" 
ON public.conversation_members FOR UPDATE TO authenticated USING (auth.uid() = user_id);

-- Messages Policies
CREATE POLICY "Users can view messages in conversations they belong to" 
ON public.messages FOR SELECT TO authenticated USING (
    EXISTS (
        SELECT 1 FROM public.conversation_members 
        WHERE conversation_members.conversation_id = messages.conversation_id 
        AND conversation_members.user_id = auth.uid()
    )
);

CREATE POLICY "Users can create messages in conversations they belong to" 
ON public.messages FOR INSERT TO authenticated WITH CHECK (
    auth.uid() = sender_id AND
    EXISTS (
        SELECT 1 FROM public.conversation_members 
        WHERE conversation_members.conversation_id = messages.conversation_id 
        AND conversation_members.user_id = auth.uid()
    )
);

CREATE POLICY "Users can update/delete their own messages" 
ON public.messages FOR UPDATE TO authenticated USING (auth.uid() = sender_id);

-- Reactions Policies
CREATE POLICY "Users can view reactions in their conversations" 
ON public.message_reactions FOR SELECT TO authenticated USING (
    EXISTS (
        SELECT 1 FROM public.messages m
        JOIN public.conversation_members cm ON cm.conversation_id = m.conversation_id
        WHERE m.id = message_reactions.message_id AND cm.user_id = auth.uid()
    )
);

CREATE POLICY "Users can add/remove their own reactions" 
ON public.message_reactions FOR ALL TO authenticated USING (auth.uid() = user_id);

-- Message Reads Policies
CREATE POLICY "Users can mark messages as read" 
ON public.message_reads FOR ALL TO authenticated USING (auth.uid() = user_id);

-- Blocked Users Policies
CREATE POLICY "Users can manage their own blocked list" 
ON public.blocked_users FOR ALL TO authenticated USING (auth.uid() = user_id);

-- User Devices Policies
CREATE POLICY "Users can manage their own device tokens" 
ON public.user_devices FOR ALL TO authenticated USING (auth.uid() = user_id);
