package com.dashradar.neo4jextensions;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class ReachedDestinationEvaluator implements Evaluator {

    @Override
    public Evaluation evaluate(Path path) {
        Relationship rel = path.lastRelationship();
        if (rel != null && rel.getType().name().equals("MIXING_SOURCE")) {
            return Evaluation.INCLUDE_AND_PRUNE;
        } else {
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }
    }
    
}
