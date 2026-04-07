# Project Performance Experiments Write-up

## Experiment 2: Measuring Transaction Time, CPU Use, and Free Memory for Projects (Create)

**Setup:** The system starts with 0 projects. At iteration *i*, there are already *i* projects present. One new project with randomly generated title, description, completed, and activeStatus is POSTed to `/projects`. The round-trip time is recorded. After all 500 iterations, `@AfterAll` deletes all created projects.

### a. With respect to sample time

**Insert Figure 3:** Project Create — transaction time, CPU use, and heap memory vs. sample time

As seen in Figure 3, transaction time for adding a project stabilizes after early warm-up samples and remains broadly consistent throughout the run. This is expected: a POST to `/projects` is a direct insert path and does not require scanning existing records. CPU use should remain in a narrow band with occasional spikes caused by background OS/JVM activity. Heap memory generally trends upward over time because newly created project objects are retained in memory until cleanup.

### b. With respect to number of projects

**Insert Figure 4:** Project Create — transaction time, CPU use, and heap memory vs. number of projects

Consistent with Figure 3, create latency remains largely stable as project count grows from 0 to 499, indicating no clear size-dependent slowdown on insert. CPU use does not show a sustained growth pattern with object count. Heap memory shows overall growth with the number of stored projects, which matches expected in-memory retention behavior.

---

## Experiment 3: Measuring Transaction Time, CPU Use, and Free Memory for Projects (Update)

**Setup:** The system starts by creating one target project. At iteration *i*, there are *i* total projects in memory (the target plus filler projects). The same target project is updated using a newly generated payload via PUT to `/projects/{id}`. Round-trip time is recorded for each update. After each iteration (except the last), one filler project is added so object count increases up to 500. `@AfterAll` deletes all created projects.

### a. With respect to sample time

**Insert Figure 5:** Project Update — transaction time, CPU use, and heap memory vs. sample time

As seen in Figure 5, update transaction time settles after initial warm-up and remains relatively steady across samples. This behavior is expected when updating by ID, where lookup/update cost is approximately constant for each request. CPU use should remain mostly stable with intermittent short spikes attributable to runtime noise (GC, scheduling, background processes). Heap memory exhibits a sawtooth pattern with an overall upward tendency due to filler-project growth, while periodic garbage collection causes temporary drops.

### b. With respect to number of projects

**Insert Figure 6:** Project Update — transaction time, CPU use, and heap memory vs. number of projects

Consistent with Figure 5, update latency does not show strong degradation as the number of projects increases to 500, supporting the expectation of near-constant-time ID-based updates. CPU use remains broadly flat as object count grows. Heap memory does not increase perfectly linearly because JVM garbage collection periodically reclaims/compacts memory, but the general behavior is consistent with increasing live data.

---

## Experiment 4: Measuring Transaction Time, CPU Use, and Free Memory for Projects (Delete)

**Setup:** Before measurement, 500 projects are pre-created. At iteration *i*, there are *i* projects in the system. One existing project is deleted via DELETE to `/projects/{id}`, and round-trip time is recorded. This continues until all projects are removed. `@AfterAll` attempts to delete any remaining projects.

### a. With respect to sample time

**Insert Figure 7:** Project Delete — transaction time, CPU use, and heap memory vs. sample time

As seen in Figure 7, delete transaction time is generally stable with occasional outliers. This is expected for ID-based deletion in an in-memory store, where typical work per request is small and constant, while sporadic pauses may come from garbage collection or OS scheduling. CPU use is expected to remain in a modest band with transient spikes. Heap memory typically shows periodic drops and recoveries rather than a smooth decline, since JVM memory reclamation is batch-driven and not immediate after each delete.

### b. With respect to number of projects

**Insert Figure 8:** Project Delete — transaction time, CPU use, and heap memory vs. number of projects

Consistent with Figure 7, delete latency does not exhibit strong worsening as project count decreases from 500 to 1; instead, it stays mostly flat with occasional jitter. CPU use remains without a clear monotonic trend against object count. Heap memory is non-monotonic due to GC behavior, but overall memory pressure should reduce as the workload transitions toward fewer live objects.
