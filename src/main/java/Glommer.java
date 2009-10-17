import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import osm.OSMFile;
import osm.primitive.node.LatLon;
import osm.primitive.node.Node;
import osm.primitive.way.Way;


public class Glommer {
    
    private final OSMFile data;
    private final String key;

    public Glommer(OSMFile aggregate, String keyToGlomOn) {
        data = aggregate;
        key = keyToGlomOn;
    }

    public OSMFile glom() {
        Map<LatLon, List<Node>> locToNodes = new HashMap<LatLon, List<Node>>();
        Map<Node, List<Way>> nodeToWays = new HashMap<Node, List<Way>>();
        Map<Way, String> wayToTagValue = new HashMap<Way, String>();
        OSMFile out = new OSMFile();
        
        Iterator<Way> wayIterator = data.getWayIterator();
        while (wayIterator.hasNext()) {
            Way way = wayIterator.next();
            String value = way.getTagValue(key);
            
            if(value == null) {
                // No matching key, so just add it as-is to the file
                out.addWay(way);
                continue;
            } else {
                wayToTagValue.put(way, value);
                
                List<Node> nodeList = way.getNodes();
                for (int i = 0; i < nodeList.size(); i++) {
                    Node node = nodeList.get(i);
                    LatLon point = node.getPoint();

                    List<Way> waysWithNode = nodeToWays.get(node);
                    if(waysWithNode == null) {
                        waysWithNode = new LinkedList<Way>();
                        nodeToWays.put(node, waysWithNode);
                    }
                    waysWithNode.add(way);
                    
                    List<Node> nodesAtPoint = locToNodes.get(point);
                    if(nodesAtPoint == null) {
                        nodesAtPoint = new LinkedList<Node>();
                        locToNodes.put(point, nodesAtPoint);
                        nodesAtPoint.add(node);
                    } else {
                        nodesAtPoint.add(node);
                        
                        // There's already a node there, so check to see if any
                        // of the nodes are part of ways that have our
                        // glom-key's value.
                        for (Node suspectNode : nodesAtPoint) {
                            List<Way> suspectWays = nodeToWays.get(suspectNode);
                            if (suspectWays != null) {
                                for (Way suspectWay : suspectWays) {
                                    if (!suspectWay.equals(way) && suspectWay.getTagValue(key).equals(value)) {
                                        // The suspect way has the same value as
                                        // the way we're looking at, so the node
                                        // of the current way should be replaced
                                        // with the suspect's node
                                        nodeList.set(i, suspectNode);
                                        nodesAtPoint.remove(node);
                                        nodeToWays.remove(node);
                                    }
                                }
                            }
                        }
                    }
                }

                out.addWay(way);
            }
        }
        
        return out;
    }

}
