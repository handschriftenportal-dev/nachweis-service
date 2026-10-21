-- Migration to refactor Publikation structure and create approval workflow
-- 1. Move from Set<Publikation> to single Publikation in Beschreibung
-- 2. Merge ERSTPUBLIKATION and PUBLIKATION_HSP into single Publikation with two date fields
-- 3. Create approval_request table for generic approval workflow
-- 4. Simplify Publikation status to DRAFT/PUBLISHED

-- Set timeouts for long-running operations (60 minutes)
SET statement_timeout = '3600000';
SET lock_timeout = '300000';

-- Update statistics for query planner
ANALYZE beschreibungsdokument;
ANALYZE beschreibungsdokument_publikation;
ANALYZE publikation;
ANALYZE purl;

-- Cleanup: Delete any orphaned Publikation records not linked to any beschreibungsdokument
-- (to avoid issues with foreign key constraints later)
DELETE
FROM publikation
WHERE id IN (SELECT p.id
             FROM publikation p
             WHERE NOT EXISTS (SELECT 1
                               FROM beschreibungsdokument_publikation bp
                                        JOIN beschreibungsdokument b ON bp.beschreibung_id = b.id
                               WHERE bp.publikationen_id = p.id));

------------------------------------------------------------------------------
-- PART 1: Refactor Publikation Structure
------------------------------------------------------------------------------

-- Step 1: Add new columns to Publikation table
-- Note: approval-related columns (approved_by, approved_at, assigned_to) go in approval_request table
ALTER TABLE Publikation
    ADD COLUMN erstpublikations_datum TIMESTAMP    DEFAULT NULL,
    ADD COLUMN publikations_datum_hsp TIMESTAMP    DEFAULT NULL,
    ADD COLUMN status                 VARCHAR(255),
    ADD COLUMN auflagen_nummer        INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN previousPublikation_id VARCHAR(255) DEFAULT NULL,
    ADD COLUMN lizenz_id              VARCHAR(255) DEFAULT NULL;

-- Step 2: Add publikation_id column to beschreibungsdokument for one-to-one relationship
ALTER TABLE beschreibungsdokument
    ADD COLUMN publikation_id VARCHAR(255);


-- ADD permanent index for beschreibungsdokument.publikation_id
CREATE INDEX IF NOT EXISTS idx_beschreibung_publikation ON beschreibungsdokument (publikation_id);


-- Add temporary indexes for performance
CREATE INDEX IF NOT EXISTS idx_temp_bp_beschreibung ON beschreibungsdokument_publikation (beschreibung_id);
CREATE INDEX IF NOT EXISTS idx_temp_bp_publikationen ON beschreibungsdokument_publikation (publikationen_id);
CREATE INDEX IF NOT EXISTS idx_temp_publikation_typ ON publikation (publikations_typ);
CREATE INDEX IF NOT EXISTS idx_temp_beschreibung_verwaltungstyp ON beschreibungsdokument (verwaltungstyp);

-- Step 3: Link each beschreibungsdokument to its ERSTPUBLIKATION
UPDATE beschreibungsdokument b
SET publikation_id = (SELECT p.id
                      FROM Publikation p
                               INNER JOIN beschreibungsdokument_publikation bp
                                          ON p.id = bp.publikationen_id
                      WHERE bp.beschreibung_id = b.id
                        AND p.publikations_typ = 'ERSTPUBLIKATION')
WHERE publikation_id IS NULL
  AND b.verwaltungstyp = 'EXTERN';

-- Step 4: Migrate datum_der_veroeffentlichung to erstpublikations_datum for ERSTPUBLIKATION
UPDATE Publikation
SET erstpublikations_datum = datum_der_veroeffentlichung
WHERE publikations_typ = 'ERSTPUBLIKATION';

-- Step 5: Copy datum_der_veroeffentlichung from PUBLIKATION_HSP to erstpublikations_datum_hsp of the linked ERSTPUBLIKATION
-- Optimized: use UPDATE...FROM to execute join only once instead of twice
UPDATE Publikation p_erst
SET publikations_datum_hsp = p_hsp.datum_der_veroeffentlichung
FROM Publikation p_hsp
         INNER JOIN beschreibungsdokument_publikation bp_hsp
                    ON p_hsp.id = bp_hsp.publikationen_id
         INNER JOIN beschreibungsdokument b
                    ON bp_hsp.beschreibung_id = b.id
WHERE b.publikation_id = p_erst.id
  AND p_hsp.publikations_typ = 'PUBLIKATION_HSP';

-- Set status based on beschreibungsdokument verwaltungstyp
-- EXTERN -> PUBLISHED, INTERN -> DRAFT

-- Set status to 'PUBLISHED' for EXTERN beschreibungsdokument
UPDATE Publikation p
SET status = 'PUBLISHED'
FROM beschreibungsdokument b
WHERE b.publikation_id = p.id
  AND b.verwaltungstyp = 'EXTERN';

-- Set status to 'DRAFT' for INTERN beschreibungsdokument
UPDATE Publikation p
SET status = 'DRAFT'
FROM beschreibungsdokument b
WHERE b.publikation_id = p.id
  AND b.verwaltungstyp = 'INTERN';

-- Step 6: Drop the join table (no longer needed with one-to-one relationship)
-- Must be done before deleting PUBLIKATION_HSP records to avoid foreign key constraint violation
DROP TABLE beschreibungsdokument_publikation;

-- Step 7: Delete all PUBLIKATION_HSP records (data already migrated to ERSTPUBLIKATION)
DELETE
FROM Publikation
WHERE publikations_typ = 'PUBLIKATION_HSP';

-- Step 8: Drop deprecated columns from Publikation
ALTER TABLE Publikation
    DROP COLUMN IF EXISTS datum_der_veroeffentlichung,
    DROP COLUMN IF EXISTS publikations_typ;

-- Add foreign key constraints
ALTER TABLE Publikation
    ADD CONSTRAINT fk_publikation_vorherigePublikation FOREIGN KEY (previousPublikation_id) REFERENCES Publikation (id),
    ADD CONSTRAINT fk_publikation_lizenz FOREIGN KEY (lizenz_id) REFERENCES Lizenz (id);

ALTER TABLE beschreibungsdokument
    ADD CONSTRAINT fk_beschreibung_publikation
        FOREIGN KEY (publikation_id) REFERENCES Publikation (id);

------------------------------------------------------------------------------
-- PART 2: PURL Migration
------------------------------------------------------------------------------

-- Step 9: Drop old foreign key constraint first to speed up DELETE operation
ALTER TABLE purl
    DROP CONSTRAINT IF EXISTS purl_beschreibungsdokument_id_fk;

-- Add temporary index for PURL cleanup
CREATE INDEX IF NOT EXISTS idx_temp_purl_beschreibung ON purl (beschreibungsdokument_id);

-- Step 10: Clean up orphaned PURLs that reference non-existent beschreibungsdokumente
DELETE
FROM purl p
WHERE NOT EXISTS (SELECT 1
                  FROM beschreibungsdokument b
                  WHERE b.id = p.beschreibungsdokument_id);

-- Step 11: add publikation_id column to purl table
ALTER TABLE purl
    ADD COLUMN publikation_id VARCHAR(255);

-- Step 12: link each purl to its beschreibungsdokument's publikation
UPDATE purl p
SET publikation_id = b.publikation_id
FROM beschreibungsdokument b
WHERE b.id = p.beschreibungsdokument_id;

-- Step 13: publikation_id remains NULLABLE because KODs also have PURLs
-- via kulturobjektdokument_id FK. Only Beschreibung PURLs use publikation_id.

-- Step 14: add foreign key constraint with CASCADE DELETE to prevent orphaned PURLs
ALTER TABLE purl
    ADD CONSTRAINT fk_purl_publikation
        FOREIGN KEY (publikation_id) REFERENCES Publikation (id) ON DELETE CASCADE;

-- Step 15: drop beschreibungsdokument_id column from purl table
ALTER TABLE purl
    DROP COLUMN beschreibungsdokument_id;

------------------------------------------------------------------------------
-- PART 3: Lizenz Migration
------------------------------------------------------------------------------

-- Step 16: publikation.lizenz_id = beschreibungsdokument.lizenz_id
UPDATE Publikation p
SET lizenz_id = b.lizenz_id
FROM beschreibungsdokument b
WHERE b.publikation_id = p.id
  AND b.lizenz_id IS NOT NULL;

-- Step 17: drop old foreign key constraint on beschreibungsdokument.lizenz_id
ALTER TABLE beschreibungsdokument
    DROP CONSTRAINT IF EXISTS fklx9g8sp7oq8dbpcmq6ty9nq0r;

-- Step 18: drop lizenz_id column from beschreibungsdokument table
ALTER TABLE beschreibungsdokument
    DROP COLUMN lizenz_id;

------------------------------------------------------------------------------
-- PART 4: Handle INTERN Beschreibungen
------------------------------------------------------------------------------

-- Step 19: Create new Publikation for each INTERN beschreibung
DO
$$
    DECLARE
    BEGIN
        CREATE TEMP TABLE intern_mapping
        (
            beschreibung_id VARCHAR(255),
            publikation_id  VARCHAR(255)
        );

        INSERT INTO intern_mapping (beschreibung_id, publikation_id)
        SELECT id, gen_random_uuid()::VARCHAR
        FROM beschreibungsdokument
        WHERE verwaltungstyp = 'INTERN';

        INSERT INTO Publikation (id, status, auflagen_nummer)
        SELECT publikation_id, 'DRAFT', 1
        FROM intern_mapping;

        UPDATE beschreibungsdokument b
        SET publikation_id = m.publikation_id
        FROM intern_mapping m
        WHERE b.id = m.beschreibung_id;

        DROP TABLE intern_mapping;
    END
$$;

------------------------------------------------------------------------------
-- PART 5: Cleanup orphaned Publikationen
------------------------------------------------------------------------------

-- Step 20: Clean up - drop any orphaned Publikation records not linked to any beschreibungsdokument
DELETE
FROM Publikation p
WHERE NOT EXISTS (SELECT 1
                  FROM beschreibungsdokument b
                  WHERE b.publikation_id = p.id);

------------------------------------------------------------------------------
-- PART 6: Finalize Publikation status
------------------------------------------------------------------------------

-- Step 21: Enforce NOT NULL constraint on status column after all data is populated
ALTER TABLE Publikation
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN status SET DEFAULT 'DRAFT';

------------------------------------------------------------------------------
-- PART 7: Create approval_request table and publikation_approval join table
------------------------------------------------------------------------------

-- Create approval_request table (generic, reusable for different entity types)
CREATE TABLE approval_request
(
    id           VARCHAR(255) PRIMARY KEY,

    -- Type of approval (e.g., PUBLICATION)
    type         VARCHAR(50)   NOT NULL,

    -- Workflow status
    status       VARCHAR(50)   NOT NULL,

    -- Request phase
    requested_by VARCHAR(255)  NOT NULL,
    requested_at TIMESTAMP     NOT NULL,

    -- Assignment phase
    assigned_to  VARCHAR(255),
    assigned_at  TIMESTAMP,

    -- Review phase
    reviewed_by  VARCHAR(255),
    reviewed_at  TIMESTAMP,
    comment      VARCHAR(4096) NOT NULL DEFAULT '',

    -- Audit
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Create indexes for approval_request
CREATE INDEX idx_approval_request_status ON approval_request (status);
CREATE INDEX idx_approval_request_assigned ON approval_request (assigned_to) WHERE assigned_to IS NOT NULL;
CREATE INDEX idx_approval_request_type ON approval_request (type);

-- Create join table for publikation <-> approval_request relationship
-- This allows approval_request to be reusable for other entity types
CREATE TABLE publikation_approval
(
    publikation_id      VARCHAR(255) NOT NULL,
    approval_request_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (publikation_id, approval_request_id)
);

-- Add foreign key constraints for join table
-- No CASCADE on publikation to prevent accidental deletion of publikation
-- CASCADE on approval_request to clean up join table when request is deleted
ALTER TABLE publikation_approval
    ADD CONSTRAINT fk_publikation_approval_publikation
        FOREIGN KEY (publikation_id) REFERENCES publikation (id),
    ADD CONSTRAINT fk_publikation_approval_request
        FOREIGN KEY (approval_request_id) REFERENCES approval_request (id) ON DELETE CASCADE;

-- Create indexes for efficient lookups on join table
CREATE INDEX idx_publikation_approval_publikation ON publikation_approval (publikation_id);
CREATE INDEX idx_publikation_approval_request ON publikation_approval (approval_request_id);

------------------------------------------------------------------------------
-- PART 8: Cleanup
------------------------------------------------------------------------------

-- Drop temporary indexes that still exist
-- Note: Most temporary indexes were already dropped when their tables/columns were dropped
-- idx_temp_bp_beschreibung and idx_temp_bp_publikationen were dropped with beschreibungsdokument_publikation table (Step 6)
-- idx_temp_publikation_typ was dropped with publikations_typ column (Step 8)
-- idx_temp_purl_beschreibung was dropped with purl.beschreibungsdokument_id column (Step 15)

DROP INDEX IF EXISTS idx_temp_beschreibung_verwaltungstyp;

-- Update statistics
ANALYZE publikation;
ANALYZE approval_request;
ANALYZE publikation_approval;
