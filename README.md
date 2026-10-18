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

Team A has **two members**. Work is split so each phase is about **40 / 40** (D1), a shared product plus two measurements (D2), and **50 / 50** marks (D3).

| Member | D1 | D2 | D3 |
|---|---|---|---|
| **Keyoumu Aisikeer** | Problems 1 and 2 (40 marks) | Problem 4 (a) implement + Problem 5 SLOC | Problems 7 and 8 (50 marks) |
| **Himanshu Banwal** | Problem 3 (40 marks) | Problem 4 (b) tests + Problem 6 readability | Problems 9 and 10 (50 marks) |

Both members prepare the slides for that phase, keep camera and audio on for the full presentation, and attend for the entire slot. The instructor selects one presenter at random. The named owner remains responsible for that problem’s content.

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

| Member | Problem | Marks | Responsibility |
|---|---|---|---|
| Keyoumu Aisikeer | **Problems 1 and 2** | 20 + 20 | Canadian ABM selection and assumptions; one SMART GQM goal and **2N = 4** questions. |
| Himanshu Banwal | **Problem 3** | 40 | Use-case model, graphical and textual, with definitions of actors and use cases. |

**Problem 1 (Keyoumu Aisikeer).** Choose a kiosk suitable for Canada in the 21st century. Describe the machine, currency, and users. State assumptions (CAD only; 4-digit PIN; lock after 3 failed PINs; $1,000 daily cap; $20 notes; simulator only). Reject crypto ATM, USD cassette, and ABM e-Transfer.

**Problem 2 (Keyoumu Aisikeer).** One SMART goal using GQM. Team size **N = 2**, so **2N = 4** questions. Goal: evaluate CAD cash-withdrawal effectiveness on Montréal Interac ABM for authenticated Canadian cardholders in Fall 2026.

| ID | Question | Metric |
|---|---|---|
| Q1 | What share of valid withdrawals complete? | M1 success ratio |
| Q2 | Median time from PIN-OK to cash shown? | M2 latency |
| Q3 | How do failures split (NSF / daily limit / not a $20 multiple)? | M3 exception counts |
| Q4 | Share of amount screens cancelled? | M4 abandon rate (usability) |

Q1–Q3 answer the SMART goal directly. Q4 is a usability construct, not a full operations dashboard. Extra questions used in an earlier three-person draft were dropped so the set matches N = 2.

**Problem 3 (Himanshu Banwal).** Use cases follow Phase 1 scope and what Phase 2 can implement. Four actors: Cardholder (primary); Bank core, Cash dispenser, Receipt printer (secondary). Ten use cases: Select language; Authenticate; Inquire balance; Withdraw CAD; Deposit CAD; Transfer own accounts; Change PIN; Print / show receipt; End session; Lock card. Lock card «extend» Authenticate; receipt «include» Withdraw.

**Shared.** Course PowerPoint template; CASTROFF; AI Interaction Log; IEEE/ACM citations for Phase 1; one model so unsupported actors and transactions do not enter Phase 2.

### Phase 2 (D2)

Public D2 roles document: [`D2_ROLES.md`](D2_ROLES.md)

| Member | Problem | Marks (approx.) | Responsibility |
|---|---|---|---|
| Keyoumu Aisikeer | **Problem 4 (a) + Problem 5** | implementation + 20 | Java Swing product (`javac`, OOP, exceptions, reuse); Physical and Logical SLOC on production source. |
| Himanshu Banwal | **Problem 4 (b) + Problem 6** | tests + 20 | Representative tests; one authoritative readability metric, numbers, and thresholds. |

Shubo’s former D2 work (tests + SLOC) is split: SLOC stays with the implementer; tests stay with the measurement / quality owner.

### Phase 3 (D3)

| Member | Problem | Marks | Responsibility |
|---|---|---|---|
| Keyoumu Aisikeer | **Problems 7 and 8** | 20 + 30 | Cyclomatic number; WMC (unnormalized), CF, and LCOM* for each class; comment vs thresholds. |
| Himanshu Banwal | **Problems 9 and 10** | 20 + 30 | UCP and Basic COCOMO 81 vs actual effort; scatter plot and correlation of Logical SLOC vs WMC. |

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
