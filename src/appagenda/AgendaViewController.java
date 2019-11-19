package appagenda;

import entidades.Persona;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class AgendaViewController implements Initializable {

    private EntityManager entityManager;
    private Persona personaSeleccionada;

    @FXML
    private TableView<Persona> tableViewAgenda;
    @FXML
    private TableColumn<Persona, String> columnNombre;
    @FXML
    private TableColumn<Persona, String> columnApellidos;
    @FXML
    private TableColumn<Persona, String> columnEmail;
    @FXML
    private TableColumn<Persona, String> columnProvincia;
    @FXML
    private TextField textFieldNombre;
    @FXML
    private TextField textFieldApellidos;
    @FXML
    private Button buttonNuevo;
    @FXML
    private Button buttonEditar;
    @FXML
    private Button buttonSuprimir;
    @FXML
    private AnchorPane rootAgendaView;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        columnNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        columnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        columnProvincia.setCellValueFactory(
                cellData -> {
                    SimpleStringProperty property = new SimpleStringProperty();
                    if (cellData.getValue().getProvincia() != null) {
                        property.setValue(cellData.getValue().getProvincia().getNombre());
                    }
                    return property;
                });

        tableViewAgenda.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    personaSeleccionada = newValue;
                });

        tableViewAgenda.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            personaSeleccionada = newValue;
            if (personaSeleccionada != null) {
                textFieldNombre.setText(personaSeleccionada.getNombre());
                textFieldApellidos.setText(personaSeleccionada.getApellidos());
            } else {
                textFieldNombre.setText("");
                textFieldApellidos.setText("");
            }
        });
    }

    public void cargarTodasPersonas() {
        Query queryPersonaFindAll = entityManager.createNamedQuery("Persona.findAll");
        List<Persona> listPersona = queryPersonaFindAll.getResultList();
        tableViewAgenda.setItems(FXCollections.observableArrayList(listPersona));
    }

    @FXML
    private void onActionButtonGuardar(ActionEvent event) {
        if (personaSeleccionada != null) {
            personaSeleccionada.setNombre(textFieldNombre.getText());
            personaSeleccionada.setApellidos(textFieldApellidos.getText());

            //Actualiza la persona seleccionada en la base de  datos
            entityManager.getTransaction().begin();
            entityManager.merge(personaSeleccionada);
            entityManager.getTransaction().commit();

            //Devolvemos el foco a la fila seleccionada anteriormente en la tabla
            int numFilaSeleccionada = tableViewAgenda.getSelectionModel().getSelectedIndex();
            tableViewAgenda.getItems().set(numFilaSeleccionada, personaSeleccionada);
            TablePosition pos = new TablePosition(tableViewAgenda, numFilaSeleccionada, null);
            tableViewAgenda.getFocusModel().focus(pos);
            tableViewAgenda.requestFocus();
        }
    }

    @FXML
    private void onActionButtonNuevo(ActionEvent event) {
        try {
            // Cargar la vista de detalle 
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Formulario.fxml"));
            Parent rootFormularioView = fxmlLoader.load();

            FormularioController formularioViewController = (FormularioController) fxmlLoader.getController();
            formularioViewController.setRootAgendaView(rootAgendaView);

            // Ocultar la vista de la lista 
            rootAgendaView.setVisible(false);
            //Añadir la vista detalle al StackPane principal para que se muestre
            StackPane rootMain = (StackPane) rootAgendaView.getScene().getRoot();
            rootMain.getChildren().add(rootFormularioView);

            //Intercambio de datos funcionales con el detalle 
            formularioViewController.setTableViewPrevio(tableViewAgenda);

            // Para el botón Nuevo: 
            personaSeleccionada = new Persona();
            formularioViewController.setPersona(entityManager, personaSeleccionada, true);

            formularioViewController.mostrarDatos();

        } catch (IOException ex) {
            Logger.getLogger(AgendaViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void onActionButtonEditar(ActionEvent event) {
        try {
            // Cargar la vista de detalle 
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Formulario.fxml"));
            Parent rootDetalleView = fxmlLoader.load();

            FormularioController formularioViewController = (FormularioController) fxmlLoader.getController();
            formularioViewController.setRootAgendaView(rootAgendaView);

            // Ocultar la vista de la lista 
            rootAgendaView.setVisible(false);
            //Añadir la vista detalle al StackPane principal para que se muestre
            StackPane rootMain = (StackPane) rootAgendaView.getScene().getRoot();
            rootMain.getChildren().add(rootDetalleView);

            //Intercambio de datos funcionales con el detalle 
            formularioViewController.setTableViewPrevio(tableViewAgenda);

            // Pasa la persona seleccionada en el tableview al formulario
            formularioViewController.setPersona(entityManager, personaSeleccionada, false);

            // Muestra los datos en el formulario
            formularioViewController.mostrarDatos();

        } catch (IOException ex) {
            Logger.getLogger(AgendaViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void onActionButtonSuprimir(ActionEvent event) {

        //Crea una nueva alerta
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText("¿Desea suprimir el siguiente registro?");
        alert.setContentText(personaSeleccionada.getNombre() + " " + personaSeleccionada.getApellidos());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            // Acciones a realizar si el usuario acepta
            entityManager.getTransaction().begin();
            // Nos aseguramos con merge que se está gestionando el objeto seleccionado
            entityManager.merge(personaSeleccionada);
            // Se borra la persona
            entityManager.remove(personaSeleccionada);
            entityManager.getTransaction().commit();

            // Borra la persona seleccionada de la lista
            tableViewAgenda.getItems().remove(personaSeleccionada);
            // Elimina el foco
            tableViewAgenda.getFocusModel().focus(null);
            // Pide un foco nuevo
            tableViewAgenda.requestFocus();
        } 
        else {
            // Acciones a realizar si el usuario cancela
            // Vuelve a colocar a la persona en su sitio
            int numFilaSeleccionada= tableViewAgenda.getSelectionModel().getSelectedIndex();
            tableViewAgenda.getItems().set(numFilaSeleccionada,personaSeleccionada);
            
            // Devuelve el foco a donde estaba
            TablePosition pos = new TablePosition(tableViewAgenda, numFilaSeleccionada,null);
            tableViewAgenda.getFocusModel().focus(pos); tableViewAgenda.requestFocus();
        }
    }
}
