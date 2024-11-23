package juanBarrero_comp228lab5;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.control.DatePicker;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleObjectProperty;


import java.io.IOException;
import java.sql.*;

public class videogames_form extends Application {
    public Connection databaseLink;

    private PreparedStatement insertPlayer;
    private PreparedStatement insertGame;
    private PreparedStatement insertPlayersAndGames;
    private PreparedStatement displayAllPlayers;
    private PreparedStatement updatePlayer;


    private TextField updatePlayerField,nameField, lastNameField, addressField, cityField, postalCodeField, phoneNumberField, gameTitleField,gameScoreField;
    private DatePicker gameDateField;
    private TableView<Object[]> tableView = new TableView<>();


    public void getConnection() {
        String databaseUser = "COMP228_F24_sy_67";
        String databasePassword = "password";
        String url = "jdbc:oracle:thin:@oracle1.centennialcollege.ca:1521:SQLD";
        //jdbc:mysql://url:3306/connection1
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
            System.out.println("Connected to the database");
            String insertQueryPlayers = "INSERT INTO PLAYERS(FirstName, LastName, Address, PostalCode, Province, PhoneNumber) " +
                    "VALUES (?, ?, ?, ?, ?, ?) RETURNING PlayerID INTO ?";
            insertPlayer = databaseLink.prepareStatement(insertQueryPlayers, Statement.RETURN_GENERATED_KEYS);
            String insertQueryGames = "INSERT INTO Games(GameTitle) VALUES (?) RETURNING GameID INTO ?";
            insertGame = databaseLink.prepareStatement(insertQueryGames, Statement.RETURN_GENERATED_KEYS);
            String insertQueryPlayerAndGames = "INSERT INTO PlayerAndGame(playerID, GameID, PlayingDate, Score) VALUES(?, ?, ?, ?)";
            insertPlayersAndGames = databaseLink.prepareStatement(insertQueryPlayerAndGames);
            String UpdateByPLayerID = "UPDATE PLAYERS SET FirstName = ?, LastName = ?, Address = ?, PostalCode = ?, Province = ?, PhoneNumber = ?  WHERE PlayerID = ?";
            String displayAllPlayersQuery = "SELECT \n" +
                    "    C.PLAYERID AS ID, \n" +
                    "    C.FIRSTNAME AS NAME, \n" +
                    "    C.ADDRESS, \n" +
                    "    C.POSTALCODE, \n" +
                    "    C.PROVINCE, \n" +
                    "    C.PHONENUMBER, \n" +
                    "    B.GAMETITLE, \n" +
                    "    A.SCORE, \n" +
                    "    A.PLAYINGDATE\n" +
                    "FROM \n" +
                    "    PLAYERANDGAME A\n" +
                    "INNER JOIN \n" +
                    "    GAMES B ON A.GAMEID = B.GAMEID\n" +
                    "INNER JOIN \n" +
                    "    PLAYERS C ON A.PLAYERID = C.PLAYERID";
            displayAllPlayers = databaseLink.prepareStatement(displayAllPlayersQuery);

        } catch (Exception e) {
            e.printStackTrace();
            e.getCause();
            System.out.println("Database connection failed");
        }
    }

    public void UpdatePlayer() {
        try {
            int playerID = Integer.parseInt(updatePlayerField.getText());

            String getPlayerDetailsQuery = "SELECT FirstName, LastName, Address, PostalCode, Province, PhoneNumber FROM PLAYERS WHERE PlayerID = ?";
            PreparedStatement getPlayerDetails = databaseLink.prepareStatement(getPlayerDetailsQuery);
            getPlayerDetails.setInt(1, playerID);
            ResultSet resultSet = getPlayerDetails.executeQuery();

            if (resultSet.next()) {
                String existingFirstName = resultSet.getString("FirstName");
                String existingLastName = resultSet.getString("LastName");
                String existingAddress = resultSet.getString("Address");
                String existingPostalCode = resultSet.getString("PostalCode");
                String existingProvince = resultSet.getString("Province");
                String existingPhoneNumber = resultSet.getString("PhoneNumber");

                nameField.setText(nameField.getText().isEmpty() ? existingFirstName : nameField.getText());
                lastNameField.setText(lastNameField.getText().isEmpty() ? existingLastName : lastNameField.getText());
                addressField.setText(addressField.getText().isEmpty() ? existingAddress : addressField.getText());
                postalCodeField.setText(postalCodeField.getText().isEmpty() ? existingPostalCode : postalCodeField.getText());
                cityField.setText(cityField.getText().isEmpty() ? existingProvince : cityField.getText());
                phoneNumberField.setText(phoneNumberField.getText().isEmpty() ? existingPhoneNumber : phoneNumberField.getText());

                String updateQuery = "UPDATE PLAYERS SET FirstName = ?, LastName = ?, Address = ?, PostalCode = ?, Province = ?, PhoneNumber = ? WHERE PlayerID = ?";
                PreparedStatement updatePlayer = databaseLink.prepareStatement(updateQuery);

                updatePlayer.setString(1, nameField.getText());
                updatePlayer.setString(2, lastNameField.getText());
                updatePlayer.setString(3, addressField.getText());
                updatePlayer.setString(4, postalCodeField.getText());
                updatePlayer.setString(5, cityField.getText());
                updatePlayer.setString(6, phoneNumberField.getText());
                updatePlayer.setInt(7, playerID);

                updatePlayer.executeUpdate();
                System.out.println("Player updated successfully");
            } else {
                ShowAlert("Player ID not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ShowAlert("Update failed. Please try again.");
        }

    }

    public void DisplayAllPlayers() {
        try {
            ResultSet resultSet = displayAllPlayers.executeQuery();
            ObservableList<Object[]> data = tableView.getItems();
            data.clear();
            while (resultSet.next()) {
                data.add(new Object[]{
                        resultSet.getInt("ID"),
                        resultSet.getString("NAME"),
                        resultSet.getString("ADDRESS"),
                        resultSet.getString("POSTALCODE"),
                        resultSet.getString("PROVINCE"),
                        resultSet.getString("PHONENUMBER"),
                        resultSet.getString("GAMETITLE"),
                        resultSet.getInt("SCORE"),
                        resultSet.getDate("PLAYINGDATE")
                });
            }
            tableView.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            ShowAlert("Error fetching data.");
        }
    }

    public void insertPlayer2() {

        try {
            if (nameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                    addressField.getText().isEmpty() || cityField.getText().isEmpty() ||
                    postalCodeField.getText().isEmpty() || phoneNumberField.getText().isEmpty() ||
                    gameTitleField.getText().isEmpty() || gameScoreField.getText().isEmpty() ||
                    gameDateField.getValue() == null) {
                ShowAlert("All fields must be filled.");
                return;
            }

            if (!phoneNumberField.getText().matches("\\d+")) {
                ShowAlert("Phone number must be digits only.");
                return;
            }

            CallableStatement insertPlayerStmt = databaseLink.prepareCall("{call INSERT INTO PLAYERS(FirstName, LastName, Address, PostalCode, Province, PhoneNumber) " +
                    "VALUES (?, ?, ?, ?, ?, ?) RETURNING PlayerID INTO ?}");

            insertPlayerStmt.setString(1, nameField.getText());
            insertPlayerStmt.setString(2, lastNameField.getText());
            insertPlayerStmt.setString(3, addressField.getText());
            insertPlayerStmt.setString(4, postalCodeField.getText());
            insertPlayerStmt.setString(5, cityField.getText());
            insertPlayerStmt.setString(6, phoneNumberField.getText());
            insertPlayerStmt.registerOutParameter(7, Types.INTEGER);
            insertPlayerStmt.executeUpdate();
            int playerID = insertPlayerStmt.getInt(7);


            CallableStatement insertGameStmt = databaseLink.prepareCall("{call INSERT INTO Games(GameTitle) VALUES(?) RETURNING GameID INTO ?}");
            insertGameStmt.setString(1, gameTitleField.getText());
            insertGameStmt.registerOutParameter(2, Types.INTEGER);
            insertGameStmt.executeUpdate();
            int gameID = insertGameStmt.getInt(2);

            insertPlayersAndGames.setInt(1, playerID);
            insertPlayersAndGames.setInt(2, gameID);
            insertPlayersAndGames.setDate(3, Date.valueOf(gameDateField.getValue()));
            insertPlayersAndGames.setInt(4, Integer.parseInt(gameScoreField.getText()));
            insertPlayersAndGames.executeUpdate();

            System.out.println("Inserting player...");
            System.out.println("Inserting Game...");
            System.out.println("Player and Game inserted");
        }catch(SQLException e){
            e.printStackTrace();
            System.out.println("Insert failed");
        }
    }

    private void ShowAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Number could be only numbers not letters");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @Override
    public void start(Stage stage) throws IOException {
        getConnection();

        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(15));
        GridPane formPane = new GridPane();
        formPane.setPadding(new Insets(10));
        formPane.setHgap(10);
        formPane.setVgap(10);

        Text textLabel = new Text("Player Information:");
        formPane.add(textLabel, 0, 0, 2, 1); // Spanning 2 columns
        textLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        Label nameLabel = new Label("First Name:");
        nameField = new TextField();
        formPane.add(nameLabel, 0, 1);
        formPane.add(nameField, 1, 1);

        Label lastNameLabel = new Label("Last Name:");
        lastNameField = new TextField();
        formPane.add(lastNameLabel, 0, 2);
        formPane.add(lastNameField, 1, 2);

        Label addressLabel = new Label("Address:");
        addressField = new TextField();
        formPane.add(addressLabel, 0, 3);
        formPane.add(addressField, 1, 3);

        Label cityLabel = new Label("City:");
        cityField = new TextField();
        formPane.add(cityLabel, 0, 4);
        formPane.add(cityField, 1, 4);

        Label postalCodeLabel = new Label("Postal Code:");
        postalCodeField = new TextField();
        formPane.add(postalCodeLabel, 0, 5);
        formPane.add(postalCodeField, 1, 5);

        Label phoneNumberLabel = new Label("Phone Number:");
        phoneNumberField = new TextField();
        formPane.add(phoneNumberLabel, 0, 6);
        formPane.add(phoneNumberField, 1, 6);

            VBox rightPane = new VBox(10);
            rightPane.setPadding(new Insets(10));

            Label updatePlayerLabel = new Label("Update Player by ID:");
            updatePlayerField = new TextField();
            Button updateButton = new Button("Update");

            HBox updateButtonBox = new HBox(5, updatePlayerField, updateButton);
            rightPane.getChildren().addAll(updatePlayerLabel, updateButtonBox);

            updateButton.setOnAction(e -> UpdatePlayer());

            Text gameInfo = new Text("\nGame Information:");
            gameInfo.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
            rightPane.getChildren().add(gameInfo);

            Label gameTitleLabel = new Label("Game Title:");
            gameTitleField = new TextField();
            rightPane.getChildren().addAll(gameTitleLabel, gameTitleField);

            Label gameScoreLabel = new Label("Game Score:");
            gameScoreField = new TextField();
            rightPane.getChildren().addAll(gameScoreLabel, gameScoreField);

            Label gameDateLabel = new Label("Game Date:");
            gameDateField = new DatePicker();
            rightPane.getChildren().addAll(gameDateLabel, gameDateField);

            Button createPlayer = new Button("Create Player");
            rightPane.getChildren().add(createPlayer);

            createPlayer.setOnAction(e -> insertPlayer2());


            tableView = new TableView<>();
            TableColumn<Object[], Integer> idColumn = new TableColumn<>("ID");
            idColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue()[0]));

            TableColumn<Object[], String> nameColumn = new TableColumn<>("Name");
            nameColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((String) data.getValue()[1]));

            TableColumn<Object[], String> addressColumn = new TableColumn<>("Address");
            addressColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((String) data.getValue()[2]));

            TableColumn<Object[], String> postalCodeColumn = new TableColumn<>("Postal Code");
            postalCodeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((String) data.getValue()[3]));

            TableColumn<Object[], String> provinceColumn = new TableColumn<>("Province");
            provinceColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((String) data.getValue()[4]));

            TableColumn<Object[], String> phoneNumberColumn = new TableColumn<>("Phone Number");
            phoneNumberColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((String) data.getValue()[5]));

            TableColumn<Object[], String> gameTitleColumn = new TableColumn<>("Game Title");
            gameTitleColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((String) data.getValue()[6]));

            TableColumn<Object[], Integer> scoreColumn = new TableColumn<>("Score");
            scoreColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue()[7]));

            TableColumn<Object[], Date> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Date) data.getValue()[8]));

            tableView.getColumns().addAll(idColumn, nameColumn, addressColumn, postalCodeColumn, provinceColumn, phoneNumberColumn, gameTitleColumn, scoreColumn, dateColumn);
            formPane.add(tableView, 0, 7, 2, 1);

        Button DisplayAllPlayers = new Button("Display All Players");
        rightPane.getChildren().add(DisplayAllPlayers);

        DisplayAllPlayers.setOnAction(e -> DisplayAllPlayers());

            mainPane.setCenter(formPane);
            mainPane.setRight(rightPane);

            Scene scene = new Scene(mainPane, 640, 480);
            stage.setScene(scene);

            stage.setTitle("Player Registration \uD83C\uDFAE");
            stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}