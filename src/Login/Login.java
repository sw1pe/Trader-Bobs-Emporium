package Login;

import Data.Customers.EmployeeType;
import GUILoader.GUI;

import Interfaces.MonoQuery;
import Data.DataBaseManager;
import Util.LoggedInAccountUtil;
import javafx.scene.control.Alert;
import Security.PassHash;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

/**
 * @Author Aidan Stewart
 * @Year 2018
 * Copyright (c)
 * All rights reserved.
 */
public class Login implements MonoQuery {
    private DataBaseManager manager = new DataBaseManager();
    private String user;
    private String pass;
    private PassHash passHash = new PassHash();


    public Login(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    private void displayAlertDialog(Alert.AlertType alertType, String context){
        Alert alert = new Alert(alertType);
        alert.setContentText(context);
        alert.showAndWait();
    }

    private String alertContexts(boolean loggingIn){
        if (loggingIn)
            return "Failure to login is due to one the reasons below:" +
                    "\n-Incorrect username or password!" +
                    "\n-your account had not been enabled yet!";
        else
            return "Your account is now registered. Please wait for an admin to enable your account.";
    }

    @Override
    public void updateDatabase() throws SQLException {
        String query = "INSERT INTO employee VALUES('" + new Random().nextInt(999999) + "','" +
                user + "','" +
                passHash.tryToGetSaltedHash(pass)+ "','" +
                '3' + "')";
        manager.update(query);
        displayAlertDialog(Alert.AlertType.INFORMATION, alertContexts(false));
    }

    @Override
    public void retrieveDatabaseData() throws SQLException {
        String query = "SELECT * FROM employee WHERE username='" + user + "'";
        ResultSet resultSet = manager.receiver(query);
        if (canLogin(resultSet))
            login(resultSet);
        else
            displayAlertDialog(Alert.AlertType.ERROR, alertContexts(true));
        resultSet.close();
    }

    private boolean canLogin(ResultSet resultSet) throws SQLException {
        return resultSet.next()
                && EmployeeType.intToEmployeeType(resultSet.getInt("employeetype")) != EmployeeType.UNCONFIRMED &&
                passHash.tryToCheckHash(pass, resultSet.getString(3));
    }


    private void login(ResultSet resultSet) throws SQLException {
        setLoggedInAccountInfo(resultSet);
        openAccountInterface();
    }

    private void setLoggedInAccountInfo(ResultSet resultSet) throws SQLException {
        EmployeeType accountType = EmployeeType.intToEmployeeType(resultSet.getInt(4));
        LoggedInAccountUtil.thisUsername = resultSet.getString(1);
        LoggedInAccountUtil.thisAccountType = accountType;
    }
    private void openAccountInterface(){
        if (employeeInterfaceConditionsMet())
            new GUI("AccountTypes/Admin/AdminGUI/AdminGUI.fxml");
        else
            new GUI("AccountTypes/Vendor/VendorGUI/VendorGUI.fxml");
    }

    private boolean employeeInterfaceConditionsMet(){
        return LoggedInAccountUtil.thisAccountType == EmployeeType.EMPLOYEE
                || LoggedInAccountUtil.thisAccountType == EmployeeType.ADMIN;
    }

}
