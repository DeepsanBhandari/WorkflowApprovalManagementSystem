
import com.dep.Workflow.model.Approval;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    // Temporary
    private List<Approval> approvals = new ArrayList<>();
    private Long idCounter = 1L;

    // Add some sample data when controller is created
    public ApprovalController() {
        approvals.add(createSampleApproval("Purchase Order #001", "New laptops for dev team", "John Doe"));
        approvals.add(createSampleApproval("Leave Request", "Vacation from Jan 15-20", "Jane Smith"));
        approvals.add(createSampleApproval("Budget Approval", "Q1 Marketing budget", "Bob Wilson"));
    }

    private Approval createSampleApproval(String title, String description, String requestedBy) {
        Approval approval = new Approval(title, description, requestedBy);
        approval.setId(idCounter++);
        return approval;
    }


    @GetMapping
    public List<Approval> getAllApprovals() {
        return approvals;
    }

    /**
     * GET approval by ID
     *
     * What happens here:
     * 1. @PathVariable extracts ID from URL
     * 2. We search through our list
     * 3. Return the approval or null
     *
     * Try: http://localhost:8080/api/approvals/1
     */
    @GetMapping("/{id}")
    public Approval getApprovalById(@PathVariable Long id) {
        return approvals.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * POST - Create new approval
     *
     * @RequestBody automatically converts JSON to Approval object!
     *
     * Try in Postman with this JSON body:
     * {
     *   "title": "New Hire Approval",
     *   "description": "Approve hiring for Senior Developer position",
     *   "requestedBy": "HR Manager"
     * }
     *
     * Notice: You don't need to send id, status, or createdAt - we set those!
     */
    @PostMapping
    public Approval createApproval(@RequestBody Approval approval) {
        approval.setId(idCounter++);
        approval.setStatus("PENDING");
        approvals.add(approval);
        return approval;
    }

    /**
     * PUT - Approve an approval
     *
     * This finds the approval and changes its status to APPROVED
     */
    @PutMapping("/{id}/approve")
    public Approval approveApproval(@PathVariable Long id) {
        Approval approval = getApprovalById(id);
        if (approval != null) {
            approval.setStatus("APPROVED");
        }
        return approval;
    }

    /**
     * PUT - Reject an approval
     */
    @PutMapping("/{id}/reject")
    public Approval rejectApproval(@PathVariable Long id) {
        Approval approval = getApprovalById(id);
        if (approval != null) {
            approval.setStatus("REJECTED");
        }
        return approval;
    }

    /**
     * DELETE - Remove an approval
     * NEW in Phase 2! Try it out.
     */
    @DeleteMapping("/{id}")
    public String deleteApproval(@PathVariable Long id) {
        boolean removed = approvals.removeIf(a -> a.getId().equals(id));
        if (removed) {
            return "✅ Approval " + id + " deleted successfully";
        } else {
            return "❌ Approval " + id + " not found";
        }
    }

    /**
     * GET - Get approvals by status
     * NEW! Filter approvals by status
     *
     * Try: http://localhost:8080/api/approvals/status/PENDING
     */
    @GetMapping("/status/{status}")
    public List<Approval> getApprovalsByStatus(@PathVariable String status) {
        return approvals.stream()
                .filter(a -> a.getStatus().equalsIgnoreCase(status))
                .toList();
    }
}
