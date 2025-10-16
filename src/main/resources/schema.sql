-- =============================================
-- HOSPITAL DATABASE - SETUP COMPLETO
-- =============================================
DROP DATABASE IF EXISTS hospital;

CREATE DATABASE hospital
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hospital;

-- =============================================
-- TABLAS DE USUARIOS
-- =============================================

CREATE TABLE administrador (
                               id VARCHAR(50) PRIMARY KEY,
                               clave VARCHAR(100) NOT NULL,
                               nombre VARCHAR(100) NOT NULL
);

CREATE TABLE medico (
                        id VARCHAR(50) PRIMARY KEY,
                        clave VARCHAR(100) NOT NULL,
                        nombre VARCHAR(100) NOT NULL,
                        especialidad VARCHAR(100) NOT NULL
);

CREATE TABLE farmaceuta (
                            id VARCHAR(50) PRIMARY KEY,
                            clave VARCHAR(100) NOT NULL,
                            nombre VARCHAR(100) NOT NULL
);

-- =============================================
-- TABLAS DE DOMINIO
-- =============================================

CREATE TABLE paciente (
                          id VARCHAR(50) PRIMARY KEY,
                          nombre VARCHAR(100) NOT NULL,
                          fecha_nacimiento DATE NOT NULL,
                          telefono VARCHAR(20) NOT NULL
);

CREATE TABLE medicamento (
                             codigo VARCHAR(50) PRIMARY KEY,
                             nombre VARCHAR(100) NOT NULL,
                             presentacion VARCHAR(100) NOT NULL
);

-- =============================================
-- TABLAS DE TRANSACCIONES
-- =============================================

CREATE TABLE receta (
                        id VARCHAR(50) PRIMARY KEY,
                        paciente_id VARCHAR(50) NOT NULL,
                        medico_id VARCHAR(50) NOT NULL,
                        fecha DATE NOT NULL,
                        fecha_retiro DATE NOT NULL,
                        estado VARCHAR(20) NOT NULL,
                        CONSTRAINT fk_receta_paciente FOREIGN KEY (paciente_id) REFERENCES paciente(id) ON DELETE CASCADE,
                        CONSTRAINT fk_receta_medico FOREIGN KEY (medico_id) REFERENCES medico(id) ON DELETE CASCADE
);

CREATE TABLE detalle_receta (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                receta_id VARCHAR(50) NOT NULL,
                                medicamento_codigo VARCHAR(50) NOT NULL,
                                cantidad INT NOT NULL,
                                indicaciones TEXT NOT NULL,
                                dias_tratamiento INT NOT NULL DEFAULT 7,
                                CONSTRAINT fk_detalle_receta FOREIGN KEY (receta_id) REFERENCES receta(id) ON DELETE CASCADE,
                                CONSTRAINT fk_detalle_medicamento FOREIGN KEY (medicamento_codigo) REFERENCES medicamento(codigo) ON DELETE CASCADE
);

-- =============================================
-- ÍNDICES PARA MEJOR RENDIMIENTO
-- =============================================

CREATE INDEX idx_medico_nombre ON medico(nombre);
CREATE INDEX idx_farmaceuta_nombre ON farmaceuta(nombre);
CREATE INDEX idx_paciente_nombre ON paciente(nombre);
CREATE INDEX idx_medicamento_nombre ON medicamento(nombre);
CREATE INDEX idx_receta_paciente ON receta(paciente_id);
CREATE INDEX idx_receta_medico ON receta(medico_id);
CREATE INDEX idx_receta_estado ON receta(estado);
CREATE INDEX idx_receta_fecha ON receta(fecha);
CREATE INDEX idx_detalle_receta ON detalle_receta(receta_id);
CREATE INDEX idx_detalle_medicamento ON detalle_receta(medicamento_codigo);

-- =============================================
-- DATOS INICIALES
-- =============================================

-- Usuario admin por defecto
INSERT INTO administrador (id, clave, nombre) VALUES
    ('admin', 'admin', 'Atenea');

-- Médicos de prueba
INSERT INTO medico (id, clave, nombre, especialidad) VALUES
                                                         ('M001', 'M001', 'Dr. Carlos Ramírez', 'Medicina General'),
                                                         ('M002', 'M002', 'Dra. Ana Martínez', 'Pediatría'),
                                                         ('M003', 'M003', 'Dr. José Hernández', 'Cardiología');

-- Farmaceutas de prueba
INSERT INTO farmaceuta (id, clave, nombre) VALUES
                                               ('F001', 'F001', 'Laura Gómez'),
                                               ('F002', 'F002', 'Pedro Sánchez');

-- Pacientes de prueba
INSERT INTO paciente (id, nombre, fecha_nacimiento, telefono) VALUES
                                                                  ('P001', 'Juan Pérez García', '1985-03-15', '8888-1234'),
                                                                  ('P002', 'María López Rodríguez', '1990-07-22', '8888-5678'),
                                                                  ('P003', 'Carlos Mora Jiménez', '1978-11-30', '8888-9012');

-- Medicamentos de prueba
INSERT INTO medicamento (codigo, nombre, presentacion) VALUES
                                                           ('MED001', 'Paracetamol', 'Tabletas 500mg'),
                                                           ('MED002', 'Ibuprofeno', 'Cápsulas 400mg'),
                                                           ('MED003', 'Amoxicilina', 'Suspensión 250mg/5ml'),
                                                           ('MED004', 'Loratadina', 'Tabletas 10mg'),
                                                           ('MED005', 'Omeprazol', 'Cápsulas 20mg');

-- Recetas de prueba (con fechas de 2025 para gráficos)
INSERT INTO receta (id, paciente_id, medico_id, fecha, fecha_retiro, estado) VALUES
                                                                                 ('REC-001', 'P001', 'M001', '2025-09-15', '2025-09-22', 'ENTREGADA'),
                                                                                 ('REC-002', 'P002', 'M002', '2025-08-16', '2025-08-23', 'LISTA'),
                                                                                 ('REC-003', 'P003', 'M003', '2025-07-17', '2025-07-24', 'EN_PROCESO');

-- Detalles de recetas
INSERT INTO detalle_receta (receta_id, medicamento_codigo, cantidad, indicaciones, dias_tratamiento) VALUES
                                                                                                         ('REC-001', 'MED001', 20, 'Tomar 1 tableta cada 8 horas después de las comidas', 7),
                                                                                                         ('REC-002', 'MED003', 1, 'Tomar 5ml cada 8 horas por 10 días', 10),
                                                                                                         ('REC-003', 'MED002', 15, 'Tomar 1 cápsula cada 12 horas con alimentos', 5),
                                                                                                         ('REC-003', 'MED005', 30, 'Tomar 1 cápsula en ayunas cada mañana', 30);

-- =============================================
-- VERIFICACIÓN
-- =============================================

SELECT '✓ Base de datos creada' AS Estado;

SELECT 'Administradores' AS Tabla, COUNT(*) AS Total FROM administrador
UNION ALL SELECT 'Médicos', COUNT(*) FROM medico
UNION ALL SELECT 'Farmaceutas', COUNT(*) FROM farmaceuta
UNION ALL SELECT 'Pacientes', COUNT(*) FROM paciente
UNION ALL SELECT 'Medicamentos', COUNT(*) FROM medicamento
UNION ALL SELECT 'Recetas', COUNT(*) FROM receta
UNION ALL SELECT 'Detalles de Receta', COUNT(*) FROM detalle_receta;

SELECT '
================================================
✓ CONFIGURACIÓN COMPLETADA
================================================
Login: admin / admin
================================================
' AS MENSAJE;