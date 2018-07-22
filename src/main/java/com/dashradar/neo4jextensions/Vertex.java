package com.dashradar.neo4jextensions;

import java.util.Objects;

public class Vertex {
    
    public Vertex(String txid, boolean mixingSource) {
        this.txid = txid;
        this.mixingSource = mixingSource;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.txid);
        hash = 89 * hash + (this.mixingSource ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vertex other = (Vertex) obj;
        if (this.mixingSource != other.mixingSource) {
            return false;
        }
        if (!Objects.equals(this.txid, other.txid)) {
            return false;
        }
        return true;
    }
    
    
    
    public String txid;
    public boolean mixingSource;
}
