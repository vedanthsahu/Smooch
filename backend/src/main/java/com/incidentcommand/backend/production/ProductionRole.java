package com.incidentcommand.backend.production;

/**
 * The role an employee holds for one specific production -- distinct from
 * the global Role (USER/OPERATOR/ADMIN). See docs/15-security-and-rbac.md:
 * authorization for incident actions is (role for THAT production, permission),
 * not just the global role.
 */
public enum ProductionRole {
    VIEWER,
    OPERATOR,
    ADMIN
}
