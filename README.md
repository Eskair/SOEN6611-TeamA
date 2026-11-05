# Montréal Interac ABM

## Instructor and teaching assistant

| Role | Name | Contact |
|---|---|---|
| Professor | Pankaj Kamthan | SOEN 6611, Concordia University |
| Teaching assistant | Amr Abdalla | amrala304@gmail.com |

## About the project and its three phases

A 21st-century **Canadian Interac Automated Banking Machine** simulator for Greater Montréal: CAD only, bilingual EN/FR, chip-and-PIN. This is a course simulator (no live Interac, no real funds). USD, crypto ATMs, bill-pay, and operator replenishment are out of scope.

The SOEN 6611 project description still names the project **iBank**. We keep that as the official course label (and later Java package name). **Montréal Interac ABM** is the specific machine we selected and will implement.

The work is one product, delivered in three related phases. Every phase uses the course PowerPoint template, GAI with CASTROFF, and an AI Interaction Log. Phase 1 also cites non-original material (IEEE/ACM). The same ABM model is used from start to finish.

Team size is **N = 2**, so GQM uses **2N = 4** questions.

| Phase | Setting | What we deliver |
|---|---|---|
| **Phase 1 (D1)** | Zoom | Choose this Canadian ABM (Problem 1), one SMART GQM goal and four questions (Problem 2), and a graphical + textual use-case model (Problem 3). |
| **Phase 2 (D2)** | Zoom + Java product | Implement and test Montréal Interac ABM (Problem 4), count Physical and Logical SLOC (Problem 5), and apply one authoritative readability metric (Problem 6). |
| **Phase 3 (D3)** | Classroom | Cyclomatic complexity (Problem 7); WMC, CF, and LCOM* for each class (Problem 8); UCP and Basic COCOMO 81 vs actual effort (Problem 9); Logical SLOC vs WMC scatter and correlation (Problem 10). |

Phase 1 Zoom presentation (revised deck): [`D1/SOEN6611_D1_Revised-present.pptx`](D1/SOEN6611_D1_Revised-present.pptx)

## Features and scope

- Bank-owned indoor / vestibule ABM, Interac Debit, polymer $20 notes
- Cardholder flows: language, authenticate, balance, withdraw, deposit, transfer, change PIN, receipt, end session, lock card after three failed PINs
- Assumptions: 4-digit PIN; $1,000 daily cash cap; on-screen / simulated receipt
- Legal/context touchpoints: PIPEDA (mask PAN); Québec Charter of the French Language (EN/FR)

## Team

Team A has **two members**. The current division of responsibilities is recorded below.

| Phase | Primary responsibility | Review, revisions, and additions |
|---|---|---|
| **D1** | **Keyoumu Aisikeer** leads completion of Problems 1–3 and the D1 materials. | **Himanshu Banwal** reviews, corrects, and supplements the work. |
| **D2** | **Himanshu Banwal** leads completion of Problems 4–6 and the D2 materials. | **Keyoumu Aisikeer** reviews, tests, corrects, and supplements the work. |
| **D3** | **Keyoumu Aisikeer** and **Himanshu Banwal** each complete half of the D3 tasks. | Each member reviews, corrects, and supplements the other member's work. |

The specific D3 task allocation will be recorded when agreed. Both members prepare slides and understand the full presentation. Actual contributions and effort are recorded as the work progresses.

## Collaboration

**Team A** planned the project and assigned roles on **WhatsApp**. Phase 1 (D1) drafts and revisions were made on **Google Drive / Google Slides / Google Docs**. Screenshots in [`Image`](Image/) include earlier discussion; **current roles are the two-person split on this page**.

Live D1 decks (Google Slides):

- [D1_Complete](https://docs.google.com/presentation/d/1SdPOP0wPD5SaT6SHaSDFhV2KVu9qTv__/edit?usp=sharing)
- [D1 working slides](https://docs.google.com/presentation/d/1TxVz2F2qIw-Wxc3rS3ZihO5pKqQptQKh1OJaI7qm1yA/edit?usp=sharing)

| Screenshot | Content |
|---|---|
| [01](Image/01-whatsapp-group-created.png) | WhatsApp group created |
| [02](Image/02-proposed-d1-roles.png) | First proposed D1 roles |
| [03](Image/03-first-meeting-tasks.png) | First meeting tasks |
| [04](Image/04-meeting-and-problem3.png) | Meeting and Problem 3 |
| [05](Image/05-google-doc-smart-goals.png) | Google Doc SMART-goal draft |
| [06](Image/06-agenda-speaking-assignment.png) | Agenda speaking assignment |
| [07](Image/07-d1-d2-d3-roles-table.png) | Earlier D1 / D2 / D3 owner table |
| [08](Image/08-google-slide-and-doc.png) | Google Slides and Google Doc |
| [09](Image/09-d1-complete-slides-link.png) | Shared D1_Complete link |
| [10](Image/10-d1-roles-agreed.png) | Earlier D1 roles agreed |
| [11](Image/11-review-castroff-prompts.png) | Review of CASTROFF prompts |
| [12](Image/12-google-drive-d1-files.png) | Google Drive: D1 revised deck and AI log |

## Roles and responsibilities

### Phase 1 (D1)

| Member | Responsibility |
|---|---|
| Keyoumu Aisikeer | Lead completion of Problems 1–3 and the D1 materials. |
| Himanshu Banwal | Review, revise, and supplement the D1 materials. |

**Problem 1 (Keyoumu Aisikeer).** Choose a kiosk suitable for Canada in the 21st century. Describe the machine, currency, and users. State assumptions (CAD only; 4-digit PIN; lock after 3 failed PINs; $1,000 daily cap; $20 notes; simulator only). Reject crypto ATM, USD cassette, and ABM e-Transfer.

**Problem 2 (Keyoumu Aisikeer).** One SMART goal using GQM. Team size **N = 2**, so **2N = 4** questions. Goal: evaluate CAD cash-withdrawal effectiveness on Montréal Interac ABM for authenticated Canadian cardholders in Fall 2026.

| ID | Question | Metric |
|---|---|---|
| Q1 | What share of valid withdrawals complete? | M1 success ratio |
| Q2 | Median time from PIN-OK to cash shown? | M2 latency |
| Q3 | How do failures split (NSF / daily limit / not a $20 multiple)? | M3 exception counts |
| Q4 | Share of amount screens cancelled? | M4 abandon rate (usability) |

Q1–Q3 answer the SMART goal directly. Q4 is a usability construct, not a full operations dashboard. Extra questions used in an earlier three-person draft were dropped so the set matches N = 2.

**Problem 3 (Keyoumu Aisikeer; reviewed and supplemented by Himanshu Banwal).** Use cases follow Phase 1 scope and what Phase 2 can implement. Four actors: Cardholder (primary); Bank core, Cash dispenser, Receipt printer (secondary). Ten use cases: Select language; Authenticate; Inquire balance; Withdraw CAD; Deposit CAD; Transfer own accounts; Change PIN; Print / show receipt; End session; Lock card. Lock card «extend» Authenticate; receipt «include» Withdraw.

**Shared.** Course PowerPoint template; CASTROFF; AI Interaction Log; IEEE/ACM citations for Phase 1; one model so unsupported actors and transactions do not enter Phase 2.

### Phase 2 (D2)

Public D2 roles document: [`D2_ROLES.md`](D2_ROLES.md)

| Member | Responsibility |
|---|---|
| Himanshu Banwal | Lead completion of Problems 4–6, including implementation, representative testing, SLOC measurement, readability analysis, and D2 materials. |
| Keyoumu Aisikeer | Review the implementation and materials, independently run tests, identify and fix issues, and add supporting tests or analysis. |

Keyoumu's D2 review contribution includes fixing the daily withdrawal limit check before note selection and adding a regression test. The resulting suite passed all 68 tests on Windows with JDK 21. See [PR #1](https://github.com/Eskair/SOEN6611-TeamA/pull/1) for the code, validation, and AI assistance disclosure.

### Phase 3 (D3)

Each member completes half of the D3 tasks covering Problems 7–10. The exact task allocation will be recorded when agreed. Each member reviews, corrects, and supplements the other member's work before the team integrates the results and presentation.

## Tech stack

| Area | Choice |
|---|---|
| Language | Java (source independent of any one IDE) |
| UI | Swing |
| Build / run | `javac` / scripts (`compile.ps1`, `run.ps1`, `run-tests.ps1`) |
| Measurement | `metrics.py` (SLOC, readability, cyclomatic, OO metrics) |
| Slides | Course Microsoft PowerPoint template |

## Getting started

Phase 2 source will live in this repository. After it is published:

```text
.\compile.ps1
.\run-tests.ps1
.\run.ps1
```

The Phase 1 presentation is already in [`D1/`](D1/). Phase 2 source will be added here later.

## Project structure

```text
SOEN6611-TeamA/
  README.md                                 ← this file
  D2_ROLES.md                               ← D2 roles and responsibilities
  D1/SOEN6611_D1_Revised-present.pptx       ← Phase 1 Zoom presentation
  Image/                                    ← WhatsApp and Google Drive / Slides screenshots
  (D2) src/ test/                           ← Java product and tests (to be added)
  (D2) tools/                               ← measurement scripts (to be added)
```

## GAI and CASTROFF

Each problem uses a public GAI tool. Prompts follow **CASTROFF** (Constraints, Audience, Structure, Tone, Role, Output, Focus, Function).

> I certify that all AI interactions for this submission are completely and accurately documented in this log. All content derived from or inspired by AI has been independently evaluated and modified as per AI Interaction Log.

Non-use of GAI includes the $1,000 daily cap, the $20 dispense unit, and the 4-digit PIN. Those come from Phase 1 assumptions, not from generated sample ATM code.
