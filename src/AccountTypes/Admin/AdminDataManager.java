package AccountTypes.Admin;

import Data.Customers.Camper;
import Data.Customers.Employee;
import Data.Customers.EmployeeType;
import Data.DataObjectBuilder;
import Data.DataViewer;
import Data.Item.Item;
import Data.Item.ItemType;
import Data.DataBaseManager;
import Interfaces.MultiReceive;
import Security.PassHash;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import org.controlsfx.control.table.TableFilter;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * @Author Aidan Stewart
 * @Year 2018
 * Copyright (c)
 * All rights reserved.
 */
public class AdminDataManager implements MultiReceive {

    private DataBaseManager databaseManager = new DataBaseManager();

    @Override
    public void retrieveDatabaseData(DataViewer dataViewer) throws SQLException {
        ResultSet resultSet = databaseManager.receiver(dataViewer.getQuery());
        TableView tableView = (TableView) dataViewer.getNode();
        TableFilter.Builder tableFilter = TableFilter.forTableView(tableView);
        ObservableList observableList = FXCollections.observableArrayList();
        while(resultSet.next())
            observableList.add(new DataObjectBuilder(resultSet).getData(dataViewer.getQuery()));
        tableView.setItems(observableList);
        tableFilter.apply();
        resultSet.close();
    }

    public void tryToDeleteRow(String tableName, int id) throws SQLException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Would you like to delete this row?");
        ButtonType buttonTypeOne = new ButtonType("Delete");
        alert.getButtonTypes().setAll(buttonTypeOne);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne) {
            String query = "DELETE FROM " + tableName + " WHERE id = '" + id + "'";
            databaseManager.update(query);
        }
    }

    public void addToCamperTableQuery(Camper camper) throws SQLException {
      String query =  "INSERT INTO camper VALUES('" + camper.getId() + "','" + camper.getName() + "','" +
                camper.getBalance()+ "')";
      databaseManager.update(query);
    }

    public void addToItemTableQuery(Item item) throws SQLException {
      String query = "INSERT INTO item VALUES('" + item.getId() + "','" + item.getName() + "','" + item.getPrice() +
                "','" + item.getQuantity() + "','"+ item.getImageURL() + "','" + ItemType.itemTypeToInt(item.getItemType()) + "')";
      databaseManager.update(query);

    }

    public void addToEmployeeTableQuery(Employee employee) throws SQLException {
        employee.requestPermissionToModifyEmployees();
        String query = "INSERT INTO employee VALUES('" + employee.getId() + "','" + employee.getUsername() + "','" +
                new PassHash().tryToGetSaltedHash(employee.getPassword()) + "','" +
                EmployeeType.employeeTypeToInt(employee.getEmployeeType()) + "')";
        databaseManager.update(query);
    }


}
