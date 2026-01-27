<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>

<h1>Multi-Level Workflow Approval System</h1>

<p><strong>Target Role:</strong> Junior Backend / Java Developer (Remote-friendly)</p>

<p><strong>Project Focus:</strong> Complex business logic, state machine implementation, and hierarchical authorization beyond CRUD operations.</p>

<p><strong>Tech Stack:</strong> Spring Boot 3.x | Spring Security 6 | JWT | PostgreSQL | AWS (EC2/RDS) | Spring Scheduler</p>

<hr>

<h2>TL;DR (For Recruiters)</h2>
<ul>
<li><strong>What it does:</strong> Enterprise approval system for multi-step workflows (leave requests, purchase orders, expense claims)</li>
<li><strong>Complexity level:</strong> State machine with 8+ states, hierarchical approvals (L1→L2→L3), delegation chains, auto-escalation</li>
<li><strong>Key differentiator:</strong> Not just authorization—enforces approval sequence, prevents state jumps, maintains audit compliance</li>
<li><strong>Auth:</strong> Stateless JWT with role + hierarchy + ownership validation (3-layer authorization)</li>
<li><strong>Deployed:</strong> AWS (EC2 + RDS) with scheduled job for time-based escalation</li>
<li><strong>Testing:</strong> State transition matrix + delegation chain validation + escalation logic</li>
</ul>

<p><strong>Why this project matters:</strong> Demonstrates handling of complex business workflows, not just REST CRUD. Shows understanding of state machines, scheduled tasks, and compliance requirements.</p>

<hr>

<h2>Problem Statement</h2>

<p>Organizations need approval systems where:</p>
<ul>
<li>Requests must pass through <strong>specific approvers in sequence</strong> (can't skip L1 and go to L2)</li>
<li>Approvers can <strong>delegate authority</strong> while maintaining accountability</li>
<li>System must <strong>auto-escalate</strong> stuck requests after timeout</li>
<li>Every action must be <strong>auditable</strong> for compliance (who approved, when, why)</li>
<li>Requesters <strong>cannot approve their own requests</strong> even if they have approver role</li>
</ul>

<p>This backend enforces workflow integrity at the service layer using state machine patterns—invalid transitions are impossible regardless of API usage.</p>

<hr>

<h2>System Architecture Overview</h2>

<h3>Approval Hierarchy (5 Levels):</h3>
<pre>
REQUESTER → APPROVER_L1 → APPROVER_L2 → APPROVER_L3 → FINAL_AUTHORITY
                ↓              ↓              ↓              ↓
           Can Delegate   Can Delegate   Can Override    Admin View
</pre>

<h3>State Machine (Request Lifecycle):</h3>
<pre>
DRAFT → SUBMITTED → PENDING_L1 → PENDING_L2 → PENDING_L3 → APPROVED
   ↓         ↓           ↓            ↓            ↓            ↓
CANCELLED  REJECTED   REJECTED     REJECTED    REJECTED    (Terminal)
                         ↓            ↓            ↓
                    ESCALATED_L2  ESCALATED_L3  (Auto)
</pre>

<p><strong>Key Constraint:</strong> State transitions are validated by service layer—controller cannot force invalid state changes.</p>

<hr>

<h2>Role-Based + Hierarchical Authorization</h2>

<table>
<thead>
<tr>
<th>Role</th>
<th>Can Do</th>
<th>Cannot Do</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>REQUESTER</strong></td>
<td>Submit requests; view own requests; cancel if status is DRAFT/SUBMITTED</td>
<td>Approve any request; view others' requests; edit after submission</td>
</tr>
<tr>
<td><strong>APPROVER_L1</strong></td>
<td>Approve/reject PENDING_L1 requests; delegate to peer L1; add comments; request clarification</td>
<td>Approve own requests; skip to L2; approve PENDING_L2 requests; modify request details</td>
</tr>
<tr>
<td><strong>APPROVER_L2</strong></td>
<td>Approve/reject PENDING_L2 requests; delegate to peer L2; view full approval chain</td>
<td>Bypass L1 approval; approve L3 requests; approve if delegated from different level</td>
</tr>
<tr>
<td><strong>APPROVER_L3</strong></td>
<td>Final approval/rejection; override L1/L2 decisions with mandatory justification; handle escalated requests</td>
<td>Approve without L1/L2 completion (unless emergency override with audit log)</td>
</tr>
<tr>
<td><strong>ADMIN</strong></td>
<td>Configure workflow templates; view all requests; generate audit reports; assign approvers</td>
<td>Approve requests (separation of duties); delete audit logs; modify completed workflows</td>
</tr>
</tbody>
</table>

<p><strong>Authorization Strategy:</strong> Three-layer validation (Role + Hierarchy Level + Ownership/Delegation)</p>

<hr>

<h2>Core Business Logic</h2>

<h3>1. State Machine Enforcement</h3>
<ul>
<li>Each state defines <strong>allowed next states</strong> and <strong>required conditions</strong></li>
<li>Service layer validates transition before database update</li>
<li>Example: PENDING_L1 can only transition to PENDING_L2 if approver has L1 role AND request not owned by approver</li>
</ul>

<h3>2. Approval Chain Validation</h3>
<ul>
<li>System tracks: who approved, when, delegation path, comments</li>
<li>Prevents: self-approval, level-skipping, duplicate approvals</li>
<li>Enforces: sequential approval (L2 cannot approve before L1 completes)</li>
</ul>

<h3>3. Delegation with Constraints</h3>
<ul>
<li>Approver A (L1) can delegate to Approver B (L1 only)</li>
<li>Delegatee inherits same validation rules as delegator</li>
<li>Audit log shows: "Approved by B (delegated from A)"</li>
<li>Delegation chain max depth: 2 (prevents circular delegation)</li>
</ul>

<h3>4. Time-Based Auto-Escalation</h3>
<ul>
<li>Scheduled job runs every 6 hours checking SLA timers</li>
<li>If request at PENDING_L1 for >48 hours → notify L1 + L2</li>
<li>If still pending after 72 hours → auto-escalate to L2 with flag</li>
<li>Escalation logged in audit trail with reason</li>
</ul>

<h3>5. Immutable Audit Trail</h3>
<ul>
<li>Every action stored in separate audit table (append-only)</li>
<li>Records: action type, timestamp, user ID, request ID, old state, new state, comments</li>
<li>Supports compliance requirements (ISO, SOX, GDPR)</li>
</ul>

<hr>

<h2>API Design & Layered Architecture</h2>

<h3>Architecture Pattern:</h3>
<pre>
Controller (HTTP + Validation)
    ↓
Service Layer (Business Logic + State Machine)
    ↓
Repository (JPA Data Access)
    ↓
Database (PostgreSQL with Audit Tables)

Cross-Cutting: Event Publisher → Notification Service (async)
</pre>

<h3>Key Design Patterns:</h3>
<ul>
<li><strong>State Machine:</strong> Enum-based states with transition validation service</li>
<li><strong>Strategy Pattern:</strong> Different approval strategies for different request types (Leave, Purchase, Expense)</li>
<li><strong>Chain of Responsibility:</strong> Approval chain processing</li>
<li><strong>Event-Driven:</strong> Spring Events for notifications (decoupled from approval logic)</li>
</ul>

<h3>Critical Service Methods:</h3>
<pre><code>// State transition with validation
RequestResponse approveRequest(Long requestId, ApprovalDTO dto, User currentUser)

// Delegation with constraint checking
DelegationResponse delegateApproval(Long requestId, Long delegateeId, User delegator)

// Auto-escalation (scheduled)
@Scheduled(cron = "0 0 */6 * * *")
void processEscalations()
</code></pre>

<hr>

<h2>Deployment</h2>

<ul>
<li><strong>EC2 Instance:</strong> Spring Boot JAR with Java 17+, Spring Scheduler enabled</li>
<li><strong>RDS PostgreSQL:</strong> Main database + separate audit schema (write-only)</li>
<li><strong>Configuration:</strong> Environment-based profiles, secrets via env variables</li>
<li><strong>Security Groups:</strong> Application load balancer → EC2:8080, EC2 → RDS:5432 (private subnet)</li>
<li><strong>Scheduled Tasks:</strong> Escalation job, SLA monitoring, reminder notifications</li>
</ul>

<hr>

<h2>How to Run Locally</h2>

<h3>Prerequisites:</h3>
<ul>
<li>Java 17+</li>
<li>PostgreSQL 14+</li>
<li>Maven 3.8+</li>
</ul>

<h3>Steps:</h3>
<pre><code># Clone repo
git clone &lt;repo-url&gt;
cd workflow-approval-system

# Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/workflow_db
export DB_USERNAME=postgres
export DB_PASSWORD=password
export JWT_SECRET=your-256-bit-secret-key
export ESCALATION_ENABLED=true

# Run application
./mvnw spring-boot:run

# Access: http://localhost:8080
# Scheduler runs automatically (check logs for escalation job)
</code></pre>

<h3>Database Setup:</h3>
<p>Schema auto-created via Hibernate in dev. For production, use Flyway migrations included in <code>/resources/db/migration</code>.</p>

<hr>

<h2>What This Project Demonstrates</h2>

<ul>
<li><strong>State machine implementation</strong> for workflow management (8+ states with validation)</li>
<li><strong>Multi-layer authorization</strong> (role + hierarchy + ownership)</li>
<li><strong>Scheduled job integration</strong> for time-based business logic (auto-escalation)</li>
<li><strong>Delegation patterns</strong> with constraint inheritance</li>
<li><strong>Audit trail design</strong> for compliance (immutable logs)</li>
<li><strong>Event-driven architecture</strong> (async notifications)</li>
</ul>

<h3>Honest Assessment:</h3>
<p>This is a junior-to-mid-level backend project focused on <strong>complex business logic</strong> rather than simple CRUD. It demonstrates state management, scheduled tasks, and multi-step validation flows. Lacks advanced features (parallel approvals, workflow templates, real-time dashboards) but shows solid understanding of enterprise workflow patterns.</p>

<h3>Compared to My Job Portal Project:</h3>
<ul>
<li><strong>Job Portal:</strong> Role-based authorization, DTOs, RESTful design (fundamentals)</li>
<li><strong>Workflow System:</strong> State machines, scheduled jobs, hierarchical authorization, audit compliance (advanced logic)</li>
</ul>

<hr>

<details>
<summary><strong>🔐 Authentication & Authorization (Click to expand)</strong></summary>

<h3>JWT Authentication:</h3>
<ul>
<li>Access token (15 min) contains: userId, email, roles[], approvalLevel</li>
<li>Refresh token (7 days) for token renewal</li>
<li>Spring Security filter validates token before controller</li>
</ul>

<h3>Three-Layer Authorization:</h3>
<ol>
<li><strong>Role Check:</strong> <code>@PreAuthorize("hasRole('APPROVER_L2')")</code></li>
<li><strong>Hierarchy Check:</strong> Service validates approval level matches request state</li>
<li><strong>Ownership Check:</strong> Service prevents self-approval, validates delegation chain</li>
</ol>

<p>All three must pass for approval action to succeed.</p>

</details>

<details>
<summary><strong>📋 Complete Workflow Examples (Click to expand)</strong></summary>

<h3>Example 1: Standard Leave Approval</h3>
<ol>
<li>Employee submits <code>POST /api/requests</code> → Status: DRAFT</li>
<li>Employee confirms → Status: SUBMITTED → Auto-transitions to PENDING_L1</li>
<li>Direct Manager (L1) approves → Status: PENDING_L2</li>
<li>Department Head (L2) approves → Status: PENDING_L3</li>
<li>HR Director (L3) final approval → Status: APPROVED</li>
<li>Audit trail contains: 5 entries (submit, L1 approve, L2 approve, L3 approve, notification sent)</li>
</ol>

<h3>Example 2: Delegation Flow</h3>
<ol>
<li>Manager A (L1) on vacation, delegates to Manager B (L1)</li>
<li>Manager B approves request → Audit shows: "Approved by Manager B (delegated from Manager A)"</li>
<li>Request moves to PENDING_L2 normally</li>
</ol>

<h3>Example 3: Auto-Escalation</h3>
<ol>
<li>Request stuck at PENDING_L1 for 48 hours</li>
<li>Scheduler detects SLA breach → sends notification to L1 approver</li>
<li>After 72 hours total → auto-escalates to L2 with flag: "ESCALATED_L2"</li>
<li>L2 approver can now approve (skipping L1) with escalation justification</li>
<li>Audit log: "Auto-escalated from L1 to L2 due to SLA timeout (72 hours)"</li>
</ol>

<h3>Example 4: Rejection Flow</h3>
<ol>
<li>L2 approver rejects request → Status: REJECTED</li>
<li>Service validates: mandatory rejection reason provided</li>
<li>Requester receives notification with rejection reason</li>
<li>Requester can edit and resubmit → creates new request (old one remains REJECTED in audit)</li>
</ol>

</details>

<details>
<summary><strong>🧪 Testing Strategy (Click to expand)</strong></summary>

<h3>What's Tested:</h3>

<h4>1. State Machine Logic (Unit Tests)</h4>
<ul>
<li>Can DRAFT transition to PENDING_L2? → Should fail</li>
<li>Can APPROVED transition to REJECTED? → Should fail (terminal state)</li>
<li>Can PENDING_L1 transition to PENDING_L2 without L1 approval? → Should fail</li>
<li>All 40+ valid/invalid transitions tested</li>
</ul>

<h4>2. Authorization Hierarchy (Integration Tests)</h4>
<ul>
<li>Can L2 approver approve L1 request? → Should fail</li>
<li>Can requester approve own request even with L1 role? → Should fail</li>
<li>Can approver delegate to different level? → Should fail</li>
</ul>

<h4>3. Delegation Chain (Unit + Integration)</h4>
<ul>
<li>Does delegatee inherit delegator's constraints? → Should pass</li>
<li>Can delegate more than 2 levels deep? → Should fail</li>
<li>Is delegation recorded in audit? → Should pass</li>
</ul>

<h4>4. Escalation Logic (Scheduled Job Tests)</h4>
<ul>
<li>Does 48-hour timeout trigger notification? → Should pass</li>
<li>Does 72-hour timeout trigger escalation? → Should pass</li>
<li>Is escalation reason logged? → Should pass</li>
</ul>

<h4>5. Audit Trail (Repository Tests)</h4>
<ul>
<li>Are all actions logged? → Should pass</li>
<li>Can audit entries be modified? → Should fail (append-only)</li>
<li>Does audit survive request deletion? → Should pass</li>
</ul>

<p>Testing focused on state transitions, authorization layers, and business rule enforcement.</p>

</details>

<details>
<summary><strong>🎯 Design Decisions (Click to expand)</strong></summary>

<h3>Why State Machine Over Simple Status Field?</h3>
<p>State machine enforces allowed transitions. A simple status string could be set to any value by mistake. State machine makes invalid transitions impossible.</p>

<h3>Why Three-Layer Authorization?</h3>
<p>Role alone isn't enough. An L2 approver shouldn't approve L1 requests even though they have approver role. Hierarchy + ownership adds necessary constraints.</p>

<h3>Why Separate Audit Table?</h3>
<p>Main table can be updated (request details change), but audit must be immutable for compliance. Separate table with append-only constraint ensures auditability.</p>

<h3>Why Delegation vs Reassignment?</h3>
<p>Delegation maintains accountability—audit shows original approver delegated to whom. Reassignment would lose this context, creating compliance gaps.</p>

<h3>Why Scheduled Job vs Event-Driven Escalation?</h3>
<p>Event-driven would require message queue (Kafka/RabbitMQ). For MVP, scheduled job is simpler and sufficient for 6-hour check intervals. Can migrate to events later.</p>

<h3>Trade-offs Made:</h3>
<ul>
<li><strong>No parallel approvals:</strong> Sequential only (parallel would need workflow engine like Camunda)</li>
<li><strong>No custom workflow templates:</strong> All requests follow same L1→L2→L3 (templates would need visual workflow builder)</li>
<li><strong>Fixed approval levels:</strong> Hard-coded 3 levels (dynamic levels would need graph-based approval chains)</li>
<li><strong>No real-time notifications:</strong> Email/SMS integration not implemented (would need external service)</li>
</ul>

</details>

<hr>

<p><strong>Questions?</strong> Open an issue or reach out via [your-contact].<br>
<strong>License:</strong> MIT</p>

</body>
</html>
```

---

## Key Optimizations for "Complex Logic Showcase":

### 1. **Positioned as Advanced Project**
- Added "Project Focus: Complex business logic" in header
- Explicitly compared to Job Portal at the end
- Emphasized state machine + scheduled jobs (beyond CRUD)

### 2. **Highlighted Technical Depth**
- State machine diagram (visual)
- Approval hierarchy diagram
- 8+ states with transition matrix
- 3-layer authorization (not just role-based)

### 3. **Business Logic Emphasis**
- 5 core business rules (not just "authorization")
- Delegation chains
- Auto-escalation (scheduled jobs)
- Audit compliance

### 4. **Different from Job Portal**
- Job Portal = Role-based access + DTOs
- Workflow = State machines + hierarchy + scheduled tasks + compliance

### 5. **Resume Strategy**
When listing both projects on resume:
```
Projects:
1. Job Portal Backend - Role-based access, JWT auth, AWS deployment
   Focus: Solid REST API fundamentals, security, clean architecture
   
2. Workflow Approval System - Multi-level approval with state machine
   Focus: Complex business logic, scheduled jobs, audit compliance
