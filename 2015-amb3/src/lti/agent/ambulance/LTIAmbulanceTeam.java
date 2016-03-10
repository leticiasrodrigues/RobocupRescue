package lti.agent.ambulance;

/*
 * TODO
 * - desenvolver método para encontrar rotas alternativas em
 *   caso de bloqueios.
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;

import kernel.KernelConstants;

import lti.agent.AbstractLTIAgent;
import lti.message.Message;
import lti.message.type.AgentPosition;
import lti.message.type.BuildingBurnt;
import lti.message.type.Fire;
import lti.message.type.FireExtinguished;
import lti.message.type.TaskDrop;
import lti.message.type.TaskPickup;
import lti.message.type.Victim;
import lti.message.type.VictimDied;
import lti.message.type.VictimRescued;
import lti.utils.EntityIDComparator;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import area.Sector;
import area.Sectorization;

public class LTIAmbulanceTeam extends AbstractLTIAgent<AmbulanceTeam> {
	
	//private String toWrite;
	
	private static FileWriter arq;

	private Set<EntityID> buildingsToCheck;

	private List<EntityID> refuges;

	private static enum State {
		LOADING_CIVILIAN, PATROLLING, TAKING_ALTERNATE_ROUTE, MOVING_TO_UNBLOCK,
		MOVING_TO_TARGET, MOVING_TO_REFUGE, RANDOM_WALKING, RETURNING_TO_SECTOR,
		RESCUEING, DEAD, BURIED
	};

	private State state;

	private Set<EntityID> safeBuildings;
	
	private List<EntityID> ambulanceTeamsList;
	
	private Sectorization sectorization;
	
	private Sector sector;
	
	private List<EntityID> path;
	
	private List<Pair<EntityID, EntityID>> transitionsBlocked;

	@Override
	protected void postConnect() {
		super.postConnect();
		
		inicializaVariaveis();
		
		//openArq();

		transitionsBlocked = new ArrayList<Pair<EntityID, EntityID>>(MAX_TIMESTEPS_TO_KEEP_BLOCKED_PATHS);
		for (int i = 0; i < MAX_TIMESTEPS_TO_KEEP_BLOCKED_PATHS; i++) {
			transitionsBlocked.add(i, null);
		}
		
		changeState(State.RANDOM_WALKING);
	}
	
	protected void openArq(){
		try {
			if (getArq()==null){
				LTIAmbulanceTeam.setArq(new FileWriter("Ambulance.txt"));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}

	private void inicializaVariaveis() {
		currentX = me().getX();
		currentY = me().getY();

		Set<EntityID> ambulanceTeams = new TreeSet<EntityID>(
				new EntityIDComparator());

		for (StandardEntity e : model
				.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM)) {
			ambulanceTeams.add(e.getID());
		}

		ambulanceTeamsList = new ArrayList<EntityID>(ambulanceTeams);
		
		internalID = ambulanceTeamsList.indexOf(me().getID()) + 1;
		
		refuges = new ArrayList<EntityID>();
		Collection<Refuge> ref = getRefuges();

		for (Refuge next : ref) {
			refuges.add(next.getID());
		}
		
		safeBuildings = new HashSet<EntityID>();
			
		sectorize();
		
		buildingsToCheck = new HashSet<EntityID>();
		for(EntityID buildingID : buildingIDs)
			if(sector.getLocations().keySet().contains(buildingID))
				buildingsToCheck.add(buildingID);
		
		buildingsToCheck.removeAll(refuges);
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		super.think(time, changed, heard);
		transitionsBlocked.set(currentTime % MAX_TIMESTEPS_TO_KEEP_BLOCKED_PATHS, null);
		
//		try {
//			escrever(this.toWrite, time-1);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			//e.printStackTrace();
//		}
//		
//		this.toWrite="";
//		this.toWrite = "ID "+this.internalID + ","+this.getID() //+ " Sector " + this.sector.getIndex()
//				+" Posição "+me().getPosition()+"-"+this.currentX+","+ this.currentY+"\n"; //+ " Estado " + this.state+"\n";
//				//+ " Alvos Conhecidos " + this.taskTable.size() +"\n";
//		
//		for (Map.Entry<EntityID,Set<EntityID>> targets : this.taskTable.entrySet()) {
//			int size = targets.getValue().size();
//			if(size>0){
//				//Human human = (Human) model.getEntity(targets.getKey());
//				toWrite += targets + "\n";//" Position " + human.getX()+","+human.getY()+ "\n";
//			}
//		}
		
		//for (Map.Entry<EntityID,Set<EntityID>> agents : this.knownAgents.entrySet()) {
		//	this.toWrite+=agents + " - ";
		//}

		
		//if (target!=null){
		//	toWrite += "\nAlvo " +this.target+"\n";  
		//}
		
		//Will check if the agent is alive
		if (me().getHP() == 0) {
			changeState(State.DEAD);
			//toWrite += "Estado Final " +this.state+"\n";  
			return;
		}
		
		int bad=0;
		for(Command next : heard){
			if (!goodCommunication(next)){
				bad++;
			}
		}
		if (bad>4){
			sendMessageAboutPerceptions(changed, false);
		}
		else{
			sendMessageAboutPerceptions(changed, true);
		}

		//Will check if the agent is buried
		if (me().getBuriedness() != 0) {
			if (target != null) {
				taskDropped = target;
				target = null;
				log("Dropped task: " + taskDropped);
			}
			changeState(State.BURIED);
			//toWrite += "Estado Final  " +this.state+"\n";  
			return;
		}
		
		// Will check the building for victims
		if (buildingIDs.contains(currentPosition) && emptyBuilding(changed)) {
			buildingsToCheck.remove(currentPosition);
			log("Checked one more building, now empty: " + currentPosition);
		}
		
		// If I'm blocked it's probably because there's an obstructing blockade
		if (amIBlocked(time)) {
			if (movingToUnblock()){
				//toWrite += "Estado Final (desbloqueando) " +this.state+"\n";  
				return;
			}
		}
		
		evaluateTaskDroppingAndSelection(changed);
		
		Set<Pair<EntityID, EntityID>> transitionsSet = getTransitionsSet();
		
		// Work on the task, if you have one
		if (target != null) {
			// Am I carrying a civilian?
			if (state.equals(State.LOADING_CIVILIAN) || state.equals(State.MOVING_TO_REFUGE)) {
				// Am I at a refuge?
				if (refuges.contains(currentPosition)) {
					sendUnload(time);
					log("I'm at a refuge, so unloading");
					//toWrite += "Estado Final " +this.state+"\n";  
					return;
				}
				// No? I need to get to one, then.
				path = search.breadthFirstSearchAvoidingBlockedRoads(currentPosition, transitionsSet, refuges);
				if (path == null)
					path = randomWalk();
				changeState(State.MOVING_TO_REFUGE);

				if (path != null && path.size() > 0) {
					sendMove(time, path);
					log("Path calculated and sent move: " + path);
					//toWrite += "Estado Final " +this.state+"\n";  
					return;
				}
			} else {
				Human victim = (Human) model.getEntity(target);

				if (victim.getPosition().equals(currentPosition)) {
					if (victim.isBuriednessDefined() &&
							victim.getBuriedness() != 0) {
						sendRescue(time, target);
						changeState(State.RESCUEING);
						log("Rescueing " + victim + " buriedness: " + victim.getBuriedness());
					} else if (victim instanceof Civilian) {
						sendLoad(time, target);
						changeState(State.LOADING_CIVILIAN);
						log("Loading civilian " + victim);
					}
					//toWrite += "Estado Final " +this.state+"\n";  
					return;
				} else {
					path = search.breadthFirstSearchAvoidingBlockedRoads(currentPosition, transitionsSet, victim.getPosition());
					if (path != null && path.size() > 0) {
						changeState(State.MOVING_TO_TARGET);
						sendMove(time, path);
						log("Path calculated and sent move: " + path);
						//toWrite += "Estado Final " +this.state+"\n";  
						return;
					}
				}
			}
		}

		// Move around the map
		// Nothing to do here. Moving on.
		safeBuildings = getSafeBuildings(changed);

		getMoreSafeBuildings();
		
		// Using an aux list of buildings to check
		Set<EntityID> auxBuildingsToCheck = new HashSet<EntityID>(buildingsToCheck);
		
		// We remove all the buildings that aren't safe to enter
		auxBuildingsToCheck.retainAll(safeBuildings);
		
		// We then try to go to the closest building not yet checked
		path = search.breadthFirstSearchAvoidingBlockedRoads(me().getPosition(), transitionsSet, auxBuildingsToCheck);
		
		// If we find a path, we set it as the next location
		if(path != null && path.size() > 0){
			sendMove(time, path);
			changeState(State.PATROLLING);
			//toWrite += "Estado Final " +this.state+"\n";  
			return;
		}

		path = randomWalk();
		if(path != null && path.size() > 0) {
			sendMove(time, path);
			log("Path calculated and sent move: " + path);
			//toWrite += "Estado Final " +this.state+"\n";  
		}
	}

	private Set<Pair<EntityID, EntityID>> getTransitionsSet() {
		String ss = "";
		Set<Pair<EntityID, EntityID>> transitionsSet =
				new HashSet<Pair<EntityID, EntityID>>();
		for (Pair<EntityID, EntityID> transition : transitionsBlocked) {
			if (transition != null) {
				transitionsSet.add(transition);
				ss += transition.first() + "->" + transition.second() + ", ";
			}
		}
		log("transitionsBlocked: " + ss);
		return transitionsSet;
	}
	
	private boolean movingToUnblock() {
		if (path != null && path.size() >= 1 &&
				path.indexOf(currentPosition) < path.size()-1) {
			int ii = currentTime % MAX_TIMESTEPS_TO_KEEP_BLOCKED_PATHS;
			
			EntityID ee = path.get(0);
			if (path.indexOf(currentPosition) >= 0)
				ee = path.get(path.indexOf(currentPosition)+1);
			
			transitionsBlocked.set(ii,
					new Pair<EntityID, EntityID>(currentPosition, ee));
		}
		
		path = randomWalk();
		if (path != null && path.size() > 0) {
			sendMove(currentTime, path);
			log("Path calculated to unblock and sent move: " + path);
			return true;
		}
		return false;
	}
	
	private void sendMessageAboutPerceptions(ChangeSet changed, boolean complete) {
		// Send a message about all the perceptions
		Message msg;
		if (complete){
			msg = composeMessageComplete(changed);
		}
		else{
			msg = composeMessageIncomplete(changed);
		}

		if (this.channelComm) {
			if (!msg.getParameters().isEmpty() && !channelList.isEmpty()) {
				for (Pair<Integer,Integer> channel : channelList) {
					sendSpeak(currentTime, channel.first(),
							msg.getMessage(channel.second().intValue()));
				}
			}
		}
	}
	

	private void evaluateTaskDroppingAndSelection(ChangeSet changed) {
		// Evaluate task dropping
		if (target != null) {
			dropTask(currentTime, changed);
			if (target == null){
				log("Dropped task: " + taskDropped);
				//toWrite += "Tarefa abandonada "+taskDropped+ "\n";
			}
		}

		/*
		 * Choose a victim to rescue from the task table, in the following
		 * priority order: ambulances, fire brigades, civilians and police
		 * forces
		 */
		if (target == null) {
			target = selectTask();
			if (target != null)
				log("Selected task: " + model.getEntity(target));
		}
	}
		
	protected List<EntityID> randomWalk(){
		List<EntityID> result = new ArrayList<EntityID>();
		EntityID current = currentPosition;
		
		if (!sector.getLocations().keySet().contains(currentPosition)) {
			List<EntityID> local = new ArrayList<EntityID>(sector
					.getLocations().keySet());
			changeState(State.RETURNING_TO_SECTOR);
			return search.breadthFirstSearchAvoidingBlockedRoads(currentPosition, getTransitionsSet(), local);
		}

		for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
			result.add(current);
			List<EntityID> possible = new ArrayList<EntityID>();

			for (EntityID next : sector.getNeighbours(current))
				if (model.getEntity(next) instanceof Road)
					possible.add(next);

			Collections.shuffle(possible, new Random(me().getID().getValue()
					+ currentTime));
			boolean found = false;

			for (EntityID next : possible) {
				if (!result.contains(next)) {
					current = next;
					found = true;
					break;
				}
			}
			if (!found)
				break; // We reached a dead-end.
		}

		result.remove(0); // Remove actual position from path
		changeState(State.RANDOM_WALKING);
		return result;
	}

	/**
	 * Check if this ambulance is carrying a civilian.
	 * 
	 * @return true if the ambulance is carrying a civilian, false otherwise.
	 */
	private boolean targetOnBoard() {
		if (target != null && model.getEntity(target) instanceof Human) {	
			Human t = (Human) model.getEntity(target);
			if (t.isPositionDefined())
				return t.getPosition().equals(getID());
		}

		log("Position of the human target not defined. Can't say if it's on board");
		return false;
	}

	@Override
	protected boolean amIBlocked(int time) {
		return math.geom2d.Point2D.distance(lastX, lastY, currentX, currentY) < MIN_WALK_LENGTH &&
				isMovingState() &&
				time > 3;
	}

	/**
	 * Choose a vicitm from the Task Table to rescue according to the following
	 * pritority: ambulances, fire brigades, civilians and police forces.
	 * 
	 * @return The victim's ID.
	 */
	@Override
	protected EntityID selectTask() {
		Human victim = pickVictim2();

		if (victim != null) {
			for (Set<EntityID> agents : taskTable.values()) {
				if (agents != null)
					agents.remove(me().getID());
			}

			if (!taskTable.containsKey(victim.getID()))
				taskTable.put(victim.getID(), new HashSet<EntityID>());
			taskTable.get(victim.getID()).add(me().getID());
			return victim.getID();
		}

		return null;
	}
		
	private Human pickVictim() {
		int totalDistance = 0;
		double savePriority = 0, newSavePriority;
		Human result = null;

		for (EntityID next : taskTable.keySet()) {
			//toWrite+=next.getValue();
			if (!next.equals(taskDropped)) {
				Human victim = (Human) model.getEntity(next);
				int distanceFromAT = model.getDistance(getID(),
						victim.getPosition());
				
				// For now, it ignores the distance to the refuge
				int distanceToRefuge = 0;
				
				totalDistance = distanceToRefuge + distanceFromAT;
				newSavePriority = getVictimSavePriority(victim, totalDistance);
				//toWrite+=" "+newSavePriority+"\n";
				if(newSavePriority > savePriority){
					result = victim;
					savePriority = newSavePriority;
				}
			}
		}
		log("FINAL_SCORE: " + result + " -> " + savePriority);

		return result;
	}

	private Human pickVictim2() {
		
		List<EntityID> agents = new ArrayList<EntityID>(knownAgents.keySet());
		List<EntityID> victims = new ArrayList<EntityID>(taskTable.keySet());
		//toWrite += "Agentes: "+agents.toString()+" Vitimas: "+victims+"\n";				
		
		/*for (Map.Entry<EntityID,Set<EntityID>> targets : this.taskTable.entrySet()) {
			//os limites vão aqui também
			//toWrite+="Targets: "+targets+" Key: "+targets.getKey()+" Value: "+targets.getValue()+
					//" Tamanho: "+targets.getValue().size()+"\n";
			int size = targets.getValue().size();
			if(size>0){
				victims.remove(targets.getKey());
				agents.remove(targets.getValue());
				//toWrite+="Excluidos: "+targets.getKey()+","+targets.getValue()+"\n";
			}
		}*/
		
		//toWrite += "Corrigido-> Agentes: "+agents.toString()+" Vitimas: "+victims+"\n";	
		
		for (EntityID next : victims){
			int distance = 0;
			double newSavePriority;
			double priority = 0;
			EntityID responsable = null;
			Human result = null;
			for (EntityID entity : agents){
				result = (Human) model.getEntity(next);
				distance = model.getDistance(next, entity);
				newSavePriority = getVictimSavePriority (result, distance);
				//toWrite+="Teste: "+result.toString() + ","+distance+","+newSavePriority+"\n";
				if (newSavePriority > priority){
					priority = newSavePriority;
					responsable = entity;
				}
			
				//toWrite+="Selecionado: "+next+ ","+responsable+"\n";
				//toWrite+="Responsable: "+responsable+" this.getID: "+this.getID()+"\n";
				if (responsable.equals(this.getID())){
					//toWrite += "Tempo Total: "+(System.currentTimeMillis() - i)+"\nAlvo: "+ result.toString()+"\n";
					log("FINAL_SCORE: " + result + " -> " + priority);
					return result;
				}else{
					agents.remove(responsable);
				}
			}
		}
		//toWrite += "Tempo Total: "+(System.currentTimeMillis() - i)+"\nAlvo: Nulo\n";
		return null;
	}
		
		
	
	private boolean evaluateSavingConditions(Human victim, int totalDistance){
		
		//Se a vitima nao estiver em um refugio
		if(!(victim.isPositionDefined() && refuges.contains(victim.getPosition()))){
			//se a vitima ainda nao estiver sendo salva
			if(!isVictimBeingRescued(victim)){
				//Se ainda e possivel salvar a vitima
				if (isSavable(victim, totalDistance)) {
					return true;
				}
			}
		}
		return false;
	}
		
	private Boolean isVictimBeingRescued(Human victim){		
		return  taskTable.keySet().contains(victim.getID()) &&
				!taskTable.get(victim.getID()).isEmpty();		
	}

	private boolean isSavable(Human victim, int totalDistance) {
		//Calcula quantos ciclos a vitima tem de vida
		int remainingCycles = 0;
		if (victim.isDamageDefined() && victim.getDamage() != 0)
			remainingCycles = victim.getHP() / victim.getDamage();
		else
			return true;

		// Calcula quantos ciclos precisa para salvar a vitima
		// TODO: considerar tempo de rescue e load
		int necessaryCycles = 0;

		if (maxDistanceTraveledPerCycle != 0)
			necessaryCycles = totalDistance / maxDistanceTraveledPerCycle;

		if (necessaryCycles > remainingCycles)
			return false;

		return true;
	}

	private double getVictimSavePriority(Human victim, int totalDistance) {
		
		double savability = 5 - (totalDistance - 10000) / 20000;
		int b = 0;
		if(victim.isBuriednessDefined())
			b = victim.getBuriedness();
		boolean isPossibleToSave = evaluateSavingConditions(victim, totalDistance);
		boolean isSamePosition = false;
		if(victim.isPositionDefined())
			isSamePosition = victim.getPosition().equals(currentPosition);
		
		savability += (200 - b) / 40.0;
		savability += isPossibleToSave ? 2 : 0;
		savability += isSamePosition ? 5 : 0;
		
		//log("SCORE: " + victim + " -> " + savability + " (d:" + totalDistance + ", b:" + b + ", ip:" + isPossibleToSave);
		return savability;
	}

	@Override
	protected void refreshTaskTable(ChangeSet changed) {
		Set<EntityID> victims = new HashSet<EntityID>();
		List<StandardEntityURN> urns = new ArrayList<StandardEntityURN>();
		
		urns.add(StandardEntityURN.AMBULANCE_TEAM);
		urns.add(StandardEntityURN.FIRE_BRIGADE);
		urns.add(StandardEntityURN.CIVILIAN);
		urns.add(StandardEntityURN.POLICE_FORCE);
		
		/*
		 * Victims: - AT, FB and PF who are alive and buried; - Civilians who
		 * are alive and are either buried or inside a building that is not a
		 * refuge.
		 */
		for (StandardEntityURN entUrn : urns) {
			Set<EntityID> nonRefugeBuildings = new HashSet<EntityID>(
					buildingIDs);
			nonRefugeBuildings.removeAll(refuges);

			for (StandardEntity next : model.getEntitiesOfType(entUrn)) {
				Human h = (Human) next;
				if (h.isHPDefined() && h.getHP() != 0) {
					if (entUrn.equals(StandardEntityURN.CIVILIAN)
							&& nonRefugeBuildings.contains(h.getPosition())) {
						victims.add(h.getID());
					} else if (h.isBuriednessDefined() &&
							h.getBuriedness() != 0 &&
							!h.getID().equals(getID())) {
						victims.add(next.getID());
					}
				}
			}
		}

		Set<EntityID> toRemove = new HashSet<EntityID>();

		/*
		 * Remove victims who are not found in the position they were thought to
		 * be; they have already been rescued.
		 */
		for (EntityID next : victims) {
			Human exVictim = (Human) model.getEntity(next);
			if (exVictim.getPosition().equals(currentPosition) &&
					!changed.getChangedEntities().contains(next)) {
				exVictim.undefineBuriedness();
				exVictim.undefinePosition();
				toRemove.add(next);
			}
		}

		victims.removeAll(toRemove);
		
		//Include as victims the target loaded
		if (targetOnBoard())
			victims.add(target);
		
		taskTable.keySet().retainAll(victims);
		victims.removeAll(taskTable.keySet());

		for (EntityID next : victims) {
			taskTable.put(next, new HashSet<EntityID>());
		}
	}

	/**
	 * Check the building to see if there are victims inside it.
	 * 
	 * @param changed
	 *            The ChangeSet of the ambulance.
	 * @return false, if there is still a victim inside the building, otherwise
	 *         returns true.
	 */
	private boolean emptyBuilding(ChangeSet changed) {
		Set<EntityID> visible = changed.getChangedEntities();
		visible.retainAll(taskTable.keySet());

		for (EntityID next : visible) {
			Human human = (Human) model.getEntity(next);
			if (human.getPosition().equals(currentPosition)) {
				return false;
			}
		}

		return true;
	}

	private void getMoreSafeBuildings() {
		Set<EntityID> safe = new HashSet<EntityID>();

		for (StandardEntity next : model
				.getEntitiesOfType(StandardEntityURN.BUILDING)) {
			if (!((Building) next).isOnFire()) {
				if (!((Building) next).isBrokennessDefined())
					safe.add(next.getID());
			}
		}

		safe.removeAll(refuges);
		safeBuildings.retainAll(safe);
		safe.removeAll(safeBuildings);
		safeBuildings.addAll(safe);
	}

	@Override
	protected void dropTask(int time, ChangeSet changed) {
		if (!targetOnBoard() && !taskTable.keySet().contains(target)) {
			taskDropped = target;
			target = null;
		}		
	}
	
	private boolean isMovingState() {
		List<State> ss = new ArrayList<State>();
		ss.add(State.PATROLLING);
		ss.add(State.TAKING_ALTERNATE_ROUTE);
		ss.add(State.MOVING_TO_TARGET);
		ss.add(State.MOVING_TO_REFUGE);
		ss.add(State.RANDOM_WALKING);
		ss.add(State.RETURNING_TO_SECTOR);
		ss.add(State.MOVING_TO_UNBLOCK);

		return ss.contains(state);
	}
	
	private void changeState(State state) {
		this.state = state;
		log("Changed state to: " + this.state);
	}
	
	private void sectorize(){
		sectorization = new Sectorization(model, neighbours,
				ambulanceTeamsList.size(), verbose);
		
		sector = sectorization.getSector(internalID);
		
		log("Defined sector: " + sector);
	}
	
	/*protected void patrolling (int time){
		
		changeState(State.PATROLLING);
		if (amIBlocked(time)){
			return;						//se está bloqueado e não há comunicação, não há o que fazer
		}
		
		path = randomWalk();			//um caminho a ser seguido
		
	}*/
	
	private Message composeMessageIncomplete(ChangeSet changed) {
		return composeMessageIncomplete(changed, new Message());
	}
	
	/** Compõe uma mensagem para ser enviada de acordo com o que o agente vê */
	private Message composeMessageIncomplete(ChangeSet changed, Message message) {

		// Pedir socorro se está bloqueado ou buried
		if (((Human)me()).getBuriedness() != 0
				|| (amIBlocked(currentTime) && (currentTime - lastTimeNotBlocked >= 5))){
			message = addRescueMyselfMessage(message);
			
		}else if (hasRequestedToBeSaved){
			message = addGotRescuedMessage(message);
		}
		
		for (EntityID buildingID : getVisibleEntitiesOfType(
				StandardEntityURN.BUILDING, changed)) {
			Building building = (Building) model.getEntity(buildingID);

			/*
			 * Se o prédio está em chamas e este incêndio não é conhecido, deve
			 * se enviar uma mensagem falando sobre ele
			 */
			if (building.isOnFire() && !buildingsOnFire.contains(buildingID)) {
				Fire fire = new Fire(buildingID.getValue(),
						building.getGroundArea(), building.getFloors(),
						building.getFieryness());
				message.addParameter(fire);
				// O incêndio passa a ser conhecido
				buildingsOnFire.add(buildingID);

			}
			/*
			 * Se o prédio não está em chamas, mas havia um incêndio nele
			 * anteriormente, há duas opções: 1) O incêndio consumiu todo o
			 * prédio; 2) O incêndio foi extinguido
			 */
			else if (!building.isOnFire()
					&& buildingsOnFire.contains(buildingID)) {

				if (building.getFierynessEnum().equals(Fieryness.BURNT_OUT)) {
					BuildingBurnt burnt = new BuildingBurnt(
							buildingID.getValue());
					message.addParameter(burnt);

				} else if (building.getFierynessEnum().compareTo(
						Fieryness.INFERNO) > 0) {
					FireExtinguished extinguished = new FireExtinguished(
							buildingID.getValue());
					message.addParameter(extinguished);
				}

				// Não se sabe mais de nenhum incêndio neste prédio
				buildingsOnFire.remove(buildingID);
			}
		}

		/*
		 * Se algum bloqueio visível não é conhecido, deve-se apenas 
		 * adicioná-lo à própria lista de bloqueios conhecidos
		 */
		for (EntityID blockadeID : getVisibleEntitiesOfType(
				StandardEntityURN.BLOCKADE, changed)) {

			if (!knownBlockades.contains(blockadeID)) {
				knownBlockades.add(blockadeID);
			}
		}

		Set<EntityID> toRemove = new HashSet<EntityID>();

		/*
		 * Se algum bloqueio conhecido não se encontra mais em seu lugar, ele
		 * foi removido
		 */
		for (EntityID blockadeID : knownBlockades) {
			if (model.getEntity(blockadeID) == null) {
				toRemove.add(blockadeID);
			}
		}

		knownBlockades.removeAll(toRemove);

		Set<EntityID> victims = new HashSet<EntityID>();
		StandardEntityURN[] urns = { StandardEntityURN.AMBULANCE_TEAM,
				StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.CIVILIAN,
				StandardEntityURN.POLICE_FORCE };
		/*
		 * Victims: - AT, FB and PF who are alive and buried; - Civilians who
		 * are alive and are either buried or inside a building that is not a
		 * refuge.
		 */
		for (int i = 0; i < 4; i++) {
			Set<EntityID> nonRefugeBuildings = new HashSet<EntityID>(
					buildingIDs);
			nonRefugeBuildings.removeAll(getRefuges());

			for (EntityID next : getVisibleEntitiesOfType(urns[i], changed)) {
				Human human = (Human) model.getEntity(next);
				if (human.isHPDefined() && human.getHP() != 0) {
					if (urns[i].equals(StandardEntityURN.CIVILIAN)
							&& nonRefugeBuildings.contains(human.getPosition())) {
						victims.add(next);

					} else if (human.isBuriednessDefined()
							&& human.getBuriedness() != 0
							&& nonRefugeBuildings.contains(human.getPosition())) {
						victims.add(next);
					}
				}
			}
		}

		/*
		 * Se alguma vítima visível não é conhecida, deve-se enviar uma mensagem
		 * relatando-a
		 */
		for (EntityID next : victims) {
			StandardEntity entity;
			Human human;
			int urn;

			if (!knownVictims.contains(next)) {
				entity = model.getEntity(next);

				if (entity != null && entity instanceof Human) {
					switch (entity.getStandardURN()) {
					case AMBULANCE_TEAM:
						human = (AmbulanceTeam) entity;
						urn = 0;
						break;

					case FIRE_BRIGADE:
						human = (FireBrigade) entity;
						urn = 1;
						break;

					case POLICE_FORCE:
						human = (PoliceForce) entity;
						urn = 2;
						break;

					default:
						human = (Civilian) entity;
						urn = 3;
					}

					Victim victim = new Victim(next.getValue(), human
							.getPosition().getValue(), human.getHP(),
							human.getDamage(), human.getBuriedness(), urn);
					message.addParameter(victim);
					knownVictims.add(next);
				}
			}
		}

		toRemove = new HashSet<EntityID>();

		/*
		 * Se alguma vítima conhecida não está no lugar aonde deveria estar ou
		 * morreu, deve-se enviar uma mensagem relatando a situação
		 */
		for (EntityID victim : knownVictims) {
			StandardEntity entity = model.getEntity(victim);

			if (entity != null && entity instanceof Human) {
				Human human = (Human) entity;

				if (human.isHPDefined() && human.getHP() == 0) {
					VictimDied death = new VictimDied(victim.getValue());
					message.addParameter(death);
					toRemove.add(victim);
				} else if (human.isBuriednessDefined() &&
						human.getBuriedness() == 0 && human instanceof Civilian) {
					VictimRescued rescue = new VictimRescued(victim.getValue());
					message.addParameter(rescue);
					toRemove.add(victim);
				}
			}
		}

		knownVictims.removeAll(toRemove);

		/* Se uma tarefa foi abandonada, envia-se uma mensagem */
		if (taskDropped != null) {
			TaskDrop drop = new TaskDrop(taskDropped.getValue());
			message.addParameter(drop);

			/* Se uma tarefa foi escohida, envia-se uma mensagem relatando-a */
			if (target != null) {
				TaskPickup task = new TaskPickup(target.getValue());
				message.addParameter(task);
			}
		}
		/* Se uma tarefa foi escohida, envia-se uma mensagem relatando-a */
		if (target != null && currentTime == config.getIntValue(KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			TaskPickup task = new TaskPickup(target.getValue());
			message.addParameter(task);
		}
		
		/*Enviar a posição do agente*/
		StandardEntity entity;
		entity = model.getEntity(this.getID());
		int urn=0;

		if (entity != null && entity instanceof Human) {
			switch (entity.getStandardURN()) {
			case AMBULANCE_TEAM:				
				urn =0;
				break;
			case FIRE_BRIGADE:
				urn = 1;
				break;
			case POLICE_FORCE:
				urn = 2;
				break;
			}
		}
		
		AgentPosition agentPosition = new AgentPosition(this.getID().getValue(), this.currentPosition.getValue(), urn);
		message.addParameter(agentPosition);
		return message;
	}
	
	private void escrever(String out, int time) throws IOException{
		
		PrintWriter gravarArq = new PrintWriter(getArq()); 		//algum obj que vai fazer imprimir em si
	    
		gravarArq.printf(out+" "+time+"\n"+"\n"); 
		
		if (time==299){
			LTIAmbulanceTeam.getArq().close();
		}
	}

	public static FileWriter getArq() {
		return arq;
	}

	public static void setArq(FileWriter arq) {
		LTIAmbulanceTeam.arq = arq;
	}
}