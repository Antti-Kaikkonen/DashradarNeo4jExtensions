package com.dashradar.neo4jextensions;

import java.util.Objects;

public class Connection {
    
    public Connection(String fromTxid, String toTxid, long connections, String type) {
        this.fromTxid = fromTxid;
        this.toTxid = toTxid;
        this.connections = connections;
        this.type = type;
    }
    
    String fromTxid;
    String toTxid;
    long connections;
    String type;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.fromTxid);
        hash = 97 * hash + Objects.hashCode(this.toTxid);
        hash = 97 * hash + (int) (this.connections ^ (this.connections >>> 32));
        hash = 97 * hash + Objects.hashCode(this.type);
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
        final Connection other = (Connection) obj;
        if (this.connections != other.connections) {
            return false;
        }
        if (!Objects.equals(this.fromTxid, other.fromTxid)) {
            return false;
        }
        if (!Objects.equals(this.toTxid, other.toTxid)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return true;
    }
    
    public Vertex sourceVertex() {
        return new Vertex(fromTxid, false);
    }
    
    public Vertex targetVertex() {
        return new Vertex(toTxid, type.equals("MIXING_SOURCE"));
    }
    
}
