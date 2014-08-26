package net.johnewart.barista.core;

public class SemanticVersion implements Comparable<SemanticVersion> {
    final public int major, minor, patchlevel;

    public SemanticVersion(String versionString) {
        String[] parts = versionString.split("\\.");

        major = Integer.parseInt(parts[0]);
        minor = Integer.parseInt(parts[1]);

        if (parts.length > 2) {
            patchlevel = Integer.parseInt(parts[2]);
        } else {
            patchlevel = -1;
        }
    }

    @Override
    public int compareTo(SemanticVersion other) {
        if(this.major > other.major) {
            return -1;
        } else if (this.major < other.major) {
            return 1;
        }

        if(this.minor > other.minor) {
            return -1;
        } else if (this.minor < other.minor) {
            return 1;
        }

        if (this.patchlevel > other.patchlevel) {
            return -1;
        } else if (this.patchlevel < other.patchlevel) {
            return 1;
        }

        return 0;
    }

    public String toString() {
        if(this.patchlevel > -1) {
            return String.format("%d.%d.%d", major, minor, patchlevel);
        } else {
            return String.format("%d.%d", major, minor);
        }
    }
}
