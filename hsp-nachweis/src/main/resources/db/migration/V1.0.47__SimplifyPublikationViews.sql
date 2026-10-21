-- Ticket 23971 cleanup: beschreibung_publikation_view is now redundant since
-- beschreibung_dashboard_view exposes publikation_status and publikations_datum_hsp.
-- Drop the intermediate view and rebuild the approval-specific views directly
-- on beschreibung_dashboard_view.

DROP VIEW IF EXISTS beschreibung_rejected_view;
DROP VIEW IF EXISTS beschreibung_in_review_view;
DROP VIEW IF EXISTS beschreibung_publikation_view;

CREATE OR REPLACE VIEW beschreibung_rejected_view AS
SELECT bv.*,
       latest_ar.reviewed_by,
       latest_ar.reviewed_at
FROM beschreibung_dashboard_view bv
         INNER JOIN beschreibungsdokument b ON bv.hsp_id = b.id
         LEFT JOIN LATERAL (
    SELECT ar.status AS approval_status,
           ar.reviewed_by,
           ar.reviewed_at
    FROM publikation_approval pa
             INNER JOIN approval_request ar ON pa.approval_request_id = ar.id
    WHERE pa.publikation_id = b.publikation_id
    ORDER BY ar.created_at DESC
    LIMIT 1
    ) latest_ar ON true
WHERE bv.publikation_status = 'DRAFT'
  AND latest_ar.approval_status = 'REJECTED';

CREATE OR REPLACE VIEW beschreibung_in_review_view AS
SELECT bv.*,
       active_ar.requested_at,
       active_ar.assigned_to
FROM beschreibung_dashboard_view bv
         INNER JOIN beschreibungsdokument b ON bv.hsp_id = b.id
         LEFT JOIN LATERAL (
    SELECT ar.requested_at,
           ar.assigned_to
    FROM publikation_approval pa
             INNER JOIN approval_request ar ON pa.approval_request_id = ar.id
    WHERE pa.publikation_id = b.publikation_id
      AND ar.status IN ('REQUESTED', 'IN_REVIEW')
    ORDER BY ar.created_at DESC
    LIMIT 1
    ) active_ar ON true
WHERE bv.publikation_status = 'IN_REVIEW'
  AND active_ar.requested_at IS NOT NULL;
