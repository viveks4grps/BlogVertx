package com.mysocial;


public class RouterVerticleInst1 extends BaseRouterVerticle {

	private static final String NODE_NAME = "Node1";
	private static final int NODE_PORT = 9090;
	
	public RouterVerticleInst1() {
		currentNodeId = NODE_NAME;
		currentNodePort = NODE_PORT;
	}
	/*public RouterVerticleInst1(String currentNodeId, int currentNodePort,
			boolean createDistMap) {
		super(createDistMap);
	}*/
	
	public static void main(String[] args) {
		createAndDeployVerticle(RouterVerticleInst1.class.getName(),true,NODE_NAME);
	}

}