# SOEN 6611 Section D — Montréal Interac ABM team roles and responsibilities

## Instructor and teaching assistant

| Role | Name | Contact / note |
|---|---|---|
| Professor | **Pankaj Kamthan** | Course instructor, SOEN 6611 Section D. Announcement: *D1 Roles and Responsibilities* (Moodle Forums). |
| Teaching assistant | **Amr Abdalla** | `amrala304@gmail.com` — one team member emails the public URL of this page for D1. |

This public page is the team’s roles-and-responsibilities record required by the course outline:

> The roles and responsibilities for each team member should be made public, such as on social software, and should remain so for the entire duration of the project.

GitHub generates the commit and repository timestamps. We do not type a date by hand as the official record.

A Google Doc with the same D1 table is also kept for D1. From D2 onward, this GitHub repository is the only public roles page.

---

## About this project and its three phases

Our product is **Montréal Interac ABM**: a 21st-century, bank-owned indoor/vestibule **Automated Banking Machine** for Greater Montréal. It is Interac Debit, **CAD only**, bilingual EN/FR, and chip-and-PIN. This course build is a **simulator** (no live Interac, no real funds). USD, crypto ATMs, bill-pay, and operator replenishment stay out of scope for every phase.

The assignment PDF still names the project **iBank**. We keep that as the official course/project label so it matches the marking scheme and later Java packages. **Montréal Interac ABM** is the specific machine we selected in D1 and will implement.

Work is split into three related deliverables. Every phase uses the course PowerPoint template, GAI with CASTROFF, and an AI Interaction Log. D1 also cites non-original material (IEEE/ACM). The same ABM model is used from D1 through D3.

| Phase | Where | What the team delivers |
|---|---|---|
| **D1** | Zoom presentation | Choose this Canadian ABM (Problem 1), one SMART GQM goal and six questions (Problem 2), and a graphical + textual use-case model (Problem 3). |
| **D2** | Zoom + Java product | Implement and test Montréal Interac ABM (Problem 4), count Physical and Logical SLOC (Problem 5), and apply one authoritative readability metric (Problem 6). |
| **D3** | Classroom presentation | Cyclomatic complexity (Problem 7); WMC, CF, and LCOM* for each class (Problem 8); UCP and Basic COCOMO 81 vs actual effort (Problem 9); Logical SLOC vs WMC scatter and correlation (Problem 10). |

---

## Team

| Member | Role on this page |
|---|---|
| **Keyoumu Aisikeer** | Problem owner for D1 Problem 1; D2 Problem 4 (implementation); D3 Problem 7 |
| **Shubo Debnath** | Problem owner for D1 Problem 2; D2 tests + Problem 5 (SLOC); D3 Problem 8 |
| **Himanshu Banwal** | Problem owner for D1 Problem 3; D2 Problem 6 (readability); D3 Problems 9 and 10 |

All three members prepare the slides for that deliverable, keep camera and audio on for the full presentation, and attend for the entire slot. The instructor selects one presenter at random. Absence or camera-off means no credit for that member. The named **owner** is still responsible for that problem’s content if someone else is drawn to speak.

---

## D1 — roles (Problems 1–3)

These D1 roles were agreed by the team (WhatsApp discussion and two role-assignment images) **before** the D1 slides were submitted and presented. This repository is the **public** record requested after that presentation.

### Summary

| Member | D1 problem | Marks | What they own |
|---|---|---|---|
| Keyoumu Aisikeer | **Problem 1** | 20 | Select and describe one 21st-century **Canadian, lawful** ABM; list assumptions explicitly. |
| Shubo Debnath | **Problem 2** | 20 | One **SMART** GQM goal and **2N = 6** questions (N = 3); discuss whether metrics answer those questions. |
| Himanshu Banwal | **Problem 3** | 40 | Use-case model, **graphical and textual**, with definitions of actors and use cases. |

### Keyoumu Aisikeer — Problem 1 (ABM)

- Choose a kiosk suitable for Canada in the 21st century (municipal / provincial / federal law as applicable).
- Describe kind of machine, currency, and users.
- State assumptions in writing, including: CAD only; 4-digit PIN; lock after 3 failed PINs; $1,000 daily cash cap; $20 polymer notes; on-screen / simulated receipt; simulator only.
- Legal/context touchpoints used in the slides: Interac Debit, PIPEDA (mask PAN), Québec Charter of the French Language (EN/FR).
- Reject features that are not evidenced for this kiosk (crypto ATM, USD cassette, ABM e-Transfer).

### Shubo Debnath — Problem 2 (GQM)

- One goal only, written to be SMART, using the Basili GQM idea (Goal → Questions → Metrics).
- Team size **N = 3**, so **2N = 6** questions, each with a metric **M**.
- **Goal:** evaluate CAD cash-withdrawal effectiveness on Montréal Interac ABM for authenticated Canadian cardholders in Fall 2026.

| ID | Question | Metric |
|---|---|---|
| Q1 | What share of valid withdrawals complete? | M1 success ratio |
| Q2 | Median time from PIN-OK to cash shown? | M2 latency |
| Q3 | How do failures split (NSF / daily limit / not a $20 multiple)? | M3 exception counts |
| Q4 | PIN-locks per 1,000 authentications? | M4 lock rate (security proxy, not effectiveness itself) |
| Q5 | Is success similar in EN vs FR? | M5 success by language |
| Q6 | Share of amount screens cancelled? | M6 abandon rate (usability) |

- Discuss whether each metric actually answers its question. The set starts evaluation; it is not a full operations dashboard.
- Do not invent field data. Drop unmeasurable items (for example CSAT with no instrument).

### Himanshu Banwal — Problem 3 (use cases)

**How use cases were chosen:** P1 scope (this Canadian simulator) → only behaviours D2 can implement in Java → then draw the model. AI drafts that added Police, Operator, Interac Network, or 18 use cases including bill-pay were rejected.

**Four actors**

| Actor | Kind | Role |
|---|---|---|
| Cardholder | Primary, human | Starts sessions: cash, deposit, transfer, PIN, balance, language, receipt |
| Bank core | Secondary, system | Authorize PIN, post the ledger, enforce the daily limit |
| Cash dispenser | Secondary, device | Issue $20 notes after a successful withdrawal |
| Receipt printer | Secondary, device | Masked CAD receipt (on-screen in the simulator) |

**Ten use cases:** Select language; Authenticate (card + PIN); Inquire balance; Withdraw CAD; Deposit CAD; Transfer own accounts; Change PIN; Print / show receipt; End session; Lock card (after 3 bad PINs).

**Relations:** Lock card «extend» Authenticate; receipt «include» Withdraw. Other financial use cases take Authenticate as a precondition (not a web of include arrows).

### Shared D1 duties (whole team)

- Build and rehearse the 5-minute deck on the **course PowerPoint template** (Agenda names the three owners).
- Keep the written package: 5-minute slides + appendix (CASTROFF per problem, IEEE references, declaration) and the **AI Interaction Log**.
- Document **GAI use and non-use** (CASTROFF prompts, output, Accepted / Changed / Rejected). Non-use examples include the $1,000 cap, $20 unit, and 4-digit PIN (set from Problem 1 assumptions, not from generated “typical ATM” code).
- Keep P1, P2, and P3 as **one model** so unsupported actors, currencies, and transactions do not enter D2.

---

## D2 — planned roles (Problems 4–6)

From D2, source, metrics scripts, and this roles page live on **GitHub**. Slides still use the course PowerPoint template and may be stored in the same repository.

| Member | D2 problem | What they own |
|---|---|---|
| Keyoumu Aisikeer | **Problem 4 (a)** | Implement Montréal Interac ABM in Java (Swing GUI, OOP, exceptions, reuse, `javac` without an IDE). |
| Shubo Debnath | **Problem 4 (b) + Problem 5** | Representative tests; Physical SLOC and Logical SLOC (scheme stated; tests excluded from the count). |
| Himanshu Banwal | **Problem 6** | Select an authoritative readability metric, measure this product, comment on thresholds. |

Shared: product must stay CAD / bilingual / the D1 use-case set; CASTROFF + AI log for D2; one random presenter on Zoom.

---

## D3 — planned roles (Problems 7–10)

Classroom presentation. Same owners unless the team updates this file.

| Member | D3 problem | What they own |
|---|---|---|
| Keyoumu Aisikeer | **Problem 7** | Cyclomatic number of Montréal Interac ABM; comment vs published thresholds. |
| Shubo Debnath | **Problem 8** | WMC (weights not normalized), CF, and LCOM* for **each** class; comment vs thresholds. |
| Himanshu Banwal | **Problems 9 and 10** | UCP effort and Basic COCOMO 81 vs actual effort; scatter plot and correlation of Logical SLOC vs WMC. |

Shared: CASTROFF + AI log for D3; attendance mandatory for the full classroom slot.

---

## GAI / CASTROFF (every deliverable)

Each problem uses a public GAI tool. Prompts follow **CASTROFF** (Constraints, Audience, Structure, Tone, Role, Output, Focus, Function) as required in the project description (Tavakoli, *Prompt Engineering for Everyone*, 2026, ch. 6).

Every deliverable includes:

> I certify that all AI interactions for this submission are completely and accurately documented in this log. All content derived from or inspired by AI has been independently evaluated and modified as per AI Interaction Log.
