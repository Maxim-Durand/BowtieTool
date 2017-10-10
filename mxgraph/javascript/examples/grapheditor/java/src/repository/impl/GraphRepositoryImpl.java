package repository.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.Graph;
import models.User;
import repository.IGraphRepository;
import repository.MySQLAccess;

public class GraphRepositoryImpl implements IGraphRepository {
	private MySQLAccess access;

	public GraphRepositoryImpl(MySQLAccess access) {
		this.access = access;
	}

	@Override
	public void insertGraph(User u, String graph_data) {
		String insert_graph = "INSERT INTO Graph(user_id, graph_data) VALUES (?, ?)";
		String insert_role = "INSERT INTO Role(id, user_id, role) VALUES (LAST_INSERT_ID(), ?, 0)";
		try {
			access.getConnection();
			access.manual_query(insert_graph, u.getId(), graph_data);
			access.manual_query(insert_role, u.getId());
		} catch (SQLException e) {
			System.err.println("SQL error " + e.getMessage());
		} finally {
			access.close();
		}
	}

	@Override
	public void updateGraph(Graph g) {
		String update_graph = "UPDATE Graph SET graph_data = ? WHERE id = ?";
		access.query(update_graph, g.getId());
	}

	@Override
	public List<Graph> getUserGraphs(User u) {
		List<Graph> graphs = new ArrayList<Graph>();
		String query = "SELECT Graph.graph_data, Graph.id"
				+ "       FROM Graph"
				+ "            LEFT JOIN Role"
				+ "            ON Graph.id = Role.id AND Graph.user_id = Role.user_id"
				+ "      WHERE Graph.user_id = ?";
		try {
			ResultSet rs = access.query(query, u.getId());
			while (rs.next()) {
				int id = rs.getInt("id");
				String graph_data = rs.getString("graph_data");
				graphs.add(new Graph(id, u, graph_data));
			}
		} catch (SQLException e ) {
			System.out.println("Quering form db failed");
		}
		return graphs;
	}

	@Override
	public Graph getUserGraph(User u, int id) {
		Graph graph = null;
		String query = "SELECT Graph.graph_data"
				+ "       FROM Graph"
				+ "            LEFT JOIN Role"
				+ "            ON Graph.id = Role.id AND Graph.user_id = Role.user_id"
				+ "      WHERE Graph.user_id = ?"
				+ "            AND Graph.id = ?";
		try {
			ResultSet rs = access.query(query, u.getId(), id);
			while (rs.next()) {
				String graph_data = rs.getString("graph_data");
				graph = new Graph(id, u, graph_data);
			}
		} catch (SQLException e) {
			System.out.println("SQL error: " + e.getMessage());
		}
		return graph;
	}

}
