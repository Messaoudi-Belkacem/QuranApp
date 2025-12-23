# Proposal – Full Migration & Re‑Deployment of OTT CRM

## 1. Context

You already have a working **OTT CRM** product with:

- **Frontend**: React 18 + TypeScript + Vite, Tailwind CSS, shadcn/ui  
- **Backend**: Supabase (PostgreSQL + Edge Functions Deno)  
- **Payments**: Stripe (Checkout Sessions + Webhooks)  
- **Emails**: Resend + SMTP/IMAP + Gmail OAuth  
- **Client portal**: `/client/*` (dashboard, renewals, referrals, support, etc.)

You have also provided a very detailed **technical migration dossier** describing:

- Architecture and data model (subscriptions, client accounts, resellers, emails, workflows)
- RLS and dual authentication (admin vs client)
- Edge Functions catalogue
- Full **migration plan** (exports, rebuild, tests, cutover, rollback)

My mission will be to:

- Take over the existing codebase
- Perform a **full migration** to a new Supabase environment
- Re‑deploy the **backend (Supabase + Edge Functions)** and **frontend**
- Re‑configure **Stripe** and **email workflows**
- Stabilize and test all **critical business flows**

---

## 2. Scope of Work

The scope is aligned with your technical document.  
It is **migration + stabilization**, not a redesign or feature expansion.

### 2.1. Audit & Takeover

- Review the codebase structure (`src/`, `supabase/functions/`, integrations)
- Understand the data model:
  - `subscriptions`, `client_accounts`, `client_subscriptions`
  - `payment_links`, `payments`, `pending_credentials`
  - email tables, workflows, resellers
- Verify alignment between the documentation and the actual schema/RLS
- Identify main risk areas (Stripe, RLS, email deliverability, portal client)

---

### 2.2. Supabase Migration (Schema + RLS)

- Export from current project:
  - Schema SQL (`schema.sql`)
  - RLS policies, roles and grants
  - Optional data export for key business tables if needed
- Create and configure the **new Supabase project**
- Import:
  - Tables, constraints, indexes, triggers, extensions
- Re‑apply and verify:
  - RLS on all application tables
  - Policies for admin, clients, demo users
- Security checks on sensitive tables:
  - `payment_links`, `payments`, `pending_credentials`, `email_conversations`, etc.

---

### 2.3. Edge Functions & Stripe

- Re‑deploy all relevant Edge Functions (Deno), for example:
  - `stripe-webhook`
  - `create-stripe-payment` / `create-multi-stripe-payment`
  - `check-reminders`, `process-email-queue`
  - `send-credentials`, `send-magic-link`
  - `check-imap-replies`, `gmail-push-webhook`
- Configure secrets (Stripe, Resend, Google, Supabase service role)
- Configure CORS for the new frontend origin(s)
- Reconfigure Stripe:
  - API keys
  - Webhook endpoint to the new `stripe-webhook`
  - Product/price mapping if necessary
- End‑to‑end payment flow tests:
  - Link → Stripe Checkout → webhook → subscription update
  - Creation of `payments` and `pending_credentials`
  - Idempotency (no duplicate records when an event is retried)

---

### 2.4. Email System & Reminders

- Reconfigure email senders:
  - Resend
  - SMTP/IMAP accounts
  - Gmail OAuth (where used)
- Validate workflows and cron logic:
  - `reminder_workflows`, `workflow_steps`
  - `email_queue`, `reminder_history`, `email_conversations`
  - Scheduled functions: `check-reminders`, `process-email-queue`, inbound sync
- Basic deliverability hygiene:
  - Use your existing warmup / cadence strategy
  - Ensure we do not introduce regressions in volume or sending behaviour

---

### 2.5. Frontend, Client Portal & Cutover

- Update frontend environment variables:
  - `VITE_SUPABASE_URL`
  - `VITE_SUPABASE_PUBLISHABLE_KEY`
  - `VITE_SUPABASE_PROJECT_ID`
- Rebuild & redeploy (existing pipeline: Lovable or other)
- Functional testing of:
  - Admin side: subscriptions, payment links, resellers, credits
  - Client portal `/client/*`:
    - Authentication (magic link / login)
    - Subscription view and renewal
    - Downloads, referrals, support contact
- Non‑regression & cutover:
  - Run test scenarios on the new environment
  - Switch DNS / environment variables / Stripe webhook to the new project
  - Short monitoring period to catch any regression

---

## 3. Time & Pricing

### 3.1. Time Estimate

I estimate a total of:

> **40 working hours**

This includes:

- Audit and takeover  
- Supabase migration (schema + RLS)  
- Edge Functions and Stripe re‑deployment  
- Email workflow / reminders / inbound configuration  
- Frontend re‑deployment and testing  
- Non‑regression tests and cutover support  

### 3.2. Rate & Total

My rate for this mission:

> **20 € / hour**

For **40 hours**, the total proposed budget is:

> **Total fixed price: 800 €**

This is a **fixed price**, based on the 40‑hour estimate.  
If we both agree, we will keep this as a **reference scope**. Extra work beyond this scope (for example, new features) will be estimated and billed separately.

---

## 4. What Is Included

- Full migration to a new Supabase project (as described in your dossier)
- Re‑deployment of Edge Functions with proper secrets and CORS
- Re‑configuration and testing of Stripe flows
- Re‑configuration of email senders, queues and reminder workflows
- Frontend re‑deployment with new environment variables
- Non‑regression tests on:
  - Payments and subscriptions
  - Client portal access
  - Email reminders
- Support during the cutover period (monitoring and fixing blocking issues)

Suggested wording:

> “Scope is full migration, stabilization and redeployment of the existing OTT CRM, based on the current documentation and features.”

---

## 5. Next Steps

If you agree with this proposal:

1. We confirm:
   - The **fixed budget: 800 €**
   - The **approximate calendar window** for the migration and cutover
2. You provide access to:
   - App codebase
   - Current Supabase project
   - Stripe account (keys and webhook management)
   - Email providers (Resend, SMTP/IMAP, Gmail OAuth)
3. I start with:
   - A short **audit and export phase**
   - Creation of the new Supabase project
   - Step‑by‑step migration, testing, and then cutover

---

**Signature**

Developer: __________________________  
Date: __________________________  
Agreed fixed price: **800 €**  
