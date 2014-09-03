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

    // Does my version pessimistically match the other version?
    // i.e, i am 2.3. and they are 2.3.0 -> yes
    public boolean pessimisticMatch(SemanticVersion other) {
        switch(other.patchlevel) {
            // i.e 2.4, only care about MV (2.4, 2.5, 2.6, 2.9, etc. match)
            case -1:
                return this.major == other.major && this.minor >= other.patchlevel;
            default:
                // i.e 2.3.4 so 2.3.4, 2.3.5 ... 2.3.XX match but 2.4 does not
                return this.major == other.major && this.minor == other.minor && this.patchlevel >= other.patchlevel;
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

    public static boolean validate(String version) {
        return version.matches("^(\\d{1,14}\\.?){2,3}$");
    }
}
