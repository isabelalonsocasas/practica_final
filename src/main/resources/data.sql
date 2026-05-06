-- Insertamos primero los roles porque los usuarios dependen de ellos (Foreign Key)
INSERT INTO rol (nombre_rol, descripcion)
VALUES ('USER', 'Usuario estándar del sistema');

INSERT INTO rol (nombre_rol, descripcion)
VALUES ('ADMIN', 'Administrador con control total');

-- Usuarios
INSERT INTO usuario (nombre, apellidos, email, password, telefono, rol_id, fecha_registro, activo)
VALUES ('Pepe', 'Fernández Freige', 'pepe@gmail.com', '1234', '600111222', 2, CURRENT_TIMESTAMP, TRUE);

INSERT INTO usuario (nombre, apellidos, email, password, telefono, rol_id, fecha_registro, activo)
VALUES ('Isabel', 'Alonso Casas', 'isabel@gmail.com', '1234', '600333444', 1, CURRENT_TIMESTAMP, TRUE);

-- Pistas
INSERT INTO pista (nombre, ubicacion, precio_hora, activa, fecha_alta)
VALUES ('Pista Central', 'Pabellón Norte - Interior', 15.50, TRUE, CURRENT_TIMESTAMP);

INSERT INTO pista (nombre, ubicacion, precio_hora, activa, fecha_alta)
VALUES ('Pista 2', 'Complejo Sur - Exterior', 10.00, TRUE, CURRENT_TIMESTAMP);

INSERT INTO pista (nombre, ubicacion, precio_hora, activa, fecha_alta)
VALUES ('Pista 3', 'Complejo Sur - Exterior', 10.00, FALSE, CURRENT_TIMESTAMP);

-- Reservas
INSERT INTO reserva (id_usuario, id_pista, fecha_reserva, hora_inicio, duracion_minutos, hora_fin, estado, fecha_creacion)
VALUES (2, 1, '2026-03-20', '10:00:00', 90, '11:30:00', 'ACTIVA', CURRENT_TIMESTAMP);

INSERT INTO reserva (id_usuario, id_pista, fecha_reserva, hora_inicio, duracion_minutos, hora_fin, estado, fecha_creacion)
VALUES (2, 2, '2026-03-22', '18:00:00', 60, '19:00:00', 'CANCELADA', CURRENT_TIMESTAMP);