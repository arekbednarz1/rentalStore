package com.arekbednarz.utils;

import org.testcontainers.containers.Network;


public interface TestContainerNetworks {
	Network NETWORK = Network.newNetwork();
}
