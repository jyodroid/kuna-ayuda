-- Paste-and-classify: users paste a free-text post (e.g. from Instagram) and Claude classifies it
-- into a structured board entry. Classified entries land as status='PENDING' (a new status value —
-- the public GET only returns 'ACTIVE'), so an admin reviews them before they go live (anti-fraud).
-- `source` distinguishes manual vs classified; `raw_text` keeps the original for the moderator.

ALTER TABLE resource_posts ADD COLUMN source   VARCHAR(20) NOT NULL DEFAULT 'manual'; -- manual | classified
ALTER TABLE resource_posts ADD COLUMN raw_text TEXT;                                   -- original pasted text

CREATE INDEX idx_board_status ON resource_posts (status);
