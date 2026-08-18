# Cheap & capped API keys — Anthropic + Google Fact Check

This app calls two paid/quota external APIs from the **board paste‑and‑classify** flow:

- **Anthropic** (Claude) — structures a pasted social‑media post into a board entry. *Paid.*
- **Google Fact Check Tools** — searches known fact‑checks for the pasted text (moderator signal). *Free, quota‑limited.*

Because this project is self‑funded, there are **three independent layers of cost control**, so you can
never get a surprise bill:

1. **Provider‑side hard ceiling** — prepaid credit with auto‑reload OFF (Anthropic literally cannot
   charge past what you loaded).
2. **App‑side monthly cap** — `ANTHROPIC_MONTHLY_LIMIT` / `FACTCHECK_MONTHLY_LIMIT`. When hit, the
   feature returns **HTTP 429** and stops calling out for the rest of the month.
3. **Result cache** — the same viral post pasted again is served from `classify_cache` with **no** paid
   call.

The Anthropic count is recorded **only after a successful call**, so a misconfigured key returning errors
can’t burn your budget.

---

## A. Anthropic — cheapest safe setup

- [ ] **1. Create an account** at <https://console.anthropic.com> and verify your email.
- [ ] **2. Load a small prepaid credit** (Console → **Billing** → *Add credits*, e.g. **$5**).
- [ ] **3. Turn OFF auto‑reload** (Billing → *Auto‑reload* → disabled). This is your hard ceiling — when
      the $5 is spent, calls fail instead of billing your card.
- [ ] **4. Set a monthly spend limit** as a second belt (Billing → *Usage limits* → set a low monthly
      cap). Optional but recommended.
- [ ] **5. Create an API key** (Console → **API keys** → *Create key*). Copy it once — you can’t see it
      again. Name it e.g. `kuna-server`.
- [ ] **6. Pick the cheap model.** The server defaults to **Haiku** (`claude-haiku-4-5-20251001`), which
      is many times cheaper than Opus and is plenty for short structured extraction. To change it, set
      `ANTHROPIC_MODEL` (e.g. to an Opus model) — leave it unset to keep the cheap default.

**Rough order‑of‑magnitude cost:** classifying one short post is a few hundred input + ~100 output
tokens. On Haiku that is a tiny fraction of a cent per post; **$5 of credit classifies on the order of
thousands of posts**, and the cache means repeats are free. (Check current per‑token prices on the
Anthropic pricing page — they change.)

### Set the key on the server
Same mechanism as the other env vars (see `CLAUDE.md`):

- **Local dev (IntelliJ):** add `ANTHROPIC_API_KEY` (and optionally `ANTHROPIC_MODEL`) to
  `~/.zshrc` **and** to `.run/Server.run.xml` (a Dock‑launched IDE doesn’t read `~/.zshrc`). Restart the
  Gradle daemon (`./gradlew --stop`) and re‑run the **Server** config.
- **Production (Heroku):** `heroku config:set ANTHROPIC_API_KEY=sk-ant-... ANTHROPIC_MONTHLY_LIMIT=500`

- [ ] **7. Set `ANTHROPIC_API_KEY`** on the server process.
- [ ] **8. Set `ANTHROPIC_MONTHLY_LIMIT`** to the max classify calls/month you’re willing to pay for
      (e.g. `500`). `0` disables classification entirely; unset = unlimited (not recommended in prod).

---

## B. Google Fact Check Tools — free key

- [ ] **1.** Go to <https://console.cloud.google.com> and create (or pick) a project.
- [ ] **2.** Enable the **Fact Check Tools API** (APIs & Services → *Library* → search “Fact Check
      Tools” → *Enable*).
- [ ] **3.** Create an **API key** (APIs & Services → *Credentials* → *Create credentials* → *API key*).
- [ ] **4.** (Recommended) **Restrict** the key to the Fact Check Tools API only.
- [ ] **5.** Set `FACT_CHECK_API_KEY` on the server (same mechanism as above).
- [ ] **6.** Optionally set `FACTCHECK_MONTHLY_LIMIT` (e.g. `1000`). It’s free within quota, so this is
      just a guard.

Without this key the classify flow still works — it simply won’t attach a fact‑check note.

---

## C. Verify it works (no spend)

- [ ] Start the server and hit the classify endpoint with your caps set to `0` first — you should get
      **429** and **no** provider call:
      ```
      curl -i -X POST http://localhost:8080/api/board/classify \
        -H 'Content-Type: application/json' \
        -d '{"text":"Necesito agua en Ibagué","country":"CO"}'
      ```
- [ ] Then raise `ANTHROPIC_MONTHLY_LIMIT` to a small number, paste a real post in the app, and confirm
      a **PENDING** entry appears in the moderation queue (with a fact‑check note if any matched).
- [ ] Paste the **same** text again and confirm it’s served from cache (no new Anthropic spend — the
      `api_usage` count for `anthropic` shouldn’t increase).

---

## Env var reference

| Var | Purpose | Unset behavior |
|---|---|---|
| `ANTHROPIC_API_KEY` | Enables classify | classify → 503 |
| `ANTHROPIC_MODEL` | Model id | cheap Haiku default |
| `ANTHROPIC_MONTHLY_LIMIT` | Monthly classify cap | unlimited |
| `FACT_CHECK_API_KEY` | Enables fact‑check note | no note (classify still works) |
| `FACTCHECK_MONTHLY_LIMIT` | Monthly fact‑check cap | unlimited |

Caps: `0` = disabled, `N>0` = N/month, unset/blank = unlimited. Counts reset each UTC month and persist
in the `api_usage` table.
