package AccountTypes.Admin.AdminGUI;

import AccountTypes.AccountTypes;
import AccountTypes.Admin.AdminPanel;
import Data.Customers.Camper;
import Data.Customers.Employee;
import Data.ID;
import Manager.DatabaseViewer;
import Manager.Tables;
import PassProtection.PassHash;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import java.net.URL;
import java.security.cert.TrustAnchor;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @Author Aidan Stewart
 * @Year 2018
 * Copyright (c)
 * All rights reserved.
 */
public class GUIController implements Initializable {
    @FXML
    private AnchorPane mainPane;
    @FXML
    private ImageView banner;
    @FXML
    private TabPane tabPane;
    @FXML
    private TextField nameField, balanceField;
    @FXML
    private TextField usernameField, passwordField;
    @FXML
    private ChoiceBox<AccountTypes> accountTypes;
    @FXML
    private ChoiceBox itemTypes;
    @FXML
    private TableView<Employee> employeeTableView;
    @FXML
    private TableColumn<Employee, String> employeeID, username, password, accountType;
    @FXML
    private TableView<Camper> camperTableView;
    @FXML
    private TableColumn<Camper, String> camperID, name, balance;
    private AdminPanel adminPanel = new AdminPanel();
    private PassHash passHash = new PassHash();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        banner.fitWidthProperty().bind(mainPane.widthProperty());
        setCellValueFactories();
        tryToPopulateAll();
        setChoiceBoxes();

    }

    @FXML
    private void tryToPopulateAll(){
        try {
            adminPanel.retrieveDatabaseData(Tables.EMPLOYEE, employeeTableView);
            adminPanel.retrieveDatabaseData(Tables.CAMPER, camperTableView);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void tableViewClickListener(MouseEvent e) throws SQLException {
        String tableView = ((TableView) e.getSource()).getId();
        switch (tableView) {
            case "employeeTableView":
                setEmployeeFields();
                if (e.getClickCount() == 2) {
                    int empID = employeeTableView.getSelectionModel().getSelectedItem().getId();
                    deleteRow(Tables.EMPLOYEE, employeeTableView, empID);
                }
                break;
            case "camperTableView":
                setCamperFields();
                if (e.getClickCount() == 2) {
                    int cmpID = camperTableView.getSelectionModel().getSelectedItem().getId();
                    deleteRow(Tables.CAMPER, camperTableView, cmpID);
                }
                break;
        }
    }

    private void deleteRow(Tables tables, TableView tableView, int id) throws SQLException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Would you like to delete this row?");
        ButtonType buttonTypeOne = new ButtonType("Delete");
        alert.getButtonTypes().setAll(buttonTypeOne);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne) {
            String query = "DELETE FROM "+ tables.name().toLowerCase() + " WHERE id = '" + id + "'";
            adminPanel.updateDatabase(query);
            adminPanel.retrieveDatabaseData(tables, tableView);
            clearFields();
        }
    }

    private void setEmployeeFields(){
        Employee employee = employeeTableView.getSelectionModel().getSelectedItem();
        usernameField.setText(employee.getUsername());
        passwordField.setText("");
        accountTypes.setValue(employee.getAccountType());
    }

    private void setCamperFields(){
        Camper camper = camperTableView.getSelectionModel().getSelectedItem();
        nameField.setText(camper.getName());
        balanceField.setText(String.valueOf(camper.getBalance()));
    }

    @FXML
    private void buttonListener() throws SQLException {
        int tabPaneIndex = tabPane.getSelectionModel().getSelectedIndex();
        switch (tabPaneIndex){
            case 0:
                if (isTableRowNotSelected(camperTableView))
                    addToCamperTable();
                else
                    editCamperRow();
                break;
            case 2:
                if (isTableRowNotSelected(employeeTableView))
                    addToEmployeeTable();
                else
                    editEmployeeRow();
                break;
        }
        clearFields();
    }

    private boolean isTableRowNotSelected(TableView tableView){
        return tableView.getSelectionModel().getSelectedItem() == null;
    }

    private void addToCamperTable() throws SQLException {
        String query = "INSERT INTO camper VALUES('" + new ID().getId() + "','" +
                nameField.getText() + "','" +
                balanceField.getText() + "')";
        adminPanel.updateDatabase(query);
        adminPanel.retrieveDatabaseData(Tables.CAMPER, camperTableView);
    }

    private void addToEmployeeTable() throws SQLException {
        int accountType = AccountTypes.accountTypePermToInt(accountTypes.getSelectionModel().getSelectedItem());
        String query = "INSERT INTO employee VALUES('" + new ID().getId() + "','" +
                usernameField.getText() + "','" +
                passHash.tryToGetSaltedHash(passwordField.getText())+ "','" +
                accountType + "')";
        adminPanel.updateDatabase(query);
        adminPanel.retrieveDatabaseData(Tables.EMPLOYEE, employeeTableView);
    }

    private void editCamperRow() throws SQLException {
        int id = camperTableView.getSelectionModel().getSelectedItem().getId();
        String query = "UPDATE camper SET " +
                "name = '" + nameField.getText() + "',"+
                "balance = '" + balanceField.getText() + "' " +
                "WHERE id = "+ id +";";
        adminPanel.updateDatabase(query);
        adminPanel.retrieveDatabaseData(Tables.CAMPER, camperTableView);
    }

    private void editEmployeeRow() throws SQLException {
        int accountType = AccountTypes.accountTypePermToInt(accountTypes.getSelectionModel().getSelectedItem());
        int id = employeeTableView.getSelectionModel().getSelectedItem().getId();
        String query = "UPDATE employee SET " +
                "username = '" + usernameField.getText() + "',"+
                "password = '" + passHash.tryToGetSaltedHash(passwordField.getText()) + "'," +
                "accounttype = '" + accountType + "' " +
                "WHERE id = "+ id +";";
        adminPanel.updateDatabase(query);
        adminPanel.retrieveDatabaseData(Tables.EMPLOYEE, employeeTableView);
    }

    @FXML
    private void clearSelectionsOnClick() {
        if (!isTableRowNotSelected(employeeTableView) || !isTableRowNotSelected(camperTableView)) {
            employeeTableView.getSelectionModel().clearSelection();
            camperTableView.getSelectionModel().clearSelection();
            clearFields();
        }
    }

    private void clearFields(){
        clearCamperFields();
        clearEmployeeFields();
    }


    private void clearCamperFields() {
        nameField.setText("");
        balanceField.setText("");
    }

    private void clearEmployeeFields(){
        usernameField.setText("");
        passwordField.setText("");
        accountTypes.setValue(null);
    }

    private void setChoiceBoxes(){
        for (AccountTypes accountType: AccountTypes.values())
            accountTypes.getItems().add(accountType);
    }

    private void setCellValueFactories() {
        setEmployeeColumns();
        setCamperColumns();
    }

    private void setEmployeeColumns(){
        employeeID.setCellValueFactory(new PropertyValueFactory<>("id"));
        username.setCellValueFactory(new PropertyValueFactory<>("username"));
        password.setCellValueFactory(new PropertyValueFactory<>("password"));
        accountType.setCellValueFactory(new PropertyValueFactory<>("accountType"));
    }

    private void setCamperColumns(){
        camperID.setCellValueFactory(new PropertyValueFactory<>("id"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        balance.setCellValueFactory(new PropertyValueFactory<>("balance"));
    }
}
