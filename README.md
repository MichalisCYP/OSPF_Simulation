# OSPF Simulation

This project simulates the Open Shortest Path First (OSPF) routing protocol. It includes a router implementation that communicates with other routers, calculates shortest paths using Dijkstra's algorithm, and maintains a routing table.

To run, go to out/production/OSPF_Simulation and run: java RouterMain <IP> <Port> <Cost>. For instance, java Routermain localhost 5003 3.


## Features

- **Router Simulation**: Each router can connect to neighbors, exchange Link State Advertisements (LSAs), and calculate shortest paths.
- **Dijkstra's Algorithm**: Used to compute the shortest paths between routers.
- **Routing Table**: Maintains the best routes to all reachable destinations.
- **Interactive CLI**: Allows users to interact with the router via commands.
