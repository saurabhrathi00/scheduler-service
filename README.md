# Scheduler Service

## Overview

The **Scheduler Service** is responsible for **dispatching due jobs** for execution.  
It continuously scans the Job Database for jobs that are ready to run and publishes execution events to Kafka.

The scheduler does **not execute jobs** itself.  
It only decides **when a job should be dispatched** and hands it off to the execution layer.

---

## Responsibilities

- Periodically scan the Job Database for due jobs
- Select jobs with `status = SCHEDULED` and `nextRunAt <= now()`
- Dispatch eligible jobs for execution
- Publish job execution events to Kafka
- Control dispatch rate using batching, retries, and sleep logic

---

## Startup Behavior

- The scheduler **always runs once on application startup**
- This ensures that:
    - Any pending jobs are immediately picked up
    - Jobs are not missed due to service restarts

After the initial run, the scheduler continues execution in a loop.

---

## Job Selection Logic

On each scheduler cycle:

- Jobs are selected from the Job Database where:
`  status = SCHEDULED
  AND next_run_at <= current time`

- Jobs are fetched in batches to control load
- Database-level locking is used to avoid duplicate dispatch across multiple scheduler instances

---

## Dispatch Mechanism

### Dispatch Strategy

- Currently, the scheduler uses a **Kafka-based dispatch implementation**
- For each selected job:
- A Kafka-compatible `JobEvent` is created
- The event is published to the configured Kafka topic

The dispatch mechanism is intentionally abstracted to allow future extensions (e.g., alternate dispatch strategies).

---

## Inline Retry Handling

- The scheduler supports **inline retries** during dispatch
- If dispatch fails:
- The scheduler retries up to **3 times**
- A backoff is applied between retries

This protects against temporary Kafka or network issues without overwhelming the system.

---

## Sleep & Backoff Strategy

After dispatching jobs, the scheduler computes how long it should sleep before the next cycle.

### Sleep Time Calculation

- The scheduler queries the Job Database for the **minimum `nextRunAt`** among jobs with `status = SCHEDULED`
- Sleep time is calculated based on how far the next job is scheduled in the future

### Sleep Capping

- To avoid long idle periods, sleep duration is capped
- The scheduler will **never sleep for more than 30 seconds**, even if the next job is scheduled further in the future

This ensures:
- Responsiveness to newly created jobs
- Predictable scheduling behavior

---

## Configuration Management

The Scheduler Service is fully configuration-driven.

### Configurable Properties

The following properties are externalized via `service.properties`:

- Batch size for job selection
- Maximum sleep duration
- Base backoff duration for inline retries

Certain internal safety limits (such as maximum inline retries) are intentionally kept as code-level constants.

---

## Running the Service Locally

To run the Scheduler Service locally, configure the environment variable:

```bash
export SPRING_CONFIG_LOCATION=/absolute/path/to/service.properties,/absolute/path/to/secrets.properties
```

Then start the service normally.