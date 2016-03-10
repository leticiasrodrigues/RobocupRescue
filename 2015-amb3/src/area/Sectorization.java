package area;

import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class Sectorization {

	private int numberOfAgents;
	
	private StandardWorldModel model;
	
	protected Map<EntityID, Set<EntityID>> worldgraph;
	
	private int numberOfSectors;

	private Set<Sector> sectors;
	
	private List<Sector> sectorsList;
	
	private int nEntitiesTotal;
	
	public Sectorization(StandardWorldModel model, Map<EntityID, Set<EntityID>> worldGraph,
			int numberOfAgents, boolean verbose) {
		
		this.model = model;
		this.worldgraph = worldGraph;
		this.numberOfAgents = numberOfAgents;
		this.numberOfSectors = defineNumberOfDivisions();
		sectorize();
		this.nEntitiesTotal = getTotalEntities();
		
		if (verbose)
			printSectorsToFile();
	}

	private int getTotalEntities() {
		int total = 0;
		for (Sector s : sectors)
			total += s.getLocations().keySet().size();
		return total;
	}
	
	public Set<Sector> getSectors() {
		return this.sectors;
	}
	
	public List<Sector> getSectorsAsList() {
		return this.sectorsList;
	}
	
	/**
	 * @param sectors
	 */
	public Sector getSector(int internalID) {
		int mypos = internalID;
		int nAgents = numberOfAgents;

		if (mypos <= sectorsList.size())
			return sectorsList.get(mypos - 1);

		mypos -= sectorsList.size();
		nAgents -= sectorsList.size();
		int nEntities = 0;
		for (Sector next : sectorsList) {
			nEntities += next.getLocations().keySet().size();
			if (((double) mypos / nAgents) <= ((double) nEntities / nEntitiesTotal))
				return next;
		}

		return sectorsList.get(0);
	}
	
	/**
	 * Try to define the number of divisions which can maximize the performance
	 * of the sectorization
	 * 
	 * @return numberOfDivisions
	 */
	private int defineNumberOfDivisions() {
		int numberOfDivisions = 1;

		if (numberOfAgents > 1)
			numberOfDivisions = numberOfAgents / 2;

		Pair<Integer, Integer> factors = factorization(numberOfDivisions);
		// Prime numbers bigger than 3 (5, 7, 11, ...)
		if (factors.first() == 1 && factors.second() > 3)
			numberOfDivisions--;

		return numberOfDivisions;
	}
	
	/**
	 * Divides the map into sectors, assigning the "Area" entities contained in
	 * them to each Police Force.
	 * 
	 * @param worldgraph
	 *            The table of adjacency of the graph to be sectorized.
	 */
	private void sectorize() {	
		Rectangle2D bounds = model.getBounds();

		// Get the two points that define the map as rectangle.
		int x = (int) bounds.getX();
		int y = (int) bounds.getY();
		int w = (int) bounds.getWidth();
		int h = (int) bounds.getHeight();

		// Get the number of divisions on each dimension.
		Pair<Integer, Integer> factors = factorization(numberOfSectors);

		int widthDiv;
		int heightDiv;

		if (w < h) {
			widthDiv = Math.min(factors.first(), factors.second());
			heightDiv = Math.max(factors.first(), factors.second());
		} else {
			widthDiv = Math.max(factors.first(), factors.second());
			heightDiv = Math.min(factors.first(), factors.second());
		}

		// Divide the map into sectors
		sectors = new TreeSet<Sector>();

		for (int i = 0; i < heightDiv; i++) {
			for (int j = 0; j < widthDiv; j++) {
				sectors.add(new Sector(x + j * (w / widthDiv), y + i
						* (h / heightDiv), w / widthDiv, h / heightDiv,
						widthDiv * i + j + 1));
			}
		}
		
		buildLocations();
		
		sectorsList = new ArrayList<Sector>(sectors);
	}

	/**
	 * Factorize a number into the two closest factors possible.
	 * 
	 * @param n
	 *            The number to be factorized.
	 * @return The pair of factors obtained.
	 */
	private Pair<Integer, Integer> factorization(int n) {
		for (int i = (int) Math.sqrt(n); i >= 1; i--)
			if (n % i == 0)
				return new Pair<Integer, Integer>(i, n / i);
		return new Pair<Integer, Integer>(1, n);
	}
	
	/**
	 * Builds a set of connected entities to each sector.
	 * 
	 * @param sectors
	 *            The set of sectors the entities should be allocated between.
	 * 
	 * @return The set of sectors received, with the entities allocated.
	 */
	private void buildLocations() {
		Map<Integer, Set<Set<EntityID>>> connectedSubgraphs = new HashMap<Integer, Set<Set<EntityID>>>();
		/*
		 * connectedSubgraphs: maps each sector (through its index) to the set
		 * of connected subgraphs it contains
		 */

		// Form the subgraphs of each sector
		for (Sector s : sectors) {
			// Group the entities contained in the sector into subgraphs
			connectedSubgraphs.put(s.getIndex(), group(s));
		}

		// Get the main subgraph (the largest one) of each sector.
		for (Sector s : sectors) {
			int maxSize = 0;
			Set<EntityID> largest = null;

			if (!connectedSubgraphs.get(s.getIndex()).isEmpty()) {
				for (Set<EntityID> next : connectedSubgraphs.get(s.getIndex())) {
					if (next.size() >= maxSize) {
						maxSize = next.size();
						largest = next;
					}
				}

				for (EntityID next : largest) {
					s.addVertex(next);
				}
				connectedSubgraphs.get(s.getIndex()).remove(largest);
			}
		}

		Set<Set<EntityID>> subgraphs = new HashSet<Set<EntityID>>();

		for (Set<Set<EntityID>> next : connectedSubgraphs.values()) {
			subgraphs.addAll(next);
		}

		// Allocate the remaining subgraphs
		while (!subgraphs.isEmpty()) {
			List<Set<EntityID>> allocated = new ArrayList<Set<EntityID>>(
					allocateSubgraphs(subgraphs, sectors));

			/*
			 * Remove the allocated subgraphs from the subgraphs set
			 */
			while (!allocated.isEmpty()) {
				Set<EntityID> subgraph = allocated.get(0);

				if (subgraphs.contains(subgraph)) {
					subgraphs.remove(subgraph);
					allocated.remove(0);
				}
			}
		}

		// Map each entity of each sector to the set of its neighbours contained
		// in the sector
		for (Sector s : sectors) {
			for (EntityID location : s.getLocations().keySet()) {
				for (EntityID neighbour : worldgraph.get(location)) {
					if (s.getLocations().containsKey(neighbour)) {
						if (s.getNeighbours(location) == null) {
							s.getLocations().put(location,
									new HashSet<EntityID>());
						}
						s.getNeighbours(location).add(neighbour);
					}
				}
			}
		}
	}

	/**
	 * Group the entities contained in a sector into connected subgraphs.
	 * 
	 * @param sector
	 *            The sector whose entities should be grouped.
	 * 
	 * @return The set of subgraphs produced
	 */
	private Set<Set<EntityID>> group(Sector sector) {
		/*
		 * Get the entities that are partially or entirely inside the bounds of
		 * the sector
		 */
		Collection<StandardEntity> entities = model.getObjectsInRectangle(
				(int) sector.getBounds().getMinX(), (int) sector.getBounds()
						.getMinY(), (int) sector.getBounds().getMaxX(),
				(int) sector.getBounds().getMaxY());

		Set<EntityID> locations = new HashSet<EntityID>();

		/*
		 * Determine the entities geographically contained in the sector, so
		 * that each entity belongs to an unique sector
		 */
		for (StandardEntity next : entities) {
			if (next instanceof Area) {
				if (sector.containsCenter((Area) next)) {
					locations.add(next.getID());
				}
			}
		}

		Set<Set<EntityID>> subgraphs = new HashSet<Set<EntityID>>();
		Set<EntityID> visited = new HashSet<EntityID>();
		List<EntityID> nodesLeft = new ArrayList<EntityID>(locations);

		// Group the nodes into connected subgraphs
		while (!nodesLeft.isEmpty()) {
			// connected: the current subgraph being constructed
			Set<EntityID> connected = new HashSet<EntityID>();
			// border: set of nodes to be expanded
			Set<EntityID> border = new HashSet<EntityID>();

			border.add(nodesLeft.remove(0));

			// Expand each subgraph
			while (!border.isEmpty()) {
				Set<EntityID> newBorder = new HashSet<EntityID>();

				for (EntityID e : border) {
					for (EntityID next : worldgraph.get(e)) {
						if (locations.contains(next) && !visited.contains(next)
								&& !border.contains(next)) {
							newBorder.add(next);
						}
					}
					visited.add(e);
				}
				connected.addAll(border);
				nodesLeft.removeAll(border);
				border = newBorder;
			}
			subgraphs.add(connected);
		}

		return subgraphs;
	}

	/**
	 * Allocate each subgraph of a sector to the sector with the smallest main
	 * graph, if possible
	 * 
	 * @param subgraphs
	 *            The set of subgraphs to be allocated.
	 * 
	 * @param sectors
	 *            The set of sectors the subgraphs can be allocated to.
	 * 
	 * @return The set of subgraphs successfully allocated.
	 */
	private Set<Set<EntityID>> allocateSubgraphs(Set<Set<EntityID>> subgraphs,
			Set<Sector> sectors) {
		Set<Set<EntityID>> allocated = new HashSet<Set<EntityID>>();

		for (Set<EntityID> subgraph : subgraphs) {
			int smallestSectorSize = Integer.MAX_VALUE;
			Sector smallestSector = null;

			for (EntityID node : subgraph) {
				Set<Sector> evaluatedSectors = new HashSet<Sector>();

				for (EntityID next : worldgraph.get(node)) {
					for (Sector s : sectors) {
						if (s.getLocations().containsKey(next)
								&& !evaluatedSectors.contains(s)) {
							if (s.getLocations().size() < smallestSectorSize) {
								smallestSectorSize = s.getLocations().size();
								smallestSector = s;
							}

							evaluatedSectors.add(s);
						}
					}
				}
			}

			if (smallestSector != null) {
				for (EntityID next : subgraph) {
					smallestSector.addVertex(next);
				}
				allocated.add(subgraph);
			}
		}

		return allocated;
	}

	private void printSectorsToFile() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(
					"setores.txt"));

			out.write("Map dimension: "
					+ model.getWorldBounds().first().first() + ","
					+ model.getWorldBounds().first().second() + " "
					+ model.getWorldBounds().second().first() + ","
					+ model.getWorldBounds().second().second());
			out.newLine();
			
			out.write("Number of entities: " + nEntitiesTotal);
			out.newLine();
			out.write("Number of divisions: " + numberOfSectors);
			out.newLine();
			out.newLine();
			out.flush();

			for (Sector next : sectors) {
				out.write("Sector " + next.getIndex());
				out.newLine();
				Rectangle2D bounds = next.getBounds2D();
				out.write("Bottom-left: " + bounds.getX() + ", "
						+ bounds.getY());
				out.newLine();
				out.write("Width x Height: " + bounds.getWidth() + " x "
						+ bounds.getHeight());
				out.newLine();
				if (next.getLocations() != null) {
					out.write("Number of entities: "
							+ next.getLocations().keySet().size());
					out.newLine();
					out.flush();

					for (EntityID areaID : next.getLocations().keySet()) {
						out.write(areaID.toString());
						out.newLine();
					}

					out.newLine();
				} else {
					out.write("Empty sector");
					out.newLine();
					out.newLine();
					out.flush();
				}
			}
			out.close();

			createDotFile(sectors, "setor");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createDotFile(Set<Sector> sectors, String fileName) {
		BufferedWriter out;
		try {
			for (Sector sector : sectors) {
				out = new BufferedWriter(new FileWriter(fileName
						+ sector.getIndex() + ".dot"));
				out.write("graph sector" + sector.getIndex() + " {\n");

				for (EntityID node : sector.getLocations().keySet()) {
					out.write("\tsubgraph sg_" + node.toString() + "{\n");
					for (EntityID neighbour : sector.getNeighbours(node)) {
						out.write("\t\t" + node.toString() + " -- "
								+ neighbour.toString());
						out.newLine();
					}
					out.write("\t}\n\n");
					out.flush();
				}
				out.write("}");
				out.close();
			}
		} catch (IOException e) {
		}
	}
}
