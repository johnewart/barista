package net.johnewart.barista.core;

public class VersionConstraint  {
    final String constraint;
    final SemanticVersion version;

    public VersionConstraint() {
        this.constraint = null;
        this.version = null;
    }

    public VersionConstraint(String versionConstraint) {
        if(versionConstraint == null) {
            constraint = null;
            version = null;
        } else {
            String[] parts = versionConstraint.split("\\s");
            if(parts.length == 1) {
                constraint = "==";
                version = new SemanticVersion(parts[0]);
            } else {
                constraint = parts[0];
                version = new SemanticVersion(parts[1]);
            }
        }
    }

    // TODO: ~
    // TODO: Enum instead of string
    public boolean matches(SemanticVersion other) {

        // No constraint, everything matches
        if(this.version == null || this.constraint == null) {
            return true;
        }

        int result = other.compareTo(version);

        switch(constraint) {
            case "<":
                return result == 1;
            case "==":
            case "=":
                return result == 0;
            case "<=":
            case "=<":
                return (result == 0 || result == 1);
            case ">":
                return result == -1;
            case ">=":
            case "=>":
                return (result == 0 || result == -1);
            default:
                return false;
        }
    }

}