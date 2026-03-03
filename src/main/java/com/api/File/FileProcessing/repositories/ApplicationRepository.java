package com.api.File.FileProcessing.repositories;

import com.api.File.FileProcessing.entities.ApplicationInfo;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ApplicationRepository {

    private final DataSource dataSource;

    public ApplicationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ApplicationInfo> findAll() {
        List<ApplicationInfo> apps = new ArrayList<>();
        String sql = "SELECT * FROM application_info.info";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                apps.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return apps;
    }

    public ApplicationInfo findById(long id) {
        String sql = "SELECT * FROM application_info.info WHERE id = ?";
        ApplicationInfo app = null;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    app = mapRow(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return app;
    }

    public void updateStatus(long id, String status) {
        String sql = "UPDATE application_info.info SET appStatus = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setLong(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ApplicationInfo mapRow(ResultSet rs) throws SQLException {
        ApplicationInfo app = new ApplicationInfo();
        app.setId(rs.getLong("id"));
        app.setAppName(rs.getString("appName"));
        app.setAppStatus(rs.getString("appStatus"));
        app.setJarPath(rs.getString("jarpath"));
        return app;
    }
}
