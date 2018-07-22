package com.dashradar.neo4jextensions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.neo4j.graphdb.GraphDatabaseService;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jgrapht.graph.DirectedMultigraph;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;


//curl -u neo4j -H "Content-Type: application/json" -X POST -d '{"txid":"TXID","maxRounds":"8"}' http://localhost:7474/v1/dashradar/psgraph

@Path("/dashradar")
public class Main {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static DirectedMultigraph<Vertex, Connection> createGraph(Stream<Connection> c) throws FileNotFoundException {
        DirectedMultigraph<Vertex, Connection> result = new DirectedMultigraph<Vertex, Connection>(Connection.class);
        c.forEach(e -> {
            Vertex source = new Vertex(e.fromTxid, false);
            Vertex target = new Vertex(e.toTxid, e.type.equals("MIXING_SOURCE"));
            result.addVertex(source);
            result.addVertex(target);
            result.addEdge(source, target, e);
        });
        return result;
    }
    
    @POST
    @Path("/psgraph")
    public Response test3(String body, @Context GraphDatabaseService db) throws IOException {
        try (Transaction tx = db.beginTx()) {
            JsonNode readTree = objectMapper.readTree(body);
            Node startNode = db.findNode(Label.label("Transaction"), "txid", readTree.get("txid").asText());
            int maxRounds = readTree.get("maxRounds").asInt();
            TraversalDescription td = db.traversalDescription();
            //td = td.breadthFirst();
            td = td.breadthFirst();
            td = td.relationships(RelationshipType.withName("PREVIOUS_ROUND"), Direction.OUTGOING);
            td = td.relationships(RelationshipType.withName("FIRST_ROUND"), Direction.OUTGOING);
            td = td.relationships(RelationshipType.withName("MIXING_SOURCE"), Direction.OUTGOING);
            td = td.evaluator(Evaluators.toDepth(maxRounds+1));
            td = td.uniqueness(Uniqueness.RELATIONSHIP_LEVEL);
            Traverser traverse = td.traverse(startNode);
            
            Stream<Connection> connectionStream = traverse.relationships().stream().distinct().map(rel -> {
                String fromTxId = rel.getStartNode().getProperty("txid").toString();
                String toTxId = rel.getEndNode().getProperty("txid").toString();
                long connections = (long)rel.getProperty("connections");
                return new Connection(fromTxId, toTxId, connections, rel.getType().name());
            });
            
            final Map<Vertex, Set<Connection>> incoming = new HashMap<>();
            final Map<Vertex, Set<Connection>> outgoing = new HashMap<>();
            
            
            connectionStream.forEach(c -> {
                incoming.computeIfAbsent(c.targetVertex(), v -> new HashSet<>()).add(c);
                outgoing.computeIfAbsent(c.sourceVertex(), v -> new HashSet<>()).add(c);
            });
            
            while (true) {
                List<Vertex> verticesToRemove = incoming.keySet().stream().filter(v -> !v.mixingSource && !outgoing.containsKey(v)).collect(Collectors.toList());
                if (verticesToRemove.isEmpty()) break;
                verticesToRemove.forEach(v -> {
                    incoming.get(v).forEach(c -> {
                        Set<Connection> old = outgoing.get(c.sourceVertex());
                        if (old.size() == 1) {
                            outgoing.remove(c.sourceVertex());
                        } else {
                            old.remove(c);
                        }
                    });
                    incoming.remove(v);
                });
            }
            
            String result = incoming.values()
                    .stream()
                    .flatMap(list -> list.stream())
                    .map(c -> "\""+c.fromTxid+"\",\""+c.toTxid+"\",\""+c.connections+"\"")
                    .collect(Collectors.joining("\n"));
            

            return Response.ok(result).build();
            //return Response.ok("n:"+beforeCount+","+afterCount+","+log).build();
        } catch(Exception ex) {
            return Response.ok(ex.toString()).build();
        }
    }

}
