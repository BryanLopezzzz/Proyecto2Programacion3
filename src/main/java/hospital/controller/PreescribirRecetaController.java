package hospital.controller;

import hospital.logica.RecetaLogica;
import hospital.model.*;
import hospital.controller.busqueda.BuscarPacientePreescripcionController;
import hospital.controller.busqueda.BuscarMedicamentoPreescripcionController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class PreescribirRecetaController {

    @FXML private TextField txtFechaConfeccion;
    @FXML private TextField txtBuscarPaciente;
    @FXML private Button btnBuscarPaciente;
    @FXML private DatePicker dtpFechaRetiro;
    @FXML private TableView<DetalleReceta> tblRecetas;
    @FXML private TableColumn<DetalleReceta, String> colNombreMedicamento;
    @FXML private TableColumn<DetalleReceta, String> colIndicaciones;
    @FXML private TableColumn<DetalleReceta, String> colPresentacion;
    @FXML private TableColumn<DetalleReceta, Integer> conCantidad;
    @FXML private TableColumn<DetalleReceta, Integer> colDuracion;
    @FXML private Button btnAgregarReceta;
    @FXML private Button btnEliminarReceta;
    @FXML private Button btnEditarReceta;
    @FXML private Button btnVolver;
    @FXML private Button btnPreescribir;
    @FXML private Button btnLimpiarTodo;  // Nuevo botón para limpiar manualmente

    private ObservableList<DetalleReceta> listaDetalles = FXCollections.observableArrayList();
    private final RecetaLogica recetaIntermediaria = new RecetaLogica();
    private Paciente pacienteSeleccionado;
    private Medico medico;
    private Receta recetaActual;

    @FXML
    public void initialize() {
        configurarTabla();
        configurarFechaConfeccion();
        configurarFechaRetiro();
        cargarDatosPersistentes(); // Cargar datos si existen

        tblRecetas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hay = newSel != null;
            btnEliminarReceta.setDisable(!hay);
            btnEditarReceta.setDisable(!hay);
        });
    }

    private void configurarTabla() {
        colNombreMedicamento.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getMedicamento() != null ?
                                cellData.getValue().getMedicamento().getNombre() : ""));

        colIndicaciones.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIndicaciones()));

        colPresentacion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getMedicamento() != null ?
                                cellData.getValue().getMedicamento().getPresentacion() : ""));

        conCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colDuracion.setCellValueFactory(new PropertyValueFactory<>("diasTratamiento"));

        tblRecetas.setItems(listaDetalles);

        btnEliminarReceta.setDisable(true);
        btnEditarReceta.setDisable(true);
    }

    private void configurarFechaConfeccion() {
        txtFechaConfeccion.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void configurarFechaRetiro() {
        // Solo configurar si no hay fecha ya establecida
        if (dtpFechaRetiro.getValue() == null) {
            dtpFechaRetiro.setValue(LocalDate.now().plusDays(7));
        }
    }

    private void cargarDatosPersistentes() {
        // Si hay datos de una sesión anterior, mantenerlos
        // En una implementación real podrías cargar desde un archivo temporal o preferencias
        System.out.println("Cargando datos persistentes - Detalles: " + listaDetalles.size());

        // Actualizar estado de botones
        actualizarEstadoBotones();
    }

    private void actualizarEstadoBotones() {
        btnPreescribir.setDisable(pacienteSeleccionado == null || listaDetalles.isEmpty());
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
        System.out.println("Médico configurado: " + (medico != null ? medico.getNombre() : "NULL"));
    }

    @FXML
    private void BuscarPaciente(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/buscarPacientePrescripcion.fxml"));
            Parent root = loader.load();

            BuscarPacientePreescripcionController buscarView = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Buscar Paciente");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnBuscarPaciente.getScene().getWindow());
            stage.showAndWait();

            Paciente pacienteNuevo = buscarView.getPacienteSeleccionado();
            if (pacienteNuevo != null) {
                // Solo cambiar si es diferente al actual
                if (pacienteSeleccionado == null || !pacienteSeleccionado.getId().equals(pacienteNuevo.getId())) {
                    pacienteSeleccionado = pacienteNuevo;
                    txtBuscarPaciente.setText(pacienteSeleccionado.getNombre() + " (" + pacienteSeleccionado.getId() + ")");
                    // No limpiar los medicamentos, mantenerlos
                }
                actualizarEstadoBotones();
            }

        } catch (IOException e) {
            mostrarError("Error", "No se pudo abrir la ventana de búsqueda de pacientes: " + e.getMessage());
        }
    }

    @FXML
    private void AgregarReceta(ActionEvent event) {
        if (medico == null) {
            mostrarError("Acceso denegado", "Debe estar autenticado como médico para preescribir recetas.");
            return;
        }

        try {
            if (recetaActual == null) {
                crearRecetaTemporal();
            }

            // Buscar medicamento
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/buscarMedicamentoPrescripcion.fxml"));
            Parent root = loader.load();
            BuscarMedicamentoPreescripcionController buscarView = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Buscar Medicamento");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnAgregarReceta.getScene().getWindow());
            stage.showAndWait();

            Medicamento seleccionado = buscarView.getMedicamentoSeleccionado();
            if (seleccionado == null) {
                return;
            }

            // Verificar si el medicamento ya está en la lista
            boolean yaExiste = listaDetalles.stream()
                    .anyMatch(d -> d.getMedicamento() != null &&
                            d.getMedicamento().getCodigo().equals(seleccionado.getCodigo()));

            if (yaExiste) {
                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacion.setTitle("Medicamento duplicado");
                confirmacion.setHeaderText("El medicamento ya está en la receta");
                confirmacion.setContentText("¿Desea agregar una nueva entrada para " + seleccionado.getNombre() + "?");

                Optional<ButtonType> resultado = confirmacion.showAndWait();
                if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
                    return;
                }
            }

            // Abrir detalle
            FXMLLoader detalleLoader = new FXMLLoader(getClass().getResource("/hospital/view/detalleReceta.fxml"));
            Parent detalleRoot = detalleLoader.load();
            DetalleRecetaController detalleView = detalleLoader.getController();

            detalleView.setContext(medico, recetaActual.getId(), seleccionado.getCodigo());

            Stage detalleStage = new Stage();
            detalleStage.setTitle("Detalle de Medicamento");
            detalleStage.setScene(new Scene(detalleRoot));
            detalleStage.initModality(Modality.WINDOW_MODAL);
            detalleStage.initOwner(btnAgregarReceta.getScene().getWindow());
            detalleStage.showAndWait();

            DetalleReceta nuevoDetalle = detalleView.getDetalleCreado();
            if (nuevoDetalle != null) {
                agregarDetalleReceta(nuevoDetalle);
                actualizarTablaDetalles();
                actualizarEstadoBotones();
            }

        } catch (IOException e) {
            mostrarError("Error", "No se pudo abrir la ventana de selección de medicamentos: " + e.getMessage());
        }
    }

    @FXML
    private void EliminarReceta(ActionEvent event) {
        DetalleReceta detalleSeleccionado = tblRecetas.getSelectionModel().getSelectedItem();
        if (detalleSeleccionado == null) {
            mostrarAdvertencia("Selección requerida", "Debe seleccionar un detalle para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar medicamento?");
        confirmacion.setContentText("¿Está seguro que desea eliminar " +
                detalleSeleccionado.getMedicamento().getNombre() + " de la receta?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            listaDetalles.remove(detalleSeleccionado);
            if (recetaActual != null) {
                recetaActual.getDetalles().remove(detalleSeleccionado);
            }
            actualizarEstadoBotones();
        }
    }

    @FXML
    private void EditarReceta(ActionEvent event) {
        DetalleReceta detalleSeleccionado = tblRecetas.getSelectionModel().getSelectedItem();
        if (detalleSeleccionado == null) {
            mostrarAdvertencia("Selección requerida", "Debe seleccionar un detalle para editar.");
            return;
        }
        if (medico == null) {
            mostrarError("Acceso denegado", "Debe estar autenticado como médico para editar la receta.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hospital/view/detalleReceta.fxml"));
            Parent root = loader.load();

            DetalleRecetaController detalleView = loader.getController();

            String recetaId = recetaActual != null ? recetaActual.getId() : generarIdReceta();
            String medicamentoId = null;
            if (detalleSeleccionado.getMedicamento() != null) {
                medicamentoId = detalleSeleccionado.getMedicamento().getCodigo();
            }

            detalleView.setContext(medico, recetaId, medicamentoId);
            detalleView.setDetalleParaEditar(detalleSeleccionado);

            Stage stage = new Stage();
            stage.setTitle("Editar Detalle de Receta");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnEditarReceta.getScene().getWindow());
            stage.showAndWait();

            DetalleReceta editado = detalleView.getDetalleCreado();
            if (editado != null) {
                if (!listaDetalles.contains(editado)) {
                    listaDetalles.remove(detalleSeleccionado);
                    listaDetalles.add(editado);
                }
                if (recetaActual != null) {
                    recetaActual.getDetalles().clear();
                    recetaActual.getDetalles().addAll(listaDetalles);
                }
                actualizarTablaDetalles();
            }

        } catch (IOException e) {
            mostrarError("Error", "No se pudo abrir el formulario de edición: " + e.getMessage());
        }
    }

    @FXML
    private void Preescribir(ActionEvent event) {
        if (pacienteSeleccionado == null) {
            mostrarAdvertencia("Paciente requerido", "Debe seleccionar un paciente.");
            return;
        }
        if (medico == null) {
            mostrarError("Acceso denegado", "Debe estar autenticado como médico para preescribir recetas.");
            return;
        }
        if (dtpFechaRetiro.getValue() == null) {
            mostrarAdvertencia("Fecha requerida", "Debe establecer una fecha de retiro.");
            return;
        }
        if (dtpFechaRetiro.getValue().isBefore(LocalDate.now())) {
            mostrarAdvertencia("Fecha inválida", "La fecha de retiro no puede ser anterior a hoy.");
            return;
        }
        if (listaDetalles.isEmpty()) {
            mostrarAdvertencia("Medicamentos requeridos", "Debe agregar al menos un medicamento a la receta.");
            return;
        }

        try {
            Receta receta = new Receta();
            receta.setId(recetaActual != null ? recetaActual.getId() : generarIdReceta());
            receta.setPaciente(pacienteSeleccionado);
            receta.setMedico(medico);
            receta.setFecha(LocalDate.now());
            receta.setFechaRetiro(dtpFechaRetiro.getValue());
            receta.setEstado(EstadoReceta.CONFECCIONADA);

            for (DetalleReceta d : listaDetalles) {
                receta.agregarDetalle(d);
            }

            recetaIntermediaria.crearReceta(medico, receta);

            mostrarInformacion("Éxito", "Receta preescrita correctamente con ID: " + receta.getId());

            // Limpiar formulario para la siguiente receta
            limpiarFormulario();

        } catch (Exception e) {
            mostrarError("Error al preescribir", e.getMessage());
        }
    }

        @FXML
    private void Volver(ActionEvent event) {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hospital/view/dashboard.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            mostrarError("Error", "No se pudo cargar el dashboard: " + e.getMessage());
        }
    }

    private void crearRecetaTemporal() {
        recetaActual = new Receta();
        recetaActual.setId(generarIdReceta());
        if (pacienteSeleccionado != null) {
            recetaActual.setPaciente(pacienteSeleccionado);
        }
        recetaActual.setMedico(medico);
        recetaActual.setFecha(LocalDate.now());
        recetaActual.setFechaRetiro(dtpFechaRetiro.getValue());
        recetaActual.setEstado(EstadoReceta.CONFECCIONADA);
    }

    private String generarIdReceta() {
        return "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void actualizarTablaDetalles() {
        if (recetaActual != null) {
            recetaActual.getDetalles().clear();
            recetaActual.getDetalles().addAll(listaDetalles);
        }
        tblRecetas.refresh();
    }

    private void limpiarFormulario() {
        pacienteSeleccionado = null;
        recetaActual = null;
        txtBuscarPaciente.clear();
        listaDetalles.clear();
        dtpFechaRetiro.setValue(LocalDate.now().plusDays(7));
        configurarFechaConfeccion();
        actualizarEstadoBotones();

        System.out.println("Formulario limpiado completamente");
    }

    public void agregarDetalleReceta(DetalleReceta detalle) {
        if (detalle != null) {
            listaDetalles.add(detalle);
            if (recetaActual != null) {
                recetaActual.agregarDetalle(detalle);
            }
            System.out.println("Detalle agregado. Total: " + listaDetalles.size());
        }
    }

    // Getters
    public Receta getRecetaActual() { return recetaActual; }
    public Paciente getPacienteSeleccionado() { return pacienteSeleccionado; }

    // Métodos de UI
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}