import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import javax.json.*;

/****
 * Description : This class reads Directed graph structure from JSON objects,
 * builds the graph, and finds the list of nodes which does not have a direct
 * path to the root, if an edge is deleted. Algorithm functionality: We need to
 * find a path from each node to root to check if a node is not an orphan node.
 * Instead of finding a path from each node to root, the edges in the graph are
 * reversed and the depth first search algorithm marks all the nodes reachable
 * from the root. The nodes which are not reachable are the orphan nodes in the
 * original graph. (i.e. there is no path from the node to the root) author:
 * Neha Mahajan Version: 1 Date : 02/25/2017
 ****/

public class FindOrphanNodes {

	/*
	 * Each edge in a graph has a start node and end node. The HashMap stores
	 * edges in the graph using key-value pairs key{Integer} "From" Node
	 * value{Set of integers} set of neighbor nodes ("To" nodes)
	 */
	private HashMap<Integer, HashSet<Integer>> adjacencyList = new HashMap<Integer, HashSet<Integer>>();

	// Store the nodes in an array so that all operations can be done using the
	// corresponding index
	private ArrayList<String> indexToNodeName = new ArrayList<String>();

	/*
	 * Create a HashMap of nodes. This map manages mapping of node names to
	 * index. key{String} nodeName value{Integer} index which is generated
	 * sequentially
	 */
	private HashMap<String, Integer> nodeNameToIndexMapping = new HashMap<String, Integer>();

	// Index of the root node
	private int root = 0;

	// Boolean array to check if a node is visited during depth-first search
	// algorithm
	private boolean visited[];

	// Counter for nodes
	private int nodeCounter = 0;

	public static void main(String[] args) {
		FindOrphanNodes fon = new FindOrphanNodes();

		// Read the input, parse it, creates graph and finds orphan nodes.
		String filename = "./datasource/example1_json.txt";
		fon.readInputDataFileAndParseIt(filename);
	}

	/*
	 * Read the json object from the input file.
	 */
	private void readInputDataFileAndParseIt(String filename) {

		System.out.println("Loading input file..\n");
		JsonReader reader = null;
		JsonObject graphObject = null;
		try {
			reader = Json.createReader(new FileReader(filename));
			graphObject = reader.readObject();

			// Parses JSON objects to generate graph and modify it as per input
			// nodes.
			parseData(graphObject);

			// Depth first search to find all the nodes that do not have a
			// directed path to root
			dfsStack();

			// Retrieve node names using the visited array and nodes array list.
			getDanglingNodes();

		} catch (FileNotFoundException e) {
			System.err.println("Input file at path" + filename + " does not exists");
			System.err.println("Exiting the program");
		} catch (Exception e) {
			System.err.println("File could not be sucessfully parsed.Below are the details:");
			System.err.print(e);
			System.err.println("\tAt Line number: " + e.getStackTrace()[0].getLineNumber());
			System.err.println("Exiting the program");
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	/*
	 * Parse the json object to read the edges, nodes and root of the graph and
	 * store them into the HashMap.
	 * 
	 * @param graphObject JSON object containing nodes, edges, root and deleted
	 * edge key-attribute pairs.
	 * 
	 * @return void
	 */
	private void parseData(JsonObject graphObject) throws Exception {
		JsonObject object;
		String nodeName, to, from = "";
		Integer toIndex, fromIndex = 0;
		HashSet<Integer> currentHashSet = null;

		// Fetch the list of nodes from JSON array
		JsonArray nodes = graphObject.getJsonArray("nodes");
		for (JsonValue jsonValue : nodes) {
			object = (JsonObject) jsonValue;
			nodeName = object.getString("id");

			// If node is not already added, add the node to the nodes list.
			if (!nodeNameToIndexMapping.containsKey(nodeName)) {
				nodeNameToIndexMapping.put(nodeName, nodeCounter);
				indexToNodeName.add(nodeName);
				nodeCounter++;
			}
		}

		// Initialize root node.
		root = nodeNameToIndexMapping.get(graphObject.getString("root"));

		// Fetch the list of edges from JSON array
		// reverse the edges for the dfs algorithm
		JsonArray edges = graphObject.getJsonArray("edges");
		for (JsonValue jsonValue : edges) {
			object = (JsonObject) jsonValue;

			to = object.getString("to");
			from = object.getString("from");

			toIndex = nodeNameToIndexMapping.get(to);
			fromIndex = nodeNameToIndexMapping.get(from);

			// Add the edge only if the to and from nodes are valid.
			if (toIndex != null && fromIndex != null) {
				if (!adjacencyList.containsKey(toIndex)) {
					// Create new entry for the node in the hash map that will
					// store
					// a set of all the end nodes.
					adjacencyList.put(toIndex, currentHashSet = new HashSet<Integer>());
				} else
					currentHashSet = adjacencyList.get(toIndex);

				/*
				 * For the algorithm to work the edges in the graph are
				 * reversed. The line below adds the edge: to -> from instead of
				 * from -> to.
				 */
				currentHashSet.add(fromIndex);
				adjacencyList.put(toIndex, currentHashSet);
			} else {
				if (toIndex == null)
					System.err.println("Node >>> " + to + " does not exists in the graph");

				if (fromIndex == null)
					System.err.println("Node >>> " + from + " does not exists in the graph");
			}
		}

		// printGraph();

		// Function call to delete the edges from the graph
		deleteEdges(graphObject.getJsonArray("deletedEdge"));

		// System.out.println("After edges removed");
		// printGraph();
	}

	private void deleteEdges(JsonArray deletedEdges) throws Exception {
		// Fetch the list of deleted edges from JSON array
		JsonObject object;
		String to, from = "";
		Integer toIndex, fromIndex = 0;
		HashSet<Integer> currentHashSet = null;

		for (JsonValue jsonValue : deletedEdges) {
			object = (JsonObject) jsonValue;

			to = object.getString("to");
			from = object.getString("from");

			toIndex = nodeNameToIndexMapping.get(to);
			fromIndex = nodeNameToIndexMapping.get(from);

			if (toIndex != null && fromIndex != null) {
				currentHashSet = adjacencyList.get(toIndex);
				if (currentHashSet != null) {
					currentHashSet.remove(fromIndex);
					adjacencyList.put(toIndex, currentHashSet);
				}
			} else {
				if (toIndex == null)
					System.err.println("Node >>> " + to + " does not exists in the graph");
				else if (fromIndex == null)
					System.err.println("Node >>> " + from + " does not exists in the graph");
				else
					System.err.println("The edge to be deleted does not exists in the graph");
			}
		}
	}

	/*
	 * Display the graph with edges and node.
	 * 
	 * @param none
	 * 
	 * @return void
	 */
	private void printGraph() {
		System.out.println("Node names to Index Mapping\n");
		for (String key : nodeNameToIndexMapping.keySet()) {
			System.out.println(key + " >>> " + nodeNameToIndexMapping.get(key));
		}

		System.out.println("Index to Node name Mapping\n");
		for (int index = 0; index < indexToNodeName.size(); index++) {
			System.out.println(index + " >>> " + indexToNodeName.get(index));
		}

		System.out.println("Edges in the graph\n");
		for (int key : adjacencyList.keySet()) {
			System.out.print(key);
			for (int node : adjacencyList.get(key)) {
				System.out.print(" --> " + node);
			}
			System.out.println();
		}
	}

	/*
	 * Check whether the nodes in the graph are reachable from the root.
	 * 
	 * @param none
	 * 
	 * @return void
	 */
	private void dfsStack() {
		visited = new boolean[nodeNameToIndexMapping.size()];
		Stack<Integer> stack = new Stack<Integer>();
		stack.add(root);
		visited[root] = true;

		while (!stack.isEmpty()) {
			int currentNode = stack.pop();
			// System.out.println("Visited Node >>> " +
			// indexToNodeName.get(currentNode) + " >>> " + currentNode);
			// System.out.println(adjacencyList.get(currentNode));

			if (adjacencyList.get(currentNode) != null)
				for (int neighbour : adjacencyList.get(currentNode)) {
					if (!visited[neighbour]) {
						stack.add(neighbour);
						visited[neighbour] = true;
					}
				}
		}
	}

	/*
	 * Print the nodes which do not have path back to the root.
	 * 
	 * @param none
	 * 
	 * @return void
	 */
	private void getDanglingNodes() {
		System.out.println("***********************OUTPUT*********************\n");

		ArrayList<String> danglingNodes = new ArrayList<String>();
		// prepare a list of dangling Nodes
		for (int i = 0; i < visited.length; i++) {
			if (visited[i] == false)
				danglingNodes.add(indexToNodeName.get(i));
		}

		// the list of dangling nodes is printed.
		if (danglingNodes.size() == 0) {
			System.out.println("No orphan nodes exists in the graph");
		} else {
			System.out.println("The orphan nodes in the graph are");
			for (String nodeName : danglingNodes) {
				System.out.print(nodeName + " ");
			}
		}
	}
}
