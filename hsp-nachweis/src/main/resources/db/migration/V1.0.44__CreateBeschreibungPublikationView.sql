-- Create views for the dashboard concerning the publication status of the descriptions.
-- The view beschreibung_publikation_view contains all descriptions with their publication status,
-- while the view beschreibung_rejected_view contains only those descriptions that are in draft status
-- and have been rejected in the approval process.
CREATE OR REPLACE VIEW beschreibung_publikation_view AS
SELECT bv.hsp_id,
       bv.kod_id,
       bv.gueltige_signatur,
       bv.besitzer,
       bv.aufbewahrungsort,
       bv.bearbeiter,
       bv.aenderungsdatum,
       bv.aenderungsdatum_string,
       p.publikations_datum_hsp,
       p.status AS publikation_status
FROM beschreibung_dashboard_view bv
         INNER JOIN beschreibungsdokument b ON bv.hsp_id = b.id
         INNER JOIN publikation p ON b.publikation_id = p.id;

----------
CREATE OR REPLACE VIEW beschreibung_rejected_view AS
SELECT bpv.*,
       latest_ar.reviewed_by,
       latest_ar.reviewed_at
FROM beschreibung_publikation_view bpv
         INNER JOIN beschreibungsdokument b ON bpv.hsp_id = b.id
         INNER JOIN publikation p ON b.publikation_id = p.id
         LEFT JOIN LATERAL (
    SELECT ar.status AS approval_status,
           ar.reviewed_by,
           ar.reviewed_at
    FROM publikation_approval pa
             INNER JOIN approval_request ar ON pa.approval_request_id = ar.id
    WHERE pa.publikation_id = p.id
    ORDER BY ar.created_at DESC
    LIMIT 1
    ) latest_ar ON true
WHERE bpv.publikation_status = 'DRAFT'
  AND latest_ar.approval_status = 'REJECTED';



-----------
CREATE OR REPLACE VIEW beschreibung_in_review_view AS
SELECT bpv.*,
       active_ar.requested_at,
       active_ar.assigned_to
FROM beschreibung_publikation_view bpv
         INNER JOIN beschreibungsdokument b ON bpv.hsp_id = b.id
         INNER JOIN publikation p ON b.publikation_id = p.id
         LEFT JOIN LATERAL (
    SELECT ar.requested_at,
           ar.assigned_to
    FROM publikation_approval pa
             INNER JOIN approval_request ar ON pa.approval_request_id = ar.id
    WHERE pa.publikation_id = p.id
      AND ar.status IN ('REQUESTED', 'IN_REVIEW')
    ORDER BY ar.created_at DESC
    LIMIT 1
    ) active_ar ON true
WHERE bpv.publikation_status = 'IN_REVIEW'
  AND active_ar.requested_at IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_approval_request_created_at
    ON approval_request (created_at);
