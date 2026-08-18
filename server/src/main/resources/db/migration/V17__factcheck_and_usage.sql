-- Post-verification support:
--  1) fact_check: a short moderator-facing note attached to a classified board post, summarizing any
--     matching Google Fact Check Tools claims (publisher + rating + link). Null when no key / no match.
--  2) api_usage: a persistent per-feature monthly call counter backing the spend cap (UsageLimiter), so
--     paid/quota external calls (Anthropic classify, Google Fact Check) hard-stop when the owner's
--     configured monthly limit is reached — and the count survives restarts (an in-memory counter would
--     reset and let the budget be blown).
ALTER TABLE resource_posts ADD COLUMN fact_check text;

CREATE TABLE api_usage (
    feature    varchar(40) NOT NULL,
    period     varchar(7)  NOT NULL,          -- 'YYYY-MM' (UTC month)
    count      integer     NOT NULL DEFAULT 0,
    PRIMARY KEY (feature, period)
);

--  3) classify_cache: memoizes the classify + fact-check RESULT keyed by a hash of the pasted text, so
--     the same viral post pasted again is served from cache instead of paying Anthropic/Fact Check a
--     second time. `checked` records whether a fact-check verdict was obtained (true) or not (false),
--     so we don't re-query the fact API for text we've already checked.
CREATE TABLE classify_cache (
    content_hash  varchar(64) PRIMARY KEY,    -- SHA-256 hex of the normalized pasted text
    kind          varchar(20)  NOT NULL,
    resource_type varchar(30)  NOT NULL,
    region        varchar(120) NOT NULL,
    description   text         NOT NULL,
    contact_phone varchar(40),
    contact_name  varchar(120),
    fact_check    text,                        -- cached fact-check note (null if none matched)
    checked       boolean      NOT NULL DEFAULT false, -- was a fact-check lookup performed?
    created_at    timestamp    NOT NULL DEFAULT now()
);
