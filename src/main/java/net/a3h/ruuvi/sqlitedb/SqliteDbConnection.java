package net.a3h.ruuvi.sqlitedb;

import fi.tkgwf.ruuvi.bean.EnhancedRuuviMeasurement;
import fi.tkgwf.ruuvi.db.DBConnection;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Objects;
import java.util.Scanner;

public class SqliteDbConnection implements DBConnection {
    private static final Logger LOG = Logger.getLogger(SqliteDbConnection.class);

    private static String createTables  = new Scanner(
        Objects.requireNonNull(SqliteDbConnection.class.getClassLoader().getResourceAsStream(
            "create-tables.sql")
        ), "UTF-8"
    ).useDelimiter("\\A").next();

    public SqliteDbConnection() {
        LOG.info(String.format("Using sqlite database at %s", SqliteDbConnectionSystemPropertyConfig.getDbPath()));
    }

    @Override
    public void save(EnhancedRuuviMeasurement measurement) {
        try {
            doSave(measurement);
        } catch (Throwable t) {
            LOG.error("Failed to save measurement", t);
        }
    }

    private void doSave(EnhancedRuuviMeasurement measurement) throws SQLException {
        Connection connection = DriverManager.getConnection(
            String.format("jdbc:sqlite:%s", SqliteDbConnectionSystemPropertyConfig.getDbPath()));

        Statement createStatement = connection.createStatement();
        createStatement.execute(createTables);
        if (createStatement.getUpdateCount() == 1) {
            LOG.info("Tables and indices created");
        }
        createStatement.close();

        PreparedStatement statement = prepareStatement(connection, measurement);
        boolean result = statement.execute();
        if (result) {
            LOG.debug(String.format("resultSet: %s", statement.getResultSet()));
        } else {
            if (statement.getUpdateCount() != 1) {
                String message = String.format("updateCount: %d", statement.getUpdateCount());
                throw new RuntimeException(
                    String.format("Failed to insert measurement %s, error: %s", measurement, message));
            }
        }
        statement.close();

        connection.close();
    }

    private static PreparedStatement prepareStatement(
        Connection connection,
        EnhancedRuuviMeasurement measurement
    ) throws SQLException {
        PreparedStatement result = connection.prepareStatement(
            "INSERT INTO measurement (" +
                "recorded_at, sensor, temperature, pressure, humidity," +
                "acceleration_x, acceleration_y, acceleration_z," +
                "battery_voltage, tx_power, movement_counter, measurement_sequence_number)" +
                "VALUES (?, ?, ?, ?, ?," +
                "?, ?, ?," +
                "?, ?, ?, ?)"
        );

        // LOG.debug(String.format("Got measurement: %s", measurement));

        result.setInt(1, Math.toIntExact(measurement.getTime() != null ?
            measurement.getTime() : System.currentTimeMillis() / 1000
        ));
        result.setString(2, measurement.getMac());
        result.setDouble(3, measurement.getTemperature());
        result.setDouble(4, measurement.getPressure());
        result.setDouble(5, measurement.getHumidity());

        result.setDouble(6, measurement.getAccelerationX());
        result.setDouble(7, measurement.getAccelerationY());
        result.setDouble(8, measurement.getAccelerationZ());

        result.setDouble(9, measurement.getBatteryVoltage());
        result.setDouble(10, measurement.getTxPower());
        result.setInt(11, measurement.getMovementCounter());
        result.setInt(12, measurement.getMeasurementSequenceNumber());

        result.setQueryTimeout(5);

        return result;
    }

    @Override
    public void close() {
    }
}
