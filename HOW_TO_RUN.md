# iBank (Montréal Interac ABM) – Problem 4

A Java Swing simulator of a bilingual (English/French) Automated Banking Machine for a
bank branch in Montréal. SOEN 6611, Fall 2026, Team A.

## Requirements

- JDK 17 or newer (`java -version`, `javac -version`). No IDE, build tool or database is needed.
- For the tests only: `lib/junit-platform-console-standalone-<version>.jar` (JUnit 5).

## Build, test, run

Run every command from the project folder (the one that contains `src` and `data`).

| Task | Linux / macOS | Windows |
|---|---|---|
| Build | `./build.sh` | `build.bat` |
| Run all JUnit tests | `./test.sh` | `test.bat` |
| Start the ABM | `./run.sh` | `run.bat` |
| Start again from the beginning | `./reset.sh` | `reset.bat` |

Without the scripts: compile all files in `src` with `javac --release 17 -d out`, copy
`src/ibank/i18n/*.properties` to `out/ibank/i18n/`, then run `java -cp out ibank.Main`.

## Test cards (data/cardholders.csv)

| Card number | PIN | Cardholder | Useful for |
|---|---|---|---|
| 4500000000001111 | 1111 | Amira Haddad | Everyday use, two accounts, transfers |
| 4500000000002222 | 2222 | Etienne Gagnon | Low balance ($35): insufficient funds |
| 4500000000003333 | 3333 | Sofia Tremblay | High balance: the $1,000 daily limit |
| 4500000000004444 | 4444 | Marc Nguyen | One account only |

These are test data only, not real people or cards.

## Features (D1 use cases)

Select language (EN/FR) · Authenticate · Lock card after 3 wrong PINs · Check balance ·
Withdraw cash (fewest-notes suggestion, customer can change the note mix) · Deposit cash ·
Transfer between own accounts · Change PIN · Receipt (always after a withdrawal, offered
after a deposit or transfer) · End session (button, or automatically after 60 s idle).

## Rules (all set in `ibank.config.AtmConfig`)

| Rule | Value |
|---|---|
| Notes dispensed | $5, $10, $20, $50, $100 (as at RBC branch ATMs) |
| Daily withdrawal limit | $1,000 |
| Wrong PINs before the card locks | 3 |
| PIN length | 4 digits |
| Session ends after | 60 seconds without activity |
| Cash deposits | Whole notes only, so multiples of $5 |

## Saved state

After every action, iBank saves balances, notes left in the machine, card locks, changed
PINs (as salted hashes) and transactions to `data/state/`. The next start continues from
there, so restarting cannot reset the daily limit. `data/cardholders.csv` is never changed.

A locked card cannot be unlocked at the machine (as at a real ABM). For testing, edit its
line in `data/state/cards.csv` to end with `,0,false`, or run the reset script.

## Project structure

```text
src/ibank/
  model/        Money, Account, Card, Cardholder, Transaction
  config/       AtmConfig: every business rule in one place
  device/       BankCore, CashDispenser, ReceiptPrinter (D1 secondary actors), CSV loader
  service/      AuthenticationService, TransactionService, AtmSession
  exception/    AtmException and one subclass per cardholder error
  security/     PinHasher (salted SHA-256)
  i18n/         Messages + messages_en/fr.properties
  persistence/  StateStore (saved state between runs)
  gui/          AtmFrame and one Swing screen per use case
  Main.java
test/ibank/     JUnit 5 tests (not part of the SLOC count)
data/           cardholders.csv (starting data); state/ is created at run time
```

## Quality checks

- 67 JUnit 5 tests, all passing (`./test.sh`).
- Style: Google Java Style, checked with Checkstyle (`google_checks.xml`): 0 warnings.
