CREATE INDEX IF NOT EXISTS idx_gueltigesignatur ON kulturobjektdokument(gueltigeidentifikation_id);
CREATE INDEX IF NOT EXISTS idx_alternativesignatur ON identifikation(alternativeidentifikationen_id);
CREATE INDEX IF NOT EXISTS idx_identifikation_ident ON identifikation(ident);
CREATE INDEX IF NOT EXISTS idx_normdatenreferenz_name ON normdatenreferenz(name);
CREATE INDEX IF NOT EXISTS idx_normdatenreferenzvariant_name ON normdatenreferenz_variantername(name);
CREATE INDEX IF NOT EXISTS idx_normdatenreferenzidentifikator ON normdatenreferenz_identifikator(text);
