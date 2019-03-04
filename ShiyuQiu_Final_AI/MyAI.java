// ======================================================================
// FILE:        MyAI.java
//
// AUTHOR:      Abdullah Younis
//
// DESCRIPTION: This file contains your agent class, which you will
//              implement. You are responsible for implementing the
//              'getAction' function and any helper methods you feel you
//              need.
//
// NOTES:       - If you are having trouble understanding how the shell
//                works, look at the other parts of the code, as well as
//                the documentation.
//
//              - You are only allowed to make changes to this portion of
//                the code. Any changes to other portions of the code will
//                be lost when the tournament runs your code.
// ======================================================================
import java.util.*; 
import java.lang.Math;

public class MyAI extends Agent
{
	enum Direction
	{
		RIGHT,
		LEFT,
		UP,
		DOWN;
	}

	public int width;
	public int length;
	Pair current;
	Stack<Action> prev_moves;
	Pair prev_cell;
    boolean gold;
	boolean goBack;
	Direction currentDir;
    int headingHomeCount;
    boolean stepedBack;
    Stack<Pair> visitedCell;
	ArrayList<ArrayList<node>> matrix;
	Pair anotherCell;
	boolean afterBump;
	int choiceAtFirst;
	Pair wumpusCell;
	int beginBreezeCount;
	int beginStenchCount;
	boolean shooting;
	int shootCount;
	boolean wumpusDead;
	boolean wumpusFound;
	int score;
	boolean resized;
	boolean WkilledAtBeginning;
	//boolean fistVisit;

	public MyAI ( )
	{
		this.width = 4;
		this.length = 4;
		this.current = new Pair(0 ,0);
		this.prev_moves = new Stack<Action>();
		this.goBack = false;
		this.currentDir = Direction.RIGHT;
		this.headingHomeCount = 0;
		this.stepedBack = false;
		this.visitedCell = new Stack<Pair>();
		this.matrix = new ArrayList<ArrayList<node>>();
		this.afterBump = false;
		this.choiceAtFirst = 3;
		this.beginBreezeCount = 0;
		this.beginStenchCount = 0;
		this.shooting = false;
		this.shootCount = 0;
		this.wumpusDead = false;
		this.wumpusFound = false;
		this.score = 0;
		this.resized = false;
		this.WkilledAtBeginning = false;
		ArrayList<node> innerList1 = new ArrayList<node>();
		ArrayList<node> innerList2 = new ArrayList<node>();
		ArrayList<node> innerList3 = new ArrayList<node>();
		ArrayList<node> innerList4 = new ArrayList<node>();
		for (int y = 0; y < this.length; ++y) 
		{
			innerList1.add(new node(false, new HashSet<String>(), 1));			
			innerList2.add(new node(false, new HashSet<String>(), 1));
			innerList3.add(new node(false, new HashSet<String>(), 1));
			innerList4.add(new node(false, new HashSet<String>(), 1));
		}
		this.matrix.add(innerList1);
		this.matrix.add(innerList2);
		this.matrix.add(innerList3);
		this.matrix.add(innerList4);
	}
	
	public Action getAction(boolean stench, boolean breeze, boolean glitter, boolean bump, boolean scream)
	{
		if (bump) {this.coordinateAfterBump();}
		if (this.outOfBound(this.current)) 
		{
			this.resizeMatrix();
		}
		if (this.score >= 100)
		{
			this.goBack = true;
		}
		if (this.allHaveVisited())
		{
			this.goBack = true;
		}
		if (this.goBack)
		{
			return this.headingHome();
		}
		else
		{
			if (this.stepedBack)
			{				Direction toTurn = decideDir(this.anotherCell);
				if (this.currentDir == toTurn) 
				{
					this.prev_moves.push(Action.FORWARD);
					this.movingForward(this.currentDir);
					this.stepedBack = false;
					this.score += 1;
					return Action.FORWARD;
				}
				else
				{
					Action turnAction = decideTurnAction(toTurn);
					this.turnDirection(turnAction);
					this.score += 1;
					return turnAction;
				}
			}	
			else
			{
				if (scream)
				{
					this.afterShoot();
				}
				if (glitter) 
				{
					this.matrix.get(this.current.x).get(this.current.y).visited = true;
					this.goBack = true;
					this.inferMatrix();
					this.score += 1;
					return Action.GRAB;
				}
				if (this.shooting) {return this.dealWithShoot();}
				this.wumpusCell = this.wumpusCell();
				if (!this.outOfBound(this.wumpusCell))
				{
					if (this.current.x == this.wumpusCell.x || this.current.y == this.wumpusCell.y)
					{
						if (this.shootCount < 1)
						{
							this.shooting = true;
							this.score += 1;
							return this.dealWithShoot();
						}
					}
				}
				if (bump)
				{
					this.matrix.get(this.current.x).get(this.current.y).visited = true;
					this.inferMatrix();
					ArrayList<Pair> safeCells = this.getSafeCells(this.current.getKey(), this.current.getValue());
					if (!safeCells.isEmpty())
					{
						int x = safeCells.get(0).x;
						int y = safeCells.get(0).y;
						if (this.unvisitedSafeCell(safeCells)) 
						{
							for (int i = 0; i < safeCells.size(); ++i)
							{
								int w = safeCells.get(i).x;
								int l = safeCells.get(i).y;
								if (this.matrix.get(w).get(l).visited == false)
								{
									x = safeCells.get(i).x;
									y = safeCells.get(i).y;
								}
							}
						}
						Direction toTurn = this.decideDir(new Pair(x, y));
						Action turnAction = decideTurnAction(toTurn);
						this.prev_moves.push(turnAction);
						this.turnDirection(turnAction);
						this.score += 1;
						return turnAction;
					}
					else
					{
						ArrayList<Action> okTurnAction = this.getOkTurnAction();
						if (okTurnAction.size() == 1) 
						{
							this.prev_moves.push(okTurnAction.get(0));
							this.turnDirection(okTurnAction.get(0));
							this.score += 1;
							return okTurnAction.get(0);
						}
						else
						{
							if (this.checkLeftVisited()) 
							{
								this.prev_moves.push(Action.TURN_RIGHT);
								this.turnDirection(Action.TURN_RIGHT);
								this.score += 1;
								return Action.TURN_RIGHT;		
							}
							this.score += 1;
							this.turnDirection(Action.TURN_LEFT);
							return Action.TURN_LEFT;
						}
					}
				}
				if (stench || breeze)
				{
					if (this.current.x == 0 && this.current.y == 0)
					{
						ArrayList<String> newStatus = new ArrayList();
						if (stench && this.wumpusDead == false) {newStatus.add("stench");newStatus.add("safe");}
						if(breeze) {newStatus.add("breeze");newStatus.add("safe");}
						this.markNode(this.current.getKey(), this.current.getValue(), newStatus, 1);
						this.matrix.get(this.current.x).get(this.current.y).visited = true;
						if (stench) 
						{
							if (this.shootCount == 0)
							{
								this.score += 10;
								if (this.shootCount < 1)
								{
									this.shootCount += 1;
									return Action.SHOOT;
								}
							}
							if (!scream) {
								if (!breeze)
								{
									this.prev_moves.push(Action.FORWARD); 
									this.movingForward(this.currentDir); 
									this.score += 1;
									return Action.FORWARD;
								}
								else{this.score += 1;return Action.CLIMB;}
							}
							else {this.WkilledAtBeginning = true;}
						}
						if (breeze && this.WkilledAtBeginning == false) {this.score += 1;return Action.CLIMB;}
						else {
							this.prev_moves.push(Action.FORWARD); 
							this.movingForward(this.currentDir); 
							this.score += 1;
							return Action.FORWARD;
						}
					}
					ArrayList<String> newStatus = new ArrayList();
					if (stench && this.wumpusDead == false) {newStatus.add("stench");newStatus.add("safe");}
					if(breeze) {newStatus.add("breeze");newStatus.add("safe");}
					this.markNode(this.current.getKey(), this.current.getValue(), newStatus, 1);
					if (this.nextToStarting() && this.matrix.get(this.current.x).get(this.current.y).visited == false)
					{
						if (breeze) {this.beginBreezeCount += 1;}
						if (stench) {this.beginStenchCount += 1;}
					}
					this.matrix.get(this.current.x).get(this.current.y).visited = true;
					this.inferMatrix();
					if (this.beginBreezeCount == 2)
					{
						this.goBack = true;
						return this.headingHome();
					}
					if (this.beginStenchCount == 2)
					{
						ArrayList<String> Status = new ArrayList();
						Status.add("wumpus");
						this.markNode(1, 1, Status, 1000);
						this.inferMatrix();
						this.wumpusCell = new Pair(1, 1);
						Direction toTurn = this.decideDir(this.wumpusCell);
						if (this.currentDir == toTurn) {this.score += 1;if (this.shootCount < 1) {this.shootCount+=1; return Action.SHOOT;}}
						else 
						{
							Action nextTurn = this.decideTurnAction(toTurn); 
							this.prev_moves.push(nextTurn);
							this.turnDirection(nextTurn);
							this.score += 1;
							return nextTurn;
						}
					}
					if (this.shooting) {return this.dealWithShoot();}
					this.wumpusCell = this.wumpusCell();
					if (!this.outOfBound(this.wumpusCell))
					{
						if (this.current.x == this.wumpusCell.x || this.current.y == this.wumpusCell.y)
						{
							if (this.shootCount <1)
							{
								this.shooting = true;
								this.score += 1;
								return this.dealWithShoot();
							}
						}
					}
					ArrayList<Pair> safeCells = this.getSafeCells(this.current.getKey(), this.current.getValue());
					if (safeCells.isEmpty())
					{
						if (this.checkAllOtherVisited())
						{
							Pair forwardCell = this.forwardCell();
							if (!this.outOfBound(forwardCell))
							{
								if (this.noDanger(forwardCell.x, forwardCell.y))
								{
									this.prev_moves.push(Action.FORWARD);
									this.movingForward(this.currentDir);
									this.score += 1;
									return Action.FORWARD;
								}
								else
								{
									Pair subSafeCell = this.substituteSafeCell(this.current.x, this.current.y);
									if (subSafeCell == null) {this.goBack = true; return this.headingHome();}
									else
									{
										Direction toTurn = this.decideDir(subSafeCell);
										if (this.currentDir == toTurn) {this.prev_moves.push(Action.FORWARD);this.movingForward(currentDir); return Action.FORWARD;}
										else 
										{
											Action turnAction = decideTurnAction(toTurn);
											this.prev_moves.push(turnAction);
											this.turnDirection(turnAction);
											this.score += 1;
											return turnAction;
										}
									}
								}
							}
							else 
							{ 
								this.goBack = true;
								return this.headingHome();							}
						}
						else
						{
							this.anotherCell = this.getAnotherCell();
							if (this.anotherCell != null)
							{
								if (this.nextToCurrent(this.anotherCell))
								{
									return this.aimAtCell(this.anotherCell);
								}
								return this.stepBack();
							}
							else
							{
								this.goBack = true;
								return this.headingHome(); 
							}
						}
					}
					else
					{
						Direction toTurn = this.decideDir(safeCells.get(0));
						if (this.currentDir == toTurn)
						{
							this.movingForward(this.currentDir);
							this.score += 1;
							return Action.FORWARD;
						}
						else
						{
							Action turnAction = decideTurnAction(toTurn);
							this.prev_moves.push(turnAction);
							this.turnDirection(turnAction);
							this.score += 1;
							return turnAction;
						}
					}	
				}
				ArrayList<String> newStatus = new ArrayList<String>();
				newStatus.add("safe");
				this.markNode(this.current.getKey(), this.current.getValue(), newStatus, 1);
				this.matrix.get(this.current.x).get(this.current.y).visited = true;
				this.movingForward(this.currentDir);
				this.inferMatrix();
				this.score += 1;
				return Action.FORWARD;				
			}
		}
    }

    public Action headingHome()
    {
    	if (this.current.x == 0 && this.current.y == 0)
    	{
    		return Action.CLIMB;
    	}
    	if (this.current.equal(new Pair(1,0)) || this.current.equal(new Pair(0,1)))
		{
			return this.nextToClimb();
		}
		if (this.diagonalToClimb() && this.ableShortPath())
		{
			return this.diagonalAction();
		}
    	if (this.sameXwalkable() && this.initYwalkable())
    	{
    		Stack<Pair> sameXpath = this.sameXbackPath();
    		return this.headingSameX(sameXpath);
    	}
    	if (this.sameYwalkable() && this.initXwalkable())
    	{
    		Stack<Pair> sameYpath = this.sameYbackPath();
    		return this.headingSameY(sameYpath);
    	}
    	
    		if (this.current.equal(new Pair(1,0)) || this.current.equal(new Pair(0,1)))
    		{
    			return this.nextToClimb();
    		}
    		if (this.diagonalToClimb() && this.ableShortPath())
    		{
    			return this.diagonalAction();
    		}
    		Pair lastCell = this.visitedCell.peek();
    		Direction goalDir = this.decideDir(lastCell);
    		if (this.currentDir == goalDir)
    		{
    			this.visitedCell.pop();
    			this.headingBack(this.currentDir);
    			return Action.FORWARD;
    		}
    		else
    		{
    			Action nextTurn = this.decideTurnAction(goalDir);
    			this.turnDirection(nextTurn);
    			return nextTurn;
    		}
    }
    
    public Action headingSameX(Stack<Pair> sameXpath)
    {
    	if (this.current.equal(new Pair(1,0)) || this.current.equal(new Pair(0,1)))
		{
			return this.nextToClimb();
		}
		if (this.diagonalToClimb() && this.ableShortPath())
		{
			return this.diagonalAction();
		}
		Pair lastCell = sameXpath.peek();
		Direction goalDir = this.decideDir(lastCell);
		if (this.currentDir == goalDir)
		{
			sameXpath.pop();
			this.headingBack(this.currentDir);
			return Action.FORWARD;
		}
		else
		{
			Action nextTurn = this.decideTurnAction(goalDir);
			this.turnDirection(nextTurn);
			return nextTurn;
		}
    }
    
    public Action headingSameY(Stack<Pair> sameYpath)
    {
    	if (this.current.equal(new Pair(1,0)) || this.current.equal(new Pair(0,1)))
		{
			return this.nextToClimb();
		}
		if (this.diagonalToClimb() && this.ableShortPath())
		{
			return this.diagonalAction();
		}
		Pair lastCell = sameYpath.peek();
		Direction goalDir = this.decideDir(lastCell);
		if (this.currentDir == goalDir)
		{
			sameYpath.pop();
			this.headingBack(this.currentDir);
			//System.out.println("**********headingHome ends**********");
			return Action.FORWARD;
		}
		else
		{
			Action nextTurn = this.decideTurnAction(goalDir);
			this.turnDirection(nextTurn);
			//System.out.println("**********headingHome ends**********");
			return nextTurn;
		}
    }
    
    public boolean sameYwalkable()
    {
    	int y = this.current.y;
    	int x = this.current.x;
    	for (int i = 0; i <= y; ++i)
    	{
    		if (!this.visitedCell.contains(new Pair(x, i))) {return false;}
    	}
    	return true;
    }
    
    public boolean sameXwalkable()
    {
    	int y = this.current.y;
    	int x = this.current.x;
    	for (int i = 0; i <= x; ++i)
    	{
    		if (!this.visitedCell.contains(new Pair(i, y))) {return false;}
    	}
    	return true;
    }
    
    public boolean initXwalkable()
    {
    	int y = this.current.y;
    	int x = this.current.x;
    	for (int i = 0; i <= y; ++i)
    	{
    		if (!this.visitedCell.contains(new Pair(0, y))) {return false;}
    	}
    	return true;
    }
    
    public boolean initYwalkable()
    {
    	int y = this.current.y;
    	int x = this.current.x;
    	for (int i = 0; i <= x; ++i)
    	{
    		if (!this.visitedCell.contains(new Pair(i, 0))) {return false;}
    	}
    	return true;
    }
    
    public Stack<Pair> sameXbackPath()
    {
    	Stack<Pair> sameX = new Stack<Pair>();
    	int y = this.current.y;
    	int x = this.current.x;
    	if (y>0)
    	{
    		for (int w = 0; w <= x; ++w)
    		{
    			sameX.push(new Pair(w, 0));
    		}
    		for (int l = 0; l < y; ++l)
    		{
    			sameX.push(new Pair(x, l));
    		}
    	}
    	else
    	{
    		for (int w = 0; w < x; ++w)
    		{
    			sameX.push(new Pair(w, 0));
    		}
    		for (int l = 0; l < y; ++l)
    		{
    			sameX.push(new Pair(x, l));
    		}
    	}
    	return sameX;
    }
    
    public Stack<Pair> sameYbackPath()
    {
    	Stack<Pair> sameY = new Stack<Pair>();
    	int y = this.current.y;
    	int x = this.current.x;
    	if (x>0)
    	{
    		for (int w = 0; w < x; ++w)
    		{
    			sameY.push(new Pair(w, y));
    		}
    		for (int l = 0; l <= y; ++l)
    		{
    			sameY.push(new Pair(0, l));
    		}
    	}
    	else
    	{
    		for (int w = 0; w < x; ++w)
    		{
    			sameY.push(new Pair(w, y));
    		}
    		for (int l = 0; l < y; ++l)
    		{
    			sameY.push(new Pair(0, l));
    		}
    	}
    	return sameY;
    }
    
    public void forceCurrent()
    {
    	int x = this.current.x;
    	int y = this.current.y;
    	if (x < 0) {++this.current.x;}
    	if (y < 0) {++this.current.y;}
    	if (x >=this.width) {--this.current.x;}
    	if (y >=this.length) {--this.current.y;}
    }
    
    public boolean nextToCurrent(Pair anotherCell)
    {
    	int x = anotherCell.x;
    	int y = anotherCell.y;
    	ArrayList<Integer> possibleX = new ArrayList();
    	possibleX.add(this.current.x+1);
    	possibleX.add(this.current.x-1);
    	ArrayList<Integer> possibleY = new ArrayList();
    	possibleY.add(this.current.y+1);
    	possibleY.add(this.current.y-1);
    	if (possibleX.contains(x)) {return y == this.current.y;}
    	if (possibleY.contains(y)) {return x == this.current.x;}
    	return false;
    	//return possibleX.contains(x) && possibleY.contains(y);
    }
    
    public Action diagonalAction()
    {
    	Pair lastCell = this.shortPathCell();
    	Direction goalDir = this.decideDir(lastCell);
    	if (this.currentDir == goalDir)
    	{
    		this.headingBack(this.currentDir);
    		return Action.FORWARD;
    	}
    	else
    	{
    		Action nextTurn = this.decideTurnAction(goalDir);
    		this.prev_moves.push(nextTurn);
    		this.turnDirection(nextTurn);
    		return nextTurn;
    	}
    }
    
    public boolean diagonalToClimb()
    {
    	return this.current.x == 1 && this.current.y == 1;
    }
    
    public boolean ableShortPath()
    {
    	return this.matrix.get(0).get(1).visited == true || this.matrix.get(1).get(0).visited == true;
    }
    
    public Pair shortPathCell()
    {
    	if (this.matrix.get(0).get(1).visited == true) {return new Pair(0, 1);}
    	return new Pair(1, 0);
    }
    
    public Pair getRightCell()
    {
    	int x = this.current.x;
    	int y = this.current.y;
    	
    	if (this.currentDir == Direction.DOWN) {return new Pair(x-1, y);}
    	else if (this.currentDir == Direction.LEFT) {return new Pair(x, y+1); }
    	else if (this.currentDir == Direction.RIGHT) {return new Pair(x, y-1); }
    	return new Pair(x+1,y);
    }
    
    public boolean rightNearExit()
    {
    	Pair rightCell = this.getRightCell();
    	int rx = rightCell.x;
    	int ry = rightCell.y;
    	
    	int x = this.current.x;
    	int y = this.current.y;
    	
    	if (rx < x || ry < y) {return true;}
   		return false;
    }
    
    public boolean rightVisited()
    {
    	Pair rightCell = this.getRightCell();
    	int rx = rightCell.x;
    	int ry = rightCell.y;
    	if (this.matrix.get(rx).get(ry).visited == true) {return true;}
    	return false;
    }
    
    public boolean unvisitedSafeCell(ArrayList<Pair> safeCells)
    {
    	for (int i = 0; i < safeCells.size(); ++i)
    	{
    		int x = safeCells.get(i).x;
    		int y = safeCells.get(i).y;
    		if (this.matrix.get(x).get(y).visited == false)
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    public void afterShoot()
    {
    	for (int x = 0; x < this.width; ++x)
    	{
    		for (int y = 0; y < this.length; ++y)
    		{
    			if (this.hasSuchStatus(new Pair(x,y), "wumpus"))
       			{
       				this.matrix.get(x).get(y).status.remove("wumpus");
       			}
       			if (this.hasSuchStatus(new Pair(x,y), "stench"))
       			{
       				this.matrix.get(x).get(y).status.remove("stench");
       			}
    		}
    	}
    	this.wumpusDead = true;
    	this.beginStenchCount = 0;
    }
    
    public boolean allPossibleVisited()
    {
    	for (int x = 0; x < this.width; ++x)
    	{
    		for (int y = 0; y < this.length; ++y)
    		{
    			if (!this.matrix.get(x).get(y).status.contains("pit"))
    			{
    				if (this.matrix.get(x).get(y).visited == false)
    				{
    					return false;
    				}
    				//return false;
    			}
    		}
    	}
    	return true;
    }
    
    public Pair forwardCell()
    {
    	int x = this.current.x;
    	int y = this.current.y;
    	if (this.currentDir == Direction.DOWN) {return new Pair(x, y-1);}
    	else if(this.currentDir == Direction.UP) {return new Pair(x, y+1);}
    	else if(this.currentDir == Direction.LEFT) {return new Pair(x-1, y);}
    	return new Pair(x+1, y);
    }
    public boolean allHaveVisited()
    {
    	for (int x = 0; x < this.width; ++x)
    	{
    		for (int y = 0; y < this.length; ++y)
    		{
    			if (this.matrix.get(x).get(y).visited == false && this.noDanger(x, y))
    			{
    				return false;
    			}
    		}
    	}
    	return true;
    }

    public boolean nextToStarting()
    {
    	return (this.current.x == 0 && this.current.y == 1) || (this.current.x == 1 && this.current.y == 0);
    }

    public boolean anotherAlsoDanger(boolean stench, boolean breeze)
    {
    	int x = this.current.x;
    	int y = this.current.y;
   		if (x == 1 && y == 0)
   		{
   			if (stench) {/*System.out.println("another's status: "+this.matrix.get(0).get(1).status);System.out.println("^^^^^^^^^^anotherAlsoDanger ends^^^^^^^^^^");*/return this.matrix.get(0).get(1).status.contains("stench");}
   			if (breeze) {/*System.out.println("another's status: "+this.matrix.get(0).get(1).status);System.out.println("^^^^^^^^^^anotherAlsoDanger ends^^^^^^^^^^");*/return this.matrix.get(0).get(1).status.contains("breeze");}
   		}
   		else if (x == 0 && y == 1)
   		{
		   	if (stench) {/*System.out.println("another's status: "+this.matrix.get(1).get(0).status);System.out.println("^^^^^^^^^^anotherAlsoDanger ends^^^^^^^^^^");*/return this.matrix.get(1).get(0).status.contains("stench");}
   			if (breeze) {/*System.out.println("another's status: "+this.matrix.get(1).get(0).status);System.out.println("^^^^^^^^^^anotherAlsoDanger ends^^^^^^^^^^");*/return this.matrix.get(1).get(0).status.contains("breeze");}	
   		}
   		//System.out.println("^^^^^^^^^^anotherAlsoDanger ends^^^^^^^^^^");
   		return false;
    }

    public Action dealWithShoot()
	{
    	//System.out.println("beginning at dealWithShoot, shooting now is: "+this.shooting);
    	//System.out.println("inside dealWithShoot: very beginning: wumpusCell: "+this.wumpusCell.x+" "+this.wumpusCell.y);
		Direction toTurn = null;
		if (this.current.x == this.wumpusCell.x)
		{
			if (this.current.y < this.wumpusCell.y) {toTurn = Direction.UP;}
			else {toTurn = Direction.DOWN;}
		}
		if (this.current.y == this.wumpusCell.y)
		{
			if (this.current.x < this.wumpusCell.x) {toTurn = Direction.RIGHT;}
			else {toTurn = Direction.LEFT;}
		}
		//System.out.println("wumpusCell direction: "+toTurn);
		//System.out.println("current Dir: "+this.currentDir);
		if (this.currentDir == toTurn)
		{
			//this.shooting = false;
			//System.out.println("ready to shoot, shooting now is: "+this.shooting);
			//System.out.println("shootCount now: "+this.shootCount+"... AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA!!!!!");
			this.shooting = false;
			//System.out.println("right before shoot, shooting now is: "+this.shooting);
			this.wumpusCell = new Pair(this.width, this.length);
			this.score += 1;
			this.shootCount += 1;
			return Action.SHOOT;
			//return this.shoot();
		}
		else
		{
			Action turnAction = this.decideTurnAction(toTurn);
			this.prev_moves.push(turnAction);
			this.turnDirection(turnAction);
			this.score += 1;
			return turnAction;
		}
	}

    /*public Action shoot()
    {
    	for (int x = 0; x < this.width; ++x)
    	{
    		for (int y = 0; y < this.length; ++y)
    		{
    			if (this.hasSuchStatus(new Pair(x,y), "wumpus"))
    			{
    				this.matrix.get(x).get(y).status.remove("wumpus");
    			}
    			if (this.hasSuchStatus(new Pair(x,y), "stench"))
    			{
    				this.matrix.get(x).get(y).status.remove("stench");
    			}
    		}
    	}
    	return Action.SHOOT;
    }*/

    public Action nextToClimb()
    {
    	//System.out.println("^^^^^^^^^^inside nextToClimb^^^^^^^^^^");
    	//System.out.println("this.current: "+this.current.x+" "+this.current.y);
    	Direction finalDir = this.decideDir(new Pair(0, 0));
    	if (this.currentDir == finalDir)
    	{
    		this.headingBack(this.currentDir);
    		//System.out.println("^^^^^^^^^^nextToClimb ends^^^^^^^^^^");
    		return Action.FORWARD;
    	}
    	else
    	{
    		Action turnAction = decideTurnAction(finalDir);
    		this.turnDirection(turnAction);
    		//System.out.println("^^^^^^^^^^nextToClimb ends^^^^^^^^^^");
    		return turnAction;
    	}
    }

    public Action anotherBranch()
    {
    	if (this.currentDir == Direction.UP)
    	{
    		this.movingForward(this.currentDir);
    		this.score += 1;
    		return Action.FORWARD;
    	}
    	else
    	{
    		this.turnDirection(Action.TURN_LEFT);
    		this.score += 1;
    		return Action.TURN_LEFT;
    	}
    }

    public void turnDirection(Action action)
    {
    	if (action == Action.TURN_RIGHT) {
			if (this.currentDir == Direction.RIGHT) { this.currentDir = Direction.DOWN; }
			else if (this.currentDir == Direction.DOWN) { this.currentDir = Direction.LEFT; }
			else if (this.currentDir == Direction.LEFT) { this.currentDir = Direction.UP; }
			else if (this.currentDir == Direction.UP) {this.currentDir = Direction.RIGHT; }
		}
		else if (action == Action.TURN_LEFT) {
			if (this.currentDir == Direction.RIGHT) { this.currentDir = Direction.UP; }
			else if (this.currentDir == Direction.UP) { this.currentDir = Direction.LEFT; }
			else if (this.currentDir == Direction.LEFT) { this.currentDir = Direction.DOWN; }
			else if (this.currentDir == Direction.DOWN) {this.currentDir = Direction.RIGHT; }
		}
    }

    public boolean lastEqualCurrent(Pair cpy_current)
    {
    	if (this.visitedCell.isEmpty())
    	{
    		return false;
    	}
    	if (this.visitedCell.peek().x == cpy_current.x && this.visitedCell.peek().y == cpy_current.y)
    	{
    		return true;
    	}
    	return false;
    }

    public void movingForward (Direction currentDir) 
    {
    	Pair cpy_current = new Pair(this.current.x, this.current.y);
    	if (!this.lastEqualCurrent(cpy_current))
    	{
			this.visitedCell.push(cpy_current);
		}
		this.prev_cell = this.current.clone();
        if (this.currentDir == Direction.RIGHT) { this.current.x += 1; }
        else if (this.currentDir == Direction.LEFT) { this.current.x -= 1; }
        else if (this.currentDir == Direction.UP) { this.current.y += 1; }
        else if (this.currentDir == Direction.DOWN) {this.current.y -= 1; }
	}

	public void headingBack(Direction currentDir)
	{
		if (this.currentDir == Direction.RIGHT) { this.current.x += 1; }
        else if (this.currentDir == Direction.LEFT) { this.current.x -= 1; }
        else if (this.currentDir == Direction.UP) { this.current.y += 1; }
        else if (this.currentDir == Direction.DOWN) {this.current.y -= 1; }	
	}

	public void resizeMatrix()
	{
		int x = this.current.getKey();
		int y = this.current.getValue();
		int Otoadd = x+1-this.width;
		if (Otoadd > 0)
		{
			for (int w = 0; w < Otoadd; ++w)
			{
				ArrayList<node> innerList = new ArrayList<node>();
				for (int l = 0; l < this.length; ++l) 
				{
					innerList.add(new node(false, new HashSet<String>(), 1));
				}
				this.matrix.add(innerList);
			}
			this.width += Otoadd;
		}
		
		int Htoadd = y+1-this.length;
		if (Htoadd > 0) 
		{
			for (int i = 0; i < this.width; ++i)
			{
				for (int l = 0; l < Htoadd; ++l)
				{
					this.matrix.get(i).add(new node(false, new HashSet<String>(), 1));
				}
			}
			this.length += Htoadd;
		}
		
		//for (int w = 0; w < x+1-this.width;)
		/*if (x >= this.width) 
		{
			for (int w = 0; w < x+1-this.width; ++x)
			{
				ArrayList<node> innerList = new ArrayList<node>();
				for (int l = 0; l < this.length; ++l) 
				{
					innerList.add(new node(false, new HashSet<String>(), 1));
				}
				this.matrix.add(innerList);
			}
			this.width = x+1;
		}
		if (y >= length)
		{
			for (int i = 0; i < this.width; ++i)
			{
				for (int l = 0; l < y+1-this.length; ++l)
				{
					this.matrix.get(i).add(new node(false, new HashSet<String>(), 1));
				}
			}
			this.length = y+1;
		}*/
	}

	public void markNode(int x, int y, ArrayList<String> newStatus, int cost)
	{
		//System.out.println("**********inside markNode**********");
		//System.out.println(newStatus);
		//System.out.println("to mark: "+x+" "+y);
		for (int i = 0; i < newStatus.size(); ++i) 
		{
			//System.out.println("here works... before adding "+i);
			//System.out.println("status before adding..."+this.matrix.get(x).get(y).status);
			this.matrix.get(x).get(y).status.add(newStatus.get(i));
			//System.out.println("here works... after adding "+i);
		}
		this.matrix.get(x).get(y).cost = cost;	
		//System.out.println("**********markNode ends**********");
	}

	public void ifWumpusFound()
	{
		for (int x = 0; x < this.width; ++x)
		{
			for (int y = 0; y < this.length; ++y)
			{
				if (this.hasSuchStatus(new Pair(x,y), "wumpus"))
				{
					this.wumpusFound = true;
				}
			}
		}
	}
	
	public void inferMatrix()
	{
		for (int x = 0; x < this.width; ++x)
		{
			for (int y = 0; y < this.length; ++y)
			{
				/*if (this.matrix.get(x).get(y).visited == true )
				{
					if (this.hasSuchStatus(new Pair(x, y), "pit"))
					{
						System.out.println("captyred a overinferred pit!!!!!!!!!!!!!!!!!");
						this.matrix.get(x).get(y).status.remove("pit");
					}
				}*/
				ArrayList<Pair> around = this.getAround(x, y);
				if (this.noSuchStatus(x,y,"breeze") && this.noSuchStatus(x,y,"stench") && this.matrix.get(x).get(y).visited == true)
				{
					for (int i = 0; i < around.size(); ++i)
					{
						int w = around.get(i).getKey();
						int l = around.get(i).getValue();
						ArrayList newStatus = new ArrayList();
						newStatus.add("safe");
						this.markNode(w, l, newStatus, 1);
					}
				}
				if (this.allhasBreeze(around) && !this.isSafe(x,y)) {ArrayList newStatus = new ArrayList();newStatus.add("pit"); this.markNode(x,y,newStatus,1000);}
				if (this.allhasStench(around) && !this.isSafe(x,y)) {ArrayList newStatus = new ArrayList();newStatus.add("wumpus");this.markNode(x,y,newStatus,1000);}
				if (this.stenchBreezeSafe(around)) {ArrayList newStatus = new ArrayList();newStatus.add("safe");this.markNode(x,y,newStatus,1);}
				if (this.hasBreeze(new Pair(x,y)))
				{
					if (this.mostAreSafe(x, y, around))
					{
						Pair dangerCell = this.inferredDangerCell(around);
						if (!this.outOfBound(dangerCell))
						{
							ArrayList newStatus = new ArrayList();
							newStatus.add("pit"); 
							this.markNode(dangerCell.x,dangerCell.y,newStatus,1000);
						}
					}
				}
				if (this.hasStench(new Pair(x,y)))
				{
					if (this.mostAreSafe(x, y, around))
					{
						//System.out.println("ok now most are safe...");
						Pair dangerCell = this.inferredDangerCell(around);
						if (!this.outOfBound(dangerCell))
						{
							ArrayList newStatus = new ArrayList();
							newStatus.add("wumpus"); 
							this.markNode(dangerCell.x,dangerCell.y,newStatus,1000);
						}
					}
				}
				if (!this.wumpusFound)
				{
					String edge = this.EdgeThatStench(x, y);
					if (edge != "None")
					{
						Pair inferredWumpus = this.inferredWumpusPosition(x, y, edge);
						if (this.isSafe(inferredWumpus.x, inferredWumpus.y))
						{
							ArrayList newStatus = new ArrayList();newStatus.add("wumpus");this.markNode(x,y,newStatus,1000);
						}
						if (this.isSafe(x, y))
						{
							ArrayList newStatus = new ArrayList();newStatus.add("wumpus");this.markNode(inferredWumpus.x,inferredWumpus.y,newStatus,1000);
						}
						/*if (this.checkCellOverEdge(x,y,edge,"wumpus"))
						{
							ArrayList newStatus = new ArrayList();newStatus.add("safe");this.markNode(x,y,newStatus,1);
						}
						else
						{
							ArrayList newStatus = new ArrayList();newStatus.add("wumpus");this.markNode(x,y,newStatus,1000);
						}*/
					}
				}
				//System.out.println("cell 2, 3: "+this.matrix.get(2).get(3).status);
				//System.out.println(this.matrix.get(x).get(y).visited);
				if (this.hasSuchStatus(new Pair(x, y), "pit") && this.visitedCell.contains(new Pair(x, y)))
				{
					this.matrix.get(x).get(y).status.remove("pit");
					ArrayList newStatus = new ArrayList();
					newStatus.add("safe");
					this.markNode(x,y,newStatus,1);
				}
				if (this.allPit(around))
				{
					ArrayList newStatus = new ArrayList();newStatus.add("pit");this.markNode(x,y,newStatus,1000);
				}
				//if (this.matrix.get(x).get(y).visited && this.)
			}
		}
	}

	public boolean allPit(ArrayList<Pair> around)
	{
		for (int i = 0; i < around.size(); ++i)
		{
			//int x = around.get(i).x;
			//int y = around.get(i).y;
			if (!this.hasSuchStatus(around.get(i), "pit"))
			{return false;}
		}
		return true;
	}
	
	public Pair wumpusCell()
	{
		for (int x = 0; x < this.width; ++x)
		{
			for (int y = 0; y < this.length; ++y)
			{
				if (this.matrix.get(x).get(y).status.contains("wumpus"))
				{
					//System.out.println("wumpus cell ["+x+", "+y+"]");
					return new Pair(x, y);
				}
			}
		}
		return new Pair(this.width, this.length);
	}

	public void printMatrix()
	{
		for (int y = this.length-1; y >= 0; --y)
		{
			for (int x = 0; x < this.matrix.size(); ++x)
			{
				System.out.print(this.matrix.get(x).get(y).status+"		");
			}
			System.out.println();
		}	
	} 
	
	public boolean mostAreSafe(int x, int y, ArrayList<Pair> around)
	{
		//System.out.println("**********inside mostAreSafe**********");
		//System.out.println("cell to decide: ["+x+", "+y+"] ...");
		int safeCount = 0;
		for (int i = 0; i < around.size(); ++i)
		{
			int m = around.get(i).x;
			int n = around.get(i).y;
			//System.out.println("status of ["+m+", "+n+"]"+this.matrix.get(m).get(n).status);
			if (this.isSafe(m,n))
			{
				safeCount += 1;
			}
		}
		//System.out.print("safeCount: "+safeCount);
		if (safeCount == around.size()-1){/*System.out.println("**********mostAreSafe ends**********");*/return true;}
		//System.out.println("**********mostAreSafe ends**********");
		return false;
	}

	public Pair inferredDangerCell(ArrayList<Pair> around)
	{
		for (int i = 0; i < around.size(); ++i)
		{
			if (!this.isSafe(around.get(i).x, around.get(i).y))
			{
				return new Pair(around.get(i).x, around.get(i).y);
			}
		}
		return new Pair(this.width, this.length);
	}

	public boolean outOfBound(Pair pair)
	{
		if (pair.x >= this.width || pair.y >= this.length || pair.x < 0 || pair.y < 0)
		{
			return true;
		}
		return false;
	}

	/*public String EdgeThatBreeze(int x, int y)
	{
		Pair up = new Pair(x, y+1);
		Pair down = new Pair(x, y-1);
		Pair left = new Pair(x-1, y);
		Pair right = new Pair(x+1, y);
		if (y+1<this.length && x-1>0 && this.hasBreeze(up) && this.hasBreeze(left)) {return "UpLeft";}
		else if (y+1<this.length && x+1<this.width && this.hasBreeze(up) && this.hasBreeze(right)) {return "UpRight";}
		else if (y-1>0 && x-1>0 && this.hasBreeze(down) && this.hasBreeze(left)) {return "DownLeft";}
		else if (y-1>0 && x+1<this.width && this.hasBreeze(down) && this.hasBreeze(right)) {return "DownRight";}
		else {return "None";}
	}*/

	public String EdgeThatStench(int x, int y)
	{
		Pair up = new Pair(x, y+1);
		Pair down = new Pair(x, y-1);
		Pair left = new Pair(x-1, y);
		Pair right = new Pair(x+1, y);
		if (!this.outOfBound(up) && !this.outOfBound(left) && this.hasStench(up) && this.hasStench(left)) {return "UpLeft";}
		if (!this.outOfBound(up) && !this.outOfBound(right) && this.hasStench(up) && this.hasStench(right)) {return "UpRight";}
		if (!this.outOfBound(down) && !this.outOfBound(left) && this.hasStench(down) && this.hasStench(left)) {return "DownLeft";}
		if (!this.outOfBound(down) && !this.outOfBound(right) && this.hasStench(down) && this.hasStench(right)) {return "DownRight";}
		return "None";
	}

	public boolean checkCellOverEdge(int x, int y, String edge, String status)
	{
		Pair upRight = new Pair(x+1, y+1);
		Pair upLeft = new Pair(x-1, y+1);
		Pair downRight = new Pair(x+1, y-1);
		Pair downLeft = new Pair(x-1, y-1);
		if (edge == "UpLeft" && !this.outOfBound(upLeft)) {return this.hasSuchStatus(upLeft, status);}
		else if (edge == "UpRight" && !this.outOfBound(upRight)) {return this.hasSuchStatus(upRight, status);}
		else if (edge == "DownLeft" && !this.outOfBound(downLeft)) {return this.hasSuchStatus(downLeft, status);}
		else if (edge == "DownRight" && !this.outOfBound(downRight)) {return this.hasSuchStatus(downRight, status);}
		else {return false;}
	}

	public Pair inferredWumpusPosition(int x, int y, String edge)
	{
		Pair upRight = new Pair(x+1, y+1);
		Pair upLeft = new Pair(x-1, y+1);
		Pair downRight = new Pair(x+1, y-1);
		Pair downLeft = new Pair(x-1, y-1);
		if (edge == "UpLeft" && !this.outOfBound(upLeft)) {return upLeft;}
		else if (edge == "UpRight" && !this.outOfBound(upRight)) {return upRight;}
		else if (edge == "DownLeft" && !this.outOfBound(downLeft)) {return downLeft;}
		else if (edge == "DownRight" && !this.outOfBound(downRight)) {return downRight;}
		else {return null;}
	}
	
	public boolean hasSuchStatus(Pair cell, String status)
	{
		return this.matrix.get(cell.x).get(cell.y).status.contains(status);
	}

	public boolean noSuchStatus(int x, int y, String tocheck)
	{
		if (this.matrix.get(x).get(y).status.contains(tocheck)) {return false;}
		return true;
	}

	public boolean stenchBreezeSafe(ArrayList<Pair> around)
	{
		boolean hasOnlyStench = false;
		boolean hasOnlyBreeze = false;
		for (int n = 0; n < around.size(); ++n)
		{
			if (this.onlyStench(around.get(n))) {hasOnlyStench = true;}
			if (this.onlyBreeze(around.get(n))) {hasOnlyBreeze = true;}
		}
		if (hasOnlyBreeze && hasOnlyStench) {return true;}
		return false;
	}

	public boolean allhasBreeze(ArrayList<Pair> around)
	{
		for (int i = 0; i < around.size(); ++i)
		{
			if (this.hasBreeze(around.get(i)) == false)
			{
				return false;
			}
		}
		return true;
	}

	public boolean allhasStench(ArrayList<Pair> around)
	{
		for (int i = 0; i < around.size(); ++i)
		{
			if (this.hasStench(around.get(i)) == false)
			{
				return false;
			}
		}
		return true;
	}

	public ArrayList<Pair> getAround(int x, int y)
	{
		//System.out.println("*****inside getAround*****");
		//System.out.println("input x y: "+x+" "+y);
		ArrayList<Pair> around = new ArrayList<Pair>();
		Pair left = new Pair(x-1, y);
		Pair right = new Pair(x+1, y);
		Pair up = new Pair(x, y+1);
		Pair down = new Pair(x, y-1);
		if (x-1 >= 0) {/*System.out.println("left: "+left.x+" "+left.y);*/around.add(left);}
		if (x+1 < this.width) {/*System.out.println("right: "+right.x+" "+right.y);*/around.add(right);}
		if (y-1 >=0) {/*System.out.println("down: "+down.x+" "+down.y);*/around.add(down);}
		if (y+1 < this.length) {/*System.out.println("up"+up.x+" "+up.y);*/around.add(up);}
		/*around.add(left);
		around.add(right);
		around.add(up);
		around.add(down);
		if (x-1 < 0) {around.remove(left);}
		if (x+1 >= this.width) {around.remove(right);}	
		if (y-1 < 0) {around.remove(down);}
		if (y+1 >= this.length) {around.remove(up);}*/
		//System.out.println("*****getAround ends*****"+"\n");
		return around;		
	}

	public boolean hasBreeze(Pair pair)
	{
		int x = pair.getKey();
		int y = pair.getValue();
		//ArrayList<String> cpyStatus = this.matrix.get(x).get(y).status.clone();
		return this.matrix.get(x).get(y).status.contains("breeze");
	}

	public boolean hasStench(Pair pair)
	{
		int x = pair.getKey();
		int y = pair.getValue();
		//ArrayList<String> cpyStatus = this.matrix.get(x).get(y).status.clone();
		return this.matrix.get(x).get(y).status.contains("stench");
	}

	public boolean onlyBreeze(Pair pair)
	{
		int x = pair.getKey();
		int y = pair.getValue();
		//ArrayList<String> cpyStatus = this.matrix.get(x).get(y).status.clone();
		//return this.matrix.get(x).get(y).status.size() == 1 && this.matrix.get(x).get(y).status.contains("breeze");
		return this.matrix.get(x).get(y).status.contains("breeze") && !this.matrix.get(x).get(y).status.contains("stench");
	}

	public boolean onlyStench(Pair pair)
	{
		int x = pair.getKey();
		int y = pair.getValue();
		//ArrayList<String> cpyStatus = this.matrix.get(x).get(y).status.clone();
		return !this.matrix.get(x).get(y).status.contains("breeze") && this.matrix.get(x).get(y).status.contains("stench");
	}

	/*public List<String> checkNeighbor(List<Pair<Integer, Integer>> around)
	{

	}*/

	public Action stepBack()
	{
		if (this.headingHomeCount < 2)
		{
			++this.headingHomeCount;
			this.turnDirection(Action.TURN_LEFT);
			this.prev_moves.push(Action.TURN_LEFT);
			return Action.TURN_LEFT;
		}
		else
		{
			this.headingHomeCount = 0;
			this.movingForward(this.currentDir);
			this.stepedBack = true;
			this.prev_moves.push(Action.FORWARD);
			return Action.FORWARD;
		}
	}

	public boolean checkAllOtherVisited()
	{
		ArrayList<Pair> around = this.getAround(this.prev_cell.getKey(), this.prev_cell.getValue());
		for (int i = 0; i < around.size(); ++i)
		{
			int x = around.get(i).getKey();
			int y = around.get(i).getValue();
			if (this.matrix.get(x).get(y).visited == false) 
			{
				return false;
			}
		}
		return true;
	}

	public boolean noDanger(int x, int y)
	{
		return !this.matrix.get(x).get(y).status.contains("pit") && !this.matrix.get(x).get(y).status.contains("wumpus");
	}

	public Pair getAnotherCell()
	{
		ArrayList<Pair> around =  getAround(this.prev_cell.getKey(), this.prev_cell.getValue());
		for(int i = 0; i < around.size(); ++i)
		{
			int x = around.get(i).getKey();
			int y = around.get(i).getValue();
		}
		ArrayList<Pair> ok = new ArrayList();
		for (int i = 0; i < around.size(); ++i)
		{
			int x = around.get(i).getKey();
			int y = around.get(i).getValue();
			if (this.matrix.get(x).get(y).visited == false && this.noDanger(x, y)) 
			{
				ok.add(new Pair(x, y));
			}
		}
		if (ok.size()>0)
		{
			return ok.get(0);
		}
		else
		{
			return null;
		}
	}

	public Direction decideDir(Pair anotherCell)
	{
		int x = anotherCell.getKey();
		int y = anotherCell.getValue();
		int cx = this.current.getKey();
		int cy = this.current.getValue();
		/*if (x == this.current.getKey()-1 && y == this.current.getValue()) {return Direction.LEFT;}
		else if (x == this.current.getKey()+1 && y == this.current.getValue()) {return Direction.RIGHT;}
		else if (x == this.current.getKey() && y == this.current.getValue()+1) {return Direction.UP;}
		//else if (x == this.current.getKey()&& y == this.current.getValue()-1) {return Direction.DOWN;}
		else{return Direction.DOWN;}*/
		if (x == cx-1) { return Direction.LEFT;}
		else if (x == cx+1) { return Direction.RIGHT;}
		else if (y == cy+1) { return Direction.UP;}
		else {return Direction.DOWN;}
		//System.out.println("**********decideDir ends**********");
	}

	public Action decideTurnAction(Direction anotherCellDir)
	{
		if (this.currentDir == Direction.LEFT)
		{
			if (anotherCellDir == Direction.UP) {return Action.TURN_RIGHT;}			
			else if (anotherCellDir == Direction.DOWN) {return Action.TURN_LEFT;}
			else{return Action.TURN_RIGHT;}
		}
		else if (this.currentDir == Direction.RIGHT) 
		{
			if (anotherCellDir == Direction.UP) {return Action.TURN_LEFT;}
			else if (anotherCellDir == Direction.DOWN) {return Action.TURN_RIGHT;}
			else{return Action.TURN_RIGHT;}
		}
		else if (currentDir == Direction.UP) 
		{
			if (anotherCellDir == Direction.LEFT) {return Action.TURN_LEFT;}
			else if (anotherCellDir == Direction.RIGHT) {return Action.TURN_RIGHT;}
			else{return Action.TURN_RIGHT;}
		}
		else
		{
			if (anotherCellDir == Direction.RIGHT) {return Action.TURN_LEFT;}
			else if (anotherCellDir == Direction.LEFT) {return Action.TURN_RIGHT;}
			else{return Action.TURN_RIGHT;}
		}	
	}

	public ArrayList<Action> getOkTurnAction()
	{
		int x = this.current.getKey();
		int y = this.current.getValue();
		ArrayList<Action> okTurnAction = new ArrayList<Action>();
		okTurnAction.add(Action.TURN_LEFT);
		okTurnAction.add(Action.TURN_RIGHT);
		if (this.currentDir == Direction.RIGHT)
		{
			if (y+1 >= this.length) {okTurnAction.remove(Action.TURN_LEFT);}
			if (y-1 <0) {okTurnAction.remove(Action.TURN_RIGHT);}
		}
		if (this.currentDir == Direction.LEFT)
		{ 
			if (y+1 >= this.length) {okTurnAction.remove(Action.TURN_RIGHT);}
			if (y-1 < 0) {okTurnAction.remove(Action.TURN_LEFT);}
		}
		if (this.currentDir == Direction.UP)
		{
			if (x-1 < 0) {okTurnAction.remove(Action.TURN_LEFT);}
			if (x+1 >= this.width) {okTurnAction.remove(Action.TURN_RIGHT);}
		}
		if (this.currentDir == Direction.DOWN)
		{
			if (x-1 < 0) {okTurnAction.remove(Action.TURN_RIGHT);}
			if (x+1 >= this.width) {okTurnAction.remove(Action.TURN_LEFT);}
		}
		return okTurnAction;
	}

	public boolean checkLeftVisited()
	{
		if (this.currentDir == Direction.RIGHT)
		{
			int x = this.current.x;
			int y = this.current.y-1;
			if (this.matrix.get(x).get(y).visited == true) {return true;}
			else {return false;}
		}
		else if (this.currentDir == Direction.LEFT)
		{
			int x = this.current.x;
			int y = this.current.y+1;
			if (this.matrix.get(x).get(y).visited == true) {return true;}
			else {return false;}
		}
		else if (this.currentDir == Direction.UP)
		{
			int x = this.current.x-1;
			int y = this.current.y;
			if (this.matrix.get(x).get(y).visited == true) {return true;}
			else {return false;}
		}
		//if (this.currentDir == Direction.DOWN)
		else
		{
			int x = this.current.x+1;
			int y = this.current.y;
			if (this.matrix.get(x).get(y).visited == true) {return true;}
			else {return false;}
			//return false;
		}
	}

	public boolean isSafe(int x, int y)
	{
		return this.matrix.get(x).get(y).status.contains("safe");
	}

	public ArrayList<Pair> getSafeCells(int x, int y)
	{
		ArrayList<Pair> safeCells = new ArrayList<Pair>();
		ArrayList<Pair> around = this.getAround(x,y);
		for (int i = 0; i < around.size(); ++i)
		{
			int w = around.get(i).x;
			int l = around.get(i).y;
			//System.out.println(w+" "+l);
			//System.out.println(this.matrix.get(w).get(l).status);
			if (this.isSafe(w,l) && this.matrix.get(w).get(l).visited == false)
			{
				safeCells.add(around.get(i));
			}
			//if (this.isSafe(around.get(i).getKey(), around.get(i).getValue())) {safeCells.add(around.get(i));}
		}
		return safeCells;
	}
	
	public Pair getRandomSafeCell(int x, int y)
	{
		ArrayList<Pair> safeCells = new ArrayList<Pair>();
		ArrayList<Pair> around = this.getAround(x,y);
		for (int i = 0; i < around.size(); ++i)
		{
			int w = around.get(i).x;
			int l = around.get(i).y;
			//System.out.println(w+" "+l);
			//System.out.println(this.matrix.get(w).get(l).status);
			if (this.isSafe(w,l))
			{
				safeCells.add(around.get(i));
			}
			//if (this.isSafe(around.get(i).getKey(), around.get(i).getValue())) {safeCells.add(around.get(i));}
		}
		return safeCells.get(0);
	}

	public Pair substituteSafeCell(int x, int y)
	{
		ArrayList<Pair> around = this.getAround(x,y);
		for (int i = 0; i < around.size(); ++i)
		{
			int w = around.get(i).x;
			int l = around.get(i).y;
			if (/*this.matrix.get(w).get(l).visited == false && */w != this.prev_cell.x && l != this.prev_cell.y && this.noDanger(w, l))
			{
				return new Pair(w,l);
			}
			//if (this.isSafe(around.get(i).getKey(), around.get(i).getValue())) {safeCells.add(around.get(i));}
		}
		return null;
	}
	
	public Action aimAtCell(Pair anotherCell)
	{
		Direction toTurn = this.decideDir(anotherCell);
		if (this.currentDir == toTurn) { this.prev_moves.push(Action.FORWARD); this.movingForward(this.currentDir); this.score += 1;return Action.FORWARD;}
		else 
		{
			Action turnAction = this.decideTurnAction(toTurn);
			this.prev_moves.push(turnAction);
			this.turnDirection(turnAction);
			this.score += 1;
			return turnAction; 
		}
	}
	
	public void coordinateAfterBump()
	{
		if (this.currentDir == Direction.LEFT) {++this.current.x;}
		if (this.currentDir == Direction.RIGHT) {--this.current.x;}
		if (this.currentDir == Direction.UP) {--this.current.y;}
		if (this.currentDir == Direction.DOWN) {++this.current.y;}
	}

	public ArrayList<Pair> allSafe()
	{
		ArrayList<Pair> allSafe = new ArrayList<Pair>();
		for (int x = 0; x < this.width; ++x)
		{
			for (int y = 0; y < this.length; ++y)
			{
				if (this.matrix.get(x).get(y).status.contains("safe"))
				{
					allSafe.add(new Pair(x, y));
				}
			}
		}
		return allSafe;
	}

	/*
	public Stack<Pair> backPath()
	{
		ArrayList<Pair> allSafe = this.allSafe();
		Stack<Pair> backPath = new Stack<Pair>();
		Pair modelCurrent = new Pair(this.current.x, this.current.y);
		if ()
		for (int i = 0; i<allSafe.size(); ++i)
		{
			
		}
		
	}*/
	
	public class node {
		public boolean visited;
		public Set<String> status;
		public int cost;
		public node(boolean visited, Set<String> status, int cost)
		{
			this.visited = visited;
			this.status = status;
			this.cost = cost;
		}

		public node clone() 
		{
			return new node(this.visited, this.status, this.cost);
		}
	}
	
	public class Pair{
		int x;
		int y;

		public Pair(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		public int getKey()
		{
			return this.x;
		}

		public int getValue()
		{
			return this.y;
		}

		public Pair clone()
		{
			return new Pair(this.x, this.y);
		}

		public boolean equal(Pair goal)
		{
			return this.x == goal.x && this.y == goal.y;
		}
	}
	
	public class exception{}
}
