
DROP DATABASE IF EXISTS hospital;

CREATE DATABASE hospital
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hospital;

CREATE TABLE administrador (
                               id VARCHAR(50) PRIMARY KEY,
                               clave VARCHAR(255) NOT NULL,
                               nombre VARCHAR(100) NOT NULL,
                               fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               INDEX idx_admin_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE medico (
                        id VARCHAR(50) PRIMARY KEY,
                        clave VARCHAR(255) NOT NULL,
                        nombre VARCHAR(100) NOT NULL,
                        especialidad VARCHAR(100) NOT NULL,
                        fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_medico_nombre (nombre),
                        INDEX idx_medico_especialidad (especialidad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE farmaceuta (
                            id VARCHAR(50) PRIMARY KEY,
                            clave VARCHAR(255) NOT NULL,
                            nombre VARCHAR(100) NOT NULL,
                            fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            INDEX idx_farmaceuta_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE medicamento (
                             codigo VARCHAR(50) PRIMARY KEY,
                             nombre VARCHAR(150) NOT NULL,
                             presentacion VARCHAR(100) NOT NULL,
                             fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             INDEX idx_medicamento_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE paciente (
                          id VARCHAR(50) PRIMARY KEY,
                          nombre VARCHAR(100) NOT NULL,
                          fecha_nacimiento DATE NOT NULL,
                          telefono VARCHAR(20) NOT NULL,
                          fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          INDEX idx_paciente_nombre (nombre),
                          INDEX idx_paciente_telefono (telefono)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE receta (
                        id VARCHAR(50) PRIMARY KEY,
                        paciente_id VARCHAR(50) NOT NULL,
                        medico_id VARCHAR(50) NOT NULL,
                        fecha DATE NOT NULL,
                        fecha_retiro DATE NOT NULL,
                        estado ENUM('CONFECCIONADA', 'EN_PROCESO', 'LISTA', 'ENTREGADA', 'CANCELADA')
                                                NOT NULL DEFAULT 'CONFECCIONADA',
                        fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        CONSTRAINT fk_receta_paciente FOREIGN KEY (paciente_id)
                            REFERENCES paciente(id) ON DELETE RESTRICT ON UPDATE CASCADE,
                        CONSTRAINT fk_receta_medico FOREIGN KEY (medico_id)
                            REFERENCES medico(id) ON DELETE RESTRICT ON UPDATE CASCADE,

                        INDEX idx_receta_paciente (paciente_id),
                        INDEX idx_receta_medico (medico_id),
                        INDEX idx_receta_fecha (fecha),
                        INDEX idx_receta_estado (estado),
                        INDEX idx_receta_fecha_retiro (fecha_retiro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE detalle_receta (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                receta_id VARCHAR(50) NOT NULL,
                                medicamento_codigo VARCHAR(50) NOT NULL,
                                cantidad INT NOT NULL CHECK (cantidad > 0),
                                indicaciones TEXT NOT NULL,
                                dias_tratamiento INT NOT NULL CHECK (dias_tratamiento > 0),
                                fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_detalle_receta FOREIGN KEY (receta_id)
                                    REFERENCES receta(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                CONSTRAINT fk_detalle_medicamento FOREIGN KEY (medicamento_codigo)
                                    REFERENCES medicamento(codigo) ON DELETE RESTRICT ON UPDATE CASCADE,


                                CONSTRAINT uk_receta_medicamento UNIQUE (receta_id, medicamento_codigo),

                                INDEX idx_detalle_receta (receta_id),
                                INDEX idx_detalle_medicamento (medicamento_codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Administrador por defecto (clave = id = "admin")
INSERT INTO administrador (id, clave, nombre) VALUES
    ('admin', 'admin', 'Atenea');

-- Médicos de prueba
INSERT INTO medico (id, clave, nombre, especialidad) VALUES
                                                         ('M001', 'M001', 'Dr. Carlos Ramírez', 'Medicina General'),
                                                         ('M002', 'M002', 'Dra. Ana López', 'Pediatría'),
                                                         ('M003', 'M003', 'Dr. Jorge Herrera', 'Cardiología');

-- Farmaceutas de prueba
INSERT INTO farmaceuta (id, clave, nombre) VALUES
                                               ('F001', 'F001', 'María González'),
                                               ('F002', 'F002', 'Pedro Martínez');

-- Pacientes de prueba
INSERT INTO paciente (id, nombre, fecha_nacimiento, telefono) VALUES
                                                                  ('P001', 'Juan Pérez Mora', '1985-03-15', '8888-1111'),
                                                                  ('P002', 'María Rodríguez Castro', '1990-07-22', '8888-2222'),
                                                                  ('P003', 'Carlos Jiménez Vega', '1975-11-08', '8888-3333'),
                                                                  ('P004', 'Laura Fernández Soto', '2000-01-30', '8888-4444');

-- Medicamentos de prueba
INSERT INTO medicamento (codigo, nombre, presentacion) VALUES
                                                           ('MED001', 'Paracetamol', 'Tabletas 500mg'),
                                                           ('MED002', 'Ibuprofeno', 'Tabletas 400mg'),
                                                           ('MED003', 'Amoxicilina', 'Cápsulas 500mg'),
                                                           ('MED004', 'Losartán', 'Tabletas 50mg'),
                                                           ('MED005', 'Metformina', 'Tabletas 850mg'),
                                                           ('MED006', 'Omeprazol', 'Cápsulas 20mg'),
                                                           ('MED007', 'Loratadina', 'Tabletas 10mg'),
                                                           ('MED008', 'Acetaminofén', 'Jarabe 120mg/5ml');

-- Recetas de prueba
INSERT INTO receta (id, paciente_id, medico_id, fecha, fecha_retiro, estado) VALUES
                                                                                 ('REC-001', 'P001', 'M001', '2025-01-15', '2025-01-22', 'CONFECCIONADA'),
                                                                                 ('REC-002', 'P002', 'M002', '2025-01-16', '2025-01-23', 'EN_PROCESO'),
                                                                                 ('REC-003', 'P003', 'M003', '2025-01-17', '2025-01-24', 'LISTA'),
                                                                                 ('REC-004', 'P004', 'M001', '2025-01-18', '2025-01-25', 'ENTREGADA');

-- Detalles de recetas de prueba
INSERT INTO detalle_receta (receta_id, medicamento_codigo, cantidad, indicaciones, dias_tratamiento) VALUES
                                                                                                         ('REC-001', 'MED001', 10, 'Tomar 1 tableta cada 8 horas después de las comidas', 5),
                                                                                                         ('REC-001', 'MED003', 21, 'Tomar 1 cápsula cada 8 horas por 7 días completos', 7),

                                                                                                         ('REC-002', 'MED008', 1, 'Administrar 5ml cada 6 horas en caso de fiebre', 3),
                                                                                                         ('REC-002', 'MED007', 7, 'Tomar 1 tableta diaria en la noche', 7),

                                                                                                         ('REC-003', 'MED004', 30, 'Tomar 1 tableta diaria en ayunas', 30),
                                                                                                         ('REC-003', 'MED006', 30, 'Tomar 1 cápsula 30 minutos antes del desayuno', 30),

                                                                                                         ('REC-004', 'MED002', 12, 'Tomar 1 tableta cada 8 horas con alimentos', 4),
                                                                                                         ('REC-004', 'MED001', 6, 'Tomar 1 tableta en caso de dolor', 3);



SELECT
    r.id AS receta_id,
    r.fecha,
    r.estado,
    p.nombre AS paciente,
    m.nombre AS medico,
    COUNT(dr.id) AS num_medicamentos
FROM receta r
         INNER JOIN paciente p ON r.paciente_id = p.id
         INNER JOIN medico m ON r.medico_id = m.id
         LEFT JOIN detalle_receta dr ON r.id = dr.receta_id
GROUP BY r.id, r.fecha, r.estado, p.nombre, m.nombre
ORDER BY r.fecha DESC;


SELECT
    r.id AS receta,
    med.nombre AS medicamento,
    dr.cantidad,
    dr.dias_tratamiento AS dias,
    dr.indicaciones
FROM receta r
         INNER JOIN detalle_receta dr ON r.id = dr.receta_id
         INNER JOIN medicamento med ON dr.medicamento_codigo = med.codigo
WHERE r.id = 'REC-001';



SELECT estado, COUNT(*) as cantidad
FROM receta
GROUP BY estado
ORDER BY cantidad DESC;


SELECT
    m.nombre,
    m.presentacion,
    SUM(dr.cantidad) as total_recetado,
    COUNT(DISTINCT dr.receta_id) as num_recetas
FROM detalle_receta dr
         INNER JOIN medicamento m ON dr.medicamento_codigo = m.codigo
GROUP BY m.nombre, m.presentacion
ORDER BY total_recetado DESC
LIMIT 10;


SELECT
    m.nombre AS medico,
    m.especialidad,
    COUNT(r.id) as num_recetas
FROM medico m
         LEFT JOIN receta r ON m.id = r.medico_id
GROUP BY m.nombre, m.especialidad
ORDER BY num_recetas DESC;