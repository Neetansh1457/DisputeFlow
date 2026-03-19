# ⚡ DisputeFlow
### Automated Dispute Document Processing System

> *65 cross-portal cases. 30 minutes of pure manual friction every day. DisputeFlow handles all 65 in under 5 minutes.*

---

## 📌 What Is This?

**DisputeFlow** is a backend-first, full-stack automation system designed to eliminate the manual effort involved in uploading chargeback dispute documents across multiple bank portals.

In financial operations teams, investigators are required to log into each bank's portal separately, locate a dispute by its unique identifier (which differs per bank), and upload a representation document — all manually. At scale, this creates significant daily friction: repeated context switching, portal session timeouts, and up to 30 minutes of pure administrative overhead that contributes nothing to case resolution.

DisputeFlow automates this entirely.

---

## 🧩 The Problem It Solves

### Current Reality (Without DisputeFlow)

An investigator handles **128 cases per day** at a rate of **16 cases per hour (IPH)**.

Each case involving a different bank portal requires:

| Step | Time |
|------|------|
| Open bank portal (2FA, navigation) | 2–3 minutes |
| Locate dispute by case/order ID | 1–2 minutes |
| Check dispute status manually | 1–2 minutes |
| Upload document or take action | 1–2 minutes |
| **Total per case** | **~5–8 minutes average** |

With ~65 cases requiring cross-portal handling per day, an experienced investigator doing all uploads in a single focused session (worst case):

```
65 cases — manual upload session (worst case) = ~30 minutes
Standard working shift (9 hours, 1 hour break) = 480 productive minutes

This is not about impossibility — it's about friction.
30 minutes of repetitive, error-prone, context-switching overhead
every single day, per investigator.
```

### ⚠️ The Real Problem

The issue is not that investigators cannot finish their caseload — they can. The issue is **how** they finish it. Manual cross-portal uploads mean:

- Repeated logins across multiple bank portals with 2FA every time
- Constant context switching that breaks focus and increases error rates
- 30 minutes of daily administrative overhead per investigator that adds zero investigative value

Bank portals compound this further — some portals **auto-close disputes within 5–10 minutes** of inactivity. An investigator working on Bank A who receives a Bank B case may return to find the portal session expired and the window gone.

---

### After DisputeFlow

| Step | Time |
|------|------|
| Drop 65 PDFs into batch upload | 2–3 minutes |
| System auto-detects bank + case ID | 0 minutes |
| System fetches dispute status + reason codes | 0 minutes |
| System processes ~85% of cases automatically | 0 minutes |
| Investigator reviews ~10 flagged edge cases | ~2 minutes |
| **Total for all 65 cases** | **~5 minutes** |

### 💡 Key Metrics

| Metric | Before | After |
|--------|--------|-------|
| Time for 65 cross-portal cases | ~30 minutes | ~5 minutes |
| Daily manual overhead per investigator | 30 minutes | ~5 minutes |
| Cases needing human attention | 65 | ~10 |
| Risk of portal session timeout | High | Near zero |
| Risk of upload error or missed case | Moderate | Near zero |
| Estimated time saved per investigator/day | — | 25 minutes |

### At Organisational Scale (300 Investigators)

```
25 minutes saved per investigator per day
× 300 investigators
= 7,500 minutes saved daily
= 125 hours recovered every single day
```

---

## 💰 Financial Impact

Beyond time, missed or failed dispute uploads carry direct financial consequences. Portal downtime, file errors, and SLA breaches mean disputes go uncontested — and uncontested disputes are lost disputes.

### Assumptions

- Average dispute value: **$20 (minimum) to $100 (average)**
- Cases lost to portal session timeouts, upload errors, or SLA breaches per investigator per day: **~5 cases**
- Investigators affected on any given day: **20% of 300 = 60 investigators**

### Per Investigator

| | Minimum ($20/dispute) | Average ($100/dispute) |
|-|----------------------|------------------------|
| Daily exposure | $100 | $500 |
| Weekly exposure (5 days) | $500 | $2,500 |

### Across the Organisation (60 affected investigators)

| Period | Minimum Loss | Average Loss |
|--------|-------------|--------------|
| Per week | $30,000 | $150,000 |
| Per month | $120,000 | $600,000 |
| Per year | $1,560,000 | $7,800,000 |

```
Conservative estimate: $1.6M in recoverable dispute value lost annually
Realistic estimate:    $7.8M in recoverable dispute value lost annually
```

These are not theoretical losses. They represent chargebacks that were not contested in time due to portal session timeouts, disputes that auto-closed before action was taken, and cases where manual upload errors meant a document never reached the bank.

**DisputeFlow directly addresses every failure point in this chain.**

---

## 🔍 How It Works — Non-Technical Overview

Think of DisputeFlow as a smart assistant that sits between your team and the bank portals.

1. **An investigator drops their case files** into the system — just like attaching files to an email.
2. **The system reads the filenames** and automatically figures out which bank each file belongs to and what the case number is.
3. **Before doing anything**, the system checks each case on the bank portal to understand what action is needed.
4. **Using pre-configured rules**, the system decides: does this case need a document uploaded? Does it need to be accepted? Is there a reversal? It acts accordingly — automatically.
5. **Anything the system is not sure about** gets flagged for the investigator to review. Everything else is handled without human input.
6. **The investigator sees a live dashboard** showing exactly what happened to every single case — success, failure, flagged, or pending.

No chasing portals. No context switching. No missed windows.

---

## 🏗️ Technical Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND                             │
│              React + Tailwind CSS Dashboard                 │
│    Upload Page │ Jobs Dashboard │ Audit Log │ Notifications │
└──────────────────────┬──────────────────────────────────────┘
                       │ REST API
┌──────────────────────▼──────────────────────────────────────┐
│                   SPRING BOOT API                           │
│              Java │ REST Controllers │ Job Queue            │
│         Bank Config Registry │ Rules Engine                 │
└────────┬─────────────────────────────┬───────────────────────┘
         │                             │
┌────────▼────────┐         ┌──────────▼──────────┐
│  KAFKA BROKER   │         │     POSTGRESQL       │
│  Event Queue    │         │  users               │
│  Partitioned    │         │  banks               │
│  by Bank ID     │         │  bank_reason_codes   │
└────────┬────────┘         │  upload_batches      │
         │                  │  upload_jobs         │
┌────────▼────────┐         │  audit_logs          │
│ KAFKA CONSUMER  │         │  notifications       │
│ Spring Boot     │         └──────────────────────┘
│ Thread Pool     │
│ (per bank)      │
└────────┬────────┘
         │
┌────────▼────────────────────────────────────────┐
│              PYTHON PROCESSING ENGINE            │
│   Filename Parser │ PDF Validator │ Rules Check  │
└────────┬────────────────────────────────────────┘
         │
┌────────▼────────────────────────────────────────┐
│              MOCK BANK APIs (FastAPI)            │
│  AMEX │ HSBC │ PMTC │ CHASE │ + more            │
│  Simulate: Success │ Failure │ Timeout │ Reversal│
└─────────────────────────────────────────────────┘
```

---

## ⚙️ Core Features

### 🔹 Smart File Detection
Files follow a naming convention (`AMEX_123456.pdf`). The system uses regex parsing to automatically extract the bank name and case ID from the filename — no manual entry needed. If detection fails, the frontend prompts the investigator to fill in the details manually.

### 🔹 Single & Batch Upload Modes
- **Single Mode** — Upload one file with full manual control. Best for edge cases the investigator has already reviewed.
- **Batch Mode** — Upload up to 100+ files at once. System handles everything automatically and flags only what it cannot resolve.

### 🔹 Rules-Based Decision Engine
Each bank has its own reason codes that determine what action to take on a dispute. These rules are stored in the database — not hardcoded — so adding or changing rules for a bank requires zero code changes.

| Bank | Reason Code | Action Taken |
|------|-------------|--------------|
| AMEX | S01 | Accept dispute (write off) |
| PMTC | 98 | Accept dispute (reversal) |
| Any | STANDARD | Upload representation document |
| Any | Unknown | Flag for manual review |

### 🔹 Parallel Processing with Thread Pools
Each bank gets its own dedicated thread pool. If one bank's portal is slow or unresponsive, other banks continue processing without interruption.

### 🔹 Event-Driven Architecture with Kafka
Upload jobs are published as events to a Kafka topic partitioned by bank ID. Spring Boot consumers process these events asynchronously, enabling high throughput and resilience.

### 🔹 Intelligent Retry with Exponential Backoff
If a bank portal is temporarily unavailable, the system automatically retries at increasing intervals (5 min → 15 min → 30 min) before marking a job as failed and notifying the investigator.

### 🔹 Compliance-Grade Audit Trail
Every action taken by the system or an investigator is logged with a timestamp, user ID, and description. Investigators can also manually override any auto-processed decision, which is logged separately for accountability.

### 🔹 Duplicate Detection
If a case ID for a given bank has already been submitted, the system rejects the duplicate and surfaces the existing job's status — preventing double processing.

### 🔹 Investigator Notifications
When a batch finishes processing, investigators receive an in-app notification with a summary of results — no need to keep checking the dashboard.

---

## 🛡️ Edge Cases Handled

| Scenario | How It's Handled |
|----------|-----------------|
| File too large or wrong type | Rejected immediately at upload with clear error message |
| Corrupted PDF | Detected during processing, flagged for review, original file preserved |
| Bank portal is down | Exponential backoff retry, other banks unaffected, job marked PENDING_RETRY |
| Partial batch failure | Each job tracked independently, dashboard shows full breakdown |
| Duplicate submission | Detected via unique case_id + bank_id constraint, rejected with status of existing job |
| Unknown reason code | Flagged automatically for investigator review |
| Auto-processed incorrectly | Manual override available, override logged in audit trail |
| Kafka unavailable | Fallback poller processes PENDING jobs directly until Kafka recovers |

---

## 🗄️ Database Schema

```
users
  │
  └──< upload_batches
  │         │
  └──< upload_jobs >──── banks >──── bank_reason_codes
            │
            └──< audit_logs
            └──< notifications
```

**7 tables total.** Designed for clarity, compliance, and extensibility.

---

## 🔌 API Overview

| Category | Endpoints |
|----------|-----------|
| Users | Register, get user, list all |
| Banks | List, get, add, update, deactivate |
| Upload | Single upload, batch upload, batch preview (dry run) |
| Jobs | List, get, retry, cancel |
| Batches | List, get batch with all jobs |
| Audit Logs | List all, get by job |
| **Total** | **19 endpoints** |

All endpoints follow REST conventions under `/api/v1/` with query parameter filtering support.

---

## 📈 Scalability

DisputeFlow is designed to scale from a small team to an enterprise operation without architectural changes.

| Bottleneck | Solution |
|------------|----------|
| High file volume | DigitalOcean Spaces / S3 for object storage |
| Database query performance | Indexed on status, bank_id, case_id, created_at |
| Kafka throughput | Topic partitioned by bank_id for parallel consumption |
| Portal response variability | Separate thread pool per bank, configurable size |
| Server capacity | Stateless Spring Boot — horizontal scaling behind load balancer |

**At 300 investigators × 65 files = 19,500 files/day**, a single well-configured server handles this comfortably with the optimisations above in place.

---

## 🧰 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend API | Java 17, Spring Boot 3 |
| Processing Engine | Python 3, FastAPI |
| Message Broker | Apache Kafka |
| Database | PostgreSQL |
| Frontend | React, Tailwind CSS |
| Containerisation | Docker, Docker Compose |
| Deployment | DigitalOcean |

---

## 🚀 Getting Started

> Full setup instructions coming as the project progresses.

```bash
# Clone the repository
git clone https://github.com/yourusername/disputeflow.git

# Start infrastructure (Kafka + PostgreSQL)
docker-compose up -d

# Start Spring Boot API
./mvnw spring-boot:run

# Start Python processing engine
cd processor && python main.py

# Start frontend
cd frontend && npm install && npm run dev
```

---

## 📋 Project Roadmap

- [x] System design & architecture
- [x] Database schema design
- [x] API endpoint design
- [x] Project setup (Spring Boot + PostgreSQL + Kafka)
- [ ] Core backend — entities, repositories, REST endpoints
- [ ] Kafka integration — producer + consumer
- [ ] Python processing engine — filename parser + PDF validator
- [ ] Thread pool implementation — parallel bank processing
- [ ] Mock bank APIs — FastAPI simulations
- [ ] React dashboard — upload, jobs, audit log, notifications
- [ ] Retry mechanism + edge case handling
- [ ] Unit + integration tests
- [ ] Dockerize full stack
- [ ] Deploy to DigitalOcean

---

## 🤔 Why This Project?

This system was designed based on firsthand experience of the manual dispute processing workflow in a financial operations environment. The inefficiencies are real, the edge cases are real, and the financial exposure is measurable.

The goal was not just to build a file upload tool — but to design a production-grade backend system that could genuinely be deployed in an organisation and make an immediate, quantifiable impact on investigator productivity and revenue recovery.

---

*Built by Neetansh — open to feedback, contributions, and conversations.*
