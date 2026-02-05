<div align="center">

<h1> Workflow / Approval Management Backend (Spring Boot)</h1>

<p>
A backend-focused Spring Boot project demonstrating
<strong>workflow-driven state transitions</strong>,
<strong>role-based approval authority</strong>,
and <strong>service-layer business rule enforcement</strong>.
</p>

<p><em>
Designed to explore how approval workflows prevent invalid state transitions
and unauthorized actions in real-world backend systems.
</em></p>

<hr/>

</div>

<h2> Problem This Project Addresses</h2>

<p>
Approval-based systems (leave requests, expense approvals, document reviews)
often fail when business rules are enforced inconsistently across APIs.
</p>

<p>
This project enforces all workflow rules and authorization logic
inside the <strong>service layer</strong>,
ensuring that approvals, rejections, and state transitions
remain valid regardless of how the API is accessed.
</p>

<hr/>

<h2> Architecture Overview</h2>

<pre>
Client
  ↓
Controller (request/response only)
  ↓
Service (workflow rules + authorization)
  ↓
Repository
  ↓
PostgreSQL
</pre>

<p>
Controllers are intentionally thin.
All workflow state validation and role checks occur in services.
</p>

<!-- Add architecture diagram image here -->

<hr/>

<h2>🔐 Roles & Approval Authority</h2>

<h3>System Roles</h3>
<ul>
  <li><strong>REQUESTER</strong> — submits approval requests</li>
  <li><strong>APPROVER</strong> — approves or rejects requests</li>
</ul>

<h3>Approval Rules</h3>
<ul>
  <li>Only <strong>REQUESTER</strong> users can create requests</li>
  <li>Only authorized <strong>APPROVER</strong> users can approve or reject</li>
  <li>A request can be acted upon only once</li>
  <li>Invalid actions are rejected before database mutation</li>
</ul>

<hr/>

<h2>🔁 Workflow States</h2>

<p>
Each request follows a strict lifecycle:
</p>

<pre>
PENDING → APPROVED
PENDING → REJECTED
</pre>

<ul>
  <li>State transitions are validated centrally</li>
  <li>Approved or rejected requests cannot be modified</li>
  <li>Duplicate approvals or rejections are prevented</li>
</ul>

<hr/>

<h2>📡 API Overview</h2>

<h3>Request Creation</h3>
<pre>
POST /requests
</pre>

<h3>Approval Actions</h3>
<pre>
POST /requests/{id}/approve
POST /requests/{id}/reject
</pre>

<h3>Query Operations</h3>
<pre>
GET /requests/my
GET /requests/pending
</pre>

<p>
JWT authentication and role-based access are enforced consistently.
</p>

<hr/>

<h2>📝 Example Workflow Behavior</h2>

<h3>Valid Approval</h3>
<pre>
POST /requests/15/approve
Authorization: Bearer &lt;APPROVER_TOKEN&gt;

→ 200 OK
"Request approved successfully"
</pre>

<h3>Invalid Duplicate Action</h3>
<pre>
POST /requests/15/approve
Authorization: Bearer &lt;APPROVER_TOKEN&gt;

→ 409 CONFLICT
"Request has already been processed"
</pre>

<h3>Unauthorized Approval Attempt</h3>
<pre>
POST /requests/15/approve
Authorization: Bearer &lt;REQUESTER_TOKEN&gt;

→ 403 FORBIDDEN
"Only approvers can approve requests"
</pre>

<hr/>

<h2>🛡️ Security & Business Rule Enforcement</h2>

<ul>
  <li>Stateless JWT authentication</li>
  <li>Role-based authorization enforced in service layer</li>
  <li>Workflow state validation centralized in services</li>
  <li>Controllers do not contain business logic</li>
</ul>

<hr/>

<h2>🗄️ Data Model</h2>

<ul>
  <li><strong>users</strong> — authentication and role information</li>
  <li><strong>requests</strong> — approval requests with current state</li>
</ul>

<p>
State and ownership checks ensure consistency and data integrity.
</p>

<!-- Add database schema diagram here -->

<hr/>

<h2>🧪 Testing Focus</h2>

<ul>
  <li>Service-layer unit tests validating workflow transitions</li>
  <li>Tests covering invalid state changes and duplicate actions</li>
  <li>Mockito used to isolate repositories</li>
</ul>

<hr/>

<h2>⚙️ Running Locally</h2>

<pre>
git clone https://github.com/DeepsanBhandari/WorkflowApprovalManagementSystem.git
cd WorkflowApprovalManagementSystem
</pre>

<p>
Configure PostgreSQL and environment variables, then:
</p>

<pre>
mvn spring-boot:run
</pre>

<hr/>

<h2>🛠️ Tech Stack</h2>

<ul>
  <li>Java 17</li>
  <li>Spring Boot 3.x</li>
  <li>Spring Security</li>
  <li>PostgreSQL</li>
  <li>JWT</li>
  <li>Maven</li>
</ul>

<hr/>

<h2>🎯 What This Project Demonstrates</h2>

<ul>
  <li>Workflow-driven backend design</li>
  <li>State machine–like transition validation</li>
  <li>Service-layer enforcement of business rules</li>
  <li>Role-based approval authority</li>
  <li>Clean, testable layered architecture</li>
</ul>

<div align="center">
  <p><em>
  Built as a focused backend project to understand how approval systems
  enforce correctness, authorization, and workflow consistency.
  </em></p>
</div>
